package Hotel.bff.service;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * BFF: agrega usuario, reservas y servicios en una sola respuesta.
 * Cada campo sale con "datos" o "error", un microservicio caido no tumba el resto.
 */
@Slf4j
@Service
public class BffService {

    private final RestClient restClient;
    private final String urlUsuario;
    private final String urlReserva;
    private final String urlServicio;

    public BffService(RestClient.Builder builder,
            @Value("${bff.microservicios.usuario.url}") String urlUsuario,
            @Value("${bff.microservicios.reserva.url}") String urlReserva,
            @Value("${bff.microservicios.servicio.url}") String urlServicio) {
        this.restClient = builder.build();
        this.urlUsuario = urlUsuario;
        this.urlReserva = urlReserva;
        this.urlServicio = urlServicio;
    }

    /** Una sola llamada del frontend en lugar de tres. */
    public Map<String, Object> b_dashboard(String autorizacion) {
        Map<String, Object> salida = new LinkedHashMap<>();
        Map<String, Object> me = consultar(urlUsuario, "/api/v1/usuario/me", autorizacion);
        salida.put("usuario", me);
        Integer id = idDe(me);
        salida.put("reservas", id == null
                ? error("No se pudo resolver el usuario del token")
                : consultar(urlReserva, "/api/v1/reserva/usuario/" + id, autorizacion));
        salida.put("servicios", consultar(urlServicio, "/api/v1/servicio/list", autorizacion));
        return salida;
    }

    /** Resumen global, solo para ADMIN. */
    public Map<String, Object> b_admin(String autorizacion) {
        Map<String, Object> salida = new LinkedHashMap<>();
        salida.put("usuarios", consultar(urlUsuario, "/api/v1/usuario/list", autorizacion));
        salida.put("reservas", consultar(urlReserva, "/api/v1/reserva/list", autorizacion));
        salida.put("servicios", consultar(urlServicio, "/api/v1/servicio/list", autorizacion));
        return salida;
    }

    private Integer idDe(Map<String, Object> me) {
        if (me.get("datos") instanceof Map<?, ?> datos && datos.get("id") instanceof Number n) {
            return n.intValue();
        }
        return null;
    }

    private Map<String, Object> error(String mensaje) {
        Map<String, Object> salida = new LinkedHashMap<>();
        salida.put("error", mensaje);
        return salida;
    }

    private Map<String, Object> consultar(String base, String ruta, String autorizacion) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        if (base == null || base.isBlank()) {
            log.warn("Destino no configurado para {}", ruta);
            resultado.put("error", "Destino no configurado");
            return resultado;
        }
        String url = base.endsWith("/") ? base.substring(0, base.length() - 1) + ruta : base + ruta;
        try {
            // Object.class: Map si la respuesta es objeto, List si es array
            resultado.put("datos", restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, autorizacion)
                    .retrieve()
                    .body(Object.class));
        } catch (RestClientException ex) {
            log.error("Fallo al consultar {} [{}]: {}", ruta, url, ex.getMessage());
            resultado.put("error", "No se pudo consultar el microservicio");
        }
        return resultado;
    }
}