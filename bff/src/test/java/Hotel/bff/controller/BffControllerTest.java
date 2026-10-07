package Hotel.bff.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.bff.service.BffService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BffControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BffService ser;

    @Test
    void dashboard_sinToken_devuelve401ConAuth001() throws Exception {
        mvc.perform(get("/api/v1/bff/dashboard"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void dashboard_autenticado_devuelve200() throws Exception {
        when(ser.b_dashboard("Bearer abc")).thenReturn(Map.of());
        mvc.perform(get("/api/v1/bff/dashboard")
                .with(jwt().jwt(j -> j.tokenValue("abc"))
                        .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isOk());
    }

    @Test
    void admin_sinRolAdmin_devuelve403ConAuth003() throws Exception {
        mvc.perform(get("/api/v1/bff/admin/resumen")
                .with(jwt().jwt(j -> j.tokenValue("abc"))
                        .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void admin_conRolAdmin_devuelve200() throws Exception {
        when(ser.b_admin("Bearer abc")).thenReturn(Map.of());
        mvc.perform(get("/api/v1/bff/admin/resumen")
                .with(jwt().jwt(j -> j.tokenValue("abc"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void rutaInexistente_devuelve404() throws Exception {
        mvc.perform(get("/api/v1/bff/no-existe")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SYS-002"));
    }

    @Test
    void metodoNoPermitido_devuelve405() throws Exception {
        mvc.perform(post("/api/v1/bff/dashboard")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.code").value("SYS-003"));
    }
}
