package Hotel.bff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BffServiceTest {

    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
    }

    private BffService service(String usuario, String reserva, String servicio) {
        return new BffService(builder, usuario, reserva, servicio);
    }

    @Test
    void dashboardAgregaLasTresFuentesYReenviaElToken() {
        server.expect(requestTo("http://usuario.test/api/v1/usuario/me"))
              .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer abc"))
              .andRespond(withSuccess("{\"id\":7,\"correo\":\"a@b.c\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://reserva.test/api/v1/reserva/usuario/7"))
              .andRespond(withSuccess("[{\"id\":1},{\"id\":2}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://servicio.test/api/v1/servicio/list"))
              .andRespond(withSuccess("[{\"id\":9}]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service("http://usuario.test", "http://reserva.test", "http://servicio.test")
                .b_dashboard("Bearer abc");

        Map<?, ?> usuario = (Map<?, ?>) ((Map<?, ?>) salida.get("usuario")).get("datos");
        assertEquals(7, usuario.get("id"));
        assertEquals(2, ((List<?>) ((Map<?, ?>) salida.get("reservas")).get("datos")).size());
        assertEquals(1, ((List<?>) ((Map<?, ?>) salida.get("servicios")).get("datos")).size());
        server.verify();
    }

    @Test
    void microservicioCaidoReportaErrorSinTumbarElResto() {
        server.expect(requestTo("http://usuario.test/api/v1/usuario/me"))
              .andRespond(withServerError());
        server.expect(requestTo("http://servicio.test/api/v1/servicio/list"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service("http://usuario.test", "http://reserva.test", "http://servicio.test")
                .b_dashboard("Bearer abc");

        assertEquals("No se pudo consultar el microservicio", ((Map<?, ?>) salida.get("usuario")).get("error"));
        assertNull(((Map<?, ?>) salida.get("usuario")).get("datos"));
        assertEquals("No se pudo resolver el usuario del token", ((Map<?, ?>) salida.get("reservas")).get("error"));
        assertNotNull(((Map<?, ?>) salida.get("servicios")).get("datos"));
        server.verify();
    }

    @Test
    void destinoNoConfiguradoReportaErrorSinIntentarLlamar() {
        server.expect(requestTo("http://usuario.test/api/v1/usuario/me"))
              .andRespond(withSuccess("{\"id\":7}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://servicio.test/api/v1/servicio/list"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service("http://usuario.test", "", "http://servicio.test")
                .b_dashboard("Bearer abc");

        assertEquals("Destino no configurado", ((Map<?, ?>) salida.get("reservas")).get("error"));
        assertTrue(((Map<?, ?>) salida.get("servicios")).containsKey("datos"));
        server.verify();
    }

    @Test
    void adminResumenAgregaLasTresListas() {
        server.expect(requestTo("http://usuario.test/api/v1/usuario/list"))
              .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer abc"))
              .andRespond(withSuccess("[{\"id\":1}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://reserva.test/api/v1/reserva/list"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://servicio.test/api/v1/servicio/list"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Map<String, Object> salida = service("http://usuario.test", "http://reserva.test", "http://servicio.test")
                .b_admin("Bearer abc");

        assertEquals(1, ((List<?>) ((Map<?, ?>) salida.get("usuarios")).get("datos")).size());
        assertNotNull(((Map<?, ?>) salida.get("reservas")).get("datos"));
        assertNotNull(((Map<?, ?>) salida.get("servicios")).get("datos"));
        server.verify();
    }
}