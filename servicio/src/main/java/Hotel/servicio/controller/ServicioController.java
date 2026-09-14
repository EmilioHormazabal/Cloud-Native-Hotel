package Hotel.servicio.controller;

import Hotel.servicio.entity.Servicio;
import Hotel.servicio.service.ServicioService;
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
@RequestMapping("/api/v1/servicio")
public class ServicioController {
    
    @Autowired
    private ServicioService ser;
    
    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public Servicio sc_guardar(@RequestBody Servicio req){
        return ser.s_guardar(req);
    }
    
    @GetMapping("/get/{id}")
    public Servicio sc_recuperar(@PathVariable Integer id){
        return ser.s_recuperar(id);
    }
    
    @GetMapping("/list")
    public List<Servicio> sc_listar(){
        return ser.s_listar();
    }
    
    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Servicio sc_modificar(@PathVariable Integer id, @RequestBody Servicio req){
        return ser.s_modificar(id, req);
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean sc_eliminar(@PathVariable Integer id){
        return ser.s_eliminar(id);
    }
}