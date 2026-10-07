package Hotel.servicio.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.servicio.service.ServicioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServicioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioService ser;

    @Test
    void list_sinToken_devuelve401() throws Exception {
        mvc.perform(get("/api/v1/servicio/list"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void list_conCliente_devuelve200() throws Exception {
        when(ser.s_listar()).thenReturn(List.of());
        mvc.perform(get("/api/v1/servicio/list")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isOk());
    }

    @Test
    void delete_sinRolAdmin_devuelve403() throws Exception {
        mvc.perform(delete("/api/v1/servicio/delete/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void delete_conRolAdmin_devuelve200() throws Exception {
        when(ser.s_eliminar(1)).thenReturn(true);
        mvc.perform(delete("/api/v1/servicio/delete/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void rutaInexistente_devuelve404() throws Exception {
        mvc.perform(get("/api/v1/servicio/no-existe")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SYS-002"));
    }

    @Test
    void metodoNoPermitido_devuelve405() throws Exception {
        mvc.perform(post("/api/v1/servicio/list")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.code").value("SYS-003"));
    }
}
