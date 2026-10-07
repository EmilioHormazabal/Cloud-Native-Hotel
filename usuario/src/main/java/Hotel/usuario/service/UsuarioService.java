package Hotel.usuario.service;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.exception.BusinessRuleException;
import Hotel.usuario.exception.EntidadNoEncontradaException;
import Hotel.usuario.repository.UsuarioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository rep;

    public Usuario u_registrar(Usuario req){
        if (rep.findByCorreo(req.getCorreo()).isPresent()) {
            throw new BusinessRuleException("USER-002", HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + req.getCorreo());
        }
        req.setTipo_usuario(req.getTipo_usuario() != null ? req.getTipo_usuario() : "CLIENTE");
        return rep.save(req);
    }
    
    public Usuario u_login(String correo, String contrasenia){
        Usuario u = rep.findByCorreo(correo)
                .orElseThrow(() -> new EntidadNoEncontradaException("USER-003",
                        "No existe un usuario con el correo: " + correo));
        
        if (!u.getContrasenia().equals(contrasenia)) {
            throw new BusinessRuleException("USER-004", HttpStatus.UNAUTHORIZED,
                    "Las credenciales ingresadas son incorrectas");
        }
        return u;
    }
    
    // El BFF resuelve al usuario por el token, si es su primer acceso se crea aca
    public Usuario u_por_correo(String correo, String nombre){
        return rep.findByCorreo(correo)
                .orElseGet(() -> crearSiNoExiste(correo, nombre));
    }

    // Dos primeros accesos concurrentes del mismo correo: el que pierde la carrera re-lee
    private Usuario crearSiNoExiste(String correo, String nombre){
        try {
            return rep.save(u_desde_token(correo, nombre));
        } catch (DataIntegrityViolationException ex) {
            return rep.findByCorreo(correo)
                    .orElseThrow(() -> new EntidadNoEncontradaException("USER-006",
                            "No existe un usuario registrado con el correo: " + correo));
        }
    }

    private Usuario u_desde_token(String correo, String nombre){
        Usuario u = new Usuario();
        u.setCorreo(correo);
        u.setNombre(nombre != null && !nombre.isBlank() ? nombre : correo);
        u.setA_paterno("");
        u.setRut(0);
        u.setDv_rut("0");
        u.setContrasenia("");
        u.setTipo_usuario("CLIENTE");
        return u;
    }

    public Usuario u_recuperar(Integer id){
        return rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("USER-005",
                        "No existe un usuario con id: " + id));
    }
    
    public List<Usuario> u_listar(){
        return rep.findAll();
    }
    
    public Usuario u_modificar(Integer id, Usuario req){
        Usuario u_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("USER-005",
                        "No existe un usuario con id: " + id));
        u_mod.setNombre(req.getNombre());
        u_mod.setS_nombre(req.getS_nombre());
        u_mod.setA_paterno(req.getA_paterno());
        u_mod.setA_materno(req.getA_materno());
        u_mod.setRut(req.getRut());
        u_mod.setDv_rut(req.getDv_rut());
        u_mod.setEdad(req.getEdad());
        u_mod.setTipo_usuario(req.getTipo_usuario());
        u_mod.setCorreo(req.getCorreo());
        u_mod.setContrasenia(req.getContrasenia());
        u_mod.setTelefono(req.getTelefono());
        return rep.save(u_mod);
    }
    
    public Boolean u_retirar(Integer id){
        if (!rep.existsById(id)) {
            throw new EntidadNoEncontradaException("USER-005",
                    "No existe un usuario con id: " + id);
        }
        rep.deleteById(id);
        return true;
    }
}