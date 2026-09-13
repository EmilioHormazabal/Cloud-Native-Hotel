package Hotel.usuario.controller;

import Hotel.usuario.entity.Usuario;
import Hotel.usuario.service.UsuarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuario")
public class UsuarioController {
    
    @Autowired
    private UsuarioService ser;
    
    @PostMapping("/register")
    public Usuario uc_registrar(@RequestBody Usuario req){
        return ser.u_registrar(req);
    }
    
    @GetMapping("/login/{correo}/{contra}")
    public Usuario uc_login(@PathVariable String correo, @PathVariable String contra){
        return ser.u_login(correo, contra);
    }
    
    @GetMapping("/get/{id}")
    public Usuario uc_recuperar(@PathVariable Integer id){
        return ser.u_recuperar(id);
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> uc_listar(){
        return ser.u_listar();
    }
    
    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario uc_modificar(@PathVariable Integer id, @RequestBody Usuario req){
        return ser.u_modificar(id, req);
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean uc_retirar(@PathVariable Integer id){
        return ser.u_retirar(id);
    }
}