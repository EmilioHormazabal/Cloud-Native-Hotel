package Hotel.usuario.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
            .andExpect(status().isUnauthorized());
    }

    @Test
    void list_sinRolAdmin_devuelve403() throws Exception {
        mvc.perform(get("/api/v1/usuario/list")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void list_conRolAdmin_devuelve200() throws Exception {
        when(ser.u_listar()).thenReturn(List.of(new Usuario()));
        mvc.perform(get("/api/v1/usuario/list")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void get_autenticado_devuelve200() throws Exception {
        when(ser.u_recuperar(anyInt())).thenReturn(new Usuario());
        mvc.perform(get("/api/v1/usuario/get/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isOk());
    }
}
