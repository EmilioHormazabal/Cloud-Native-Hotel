package Hotel.usuario.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.service.UsuarioService;
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
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioService ser;

    @Test
    void list_sinToken_devuelve401() throws Exception {
        mvc.perform(get("/api/v1/usuario/list"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void list_sinRolAdmin_devuelve403() throws Exception {
        mvc.perform(get("/api/v1/usuario/list")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void list_conRolAdmin_devuelve200() throws Exception {
        when(ser.u_listar()).thenReturn(List.of(new Usuario()));
        mvc.perform(get("/api/v1/usuario/list")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void get_conRolAdmin_devuelve200() throws Exception {
        when(ser.u_recuperar(anyInt())).thenReturn(new Usuario());
        mvc.perform(get("/api/v1/usuario/get/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void get_sinRolAdmin_devuelve403ConAuth003() throws Exception {
        mvc.perform(get("/api/v1/usuario/get/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void me_sinToken_devuelve401() throws Exception {
        mvc.perform(get("/api/v1/usuario/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void me_autenticado_resuelvePorPreferredUsername() throws Exception {
        when(ser.u_por_correo(eq("cliente.prueba@test.cl"), isNull())).thenReturn(new Usuario());
        mvc.perform(get("/api/v1/usuario/me")
                .with(jwt().jwt(j -> j.claim("preferred_username", "cliente.prueba@test.cl"))))
            .andExpect(status().isOk());
    }

    @Test
    void rutaInexistente_devuelve404() throws Exception {
        mvc.perform(get("/api/v1/usuario/no-existe")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SYS-002"));
    }

    @Test
    void metodoNoPermitido_devuelve405() throws Exception {
        mvc.perform(post("/api/v1/usuario/list")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.code").value("SYS-003"));
    }
}
