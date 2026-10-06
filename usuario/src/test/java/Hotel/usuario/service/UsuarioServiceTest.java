package Hotel.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository rep;

    @InjectMocks
    private UsuarioService ser;

    @Test
    void usuarioExistenteSeDevuelveSinCrear() {
        Usuario existente = new Usuario();
        existente.setCorreo("admin.prueba@test.cl");
        when(rep.findByCorreo("admin.prueba@test.cl")).thenReturn(Optional.of(existente));

        Usuario res = ser.u_por_correo("admin.prueba@test.cl", "Admin Prueba");

        assertEquals(existente, res);
    }

    @Test
    void usuarioNuevoSeCreaDesdeElToken() {
        when(rep.findByCorreo("nuevo@test.cl")).thenReturn(Optional.empty());
        when(rep.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario res = ser.u_por_correo("nuevo@test.cl", "Nuevo Usuario");

        assertNotNull(res);
        assertEquals("nuevo@test.cl", res.getCorreo());
        assertEquals("Nuevo Usuario", res.getNombre());
        assertEquals("CLIENTE", res.getTipo_usuario());
    }
}
