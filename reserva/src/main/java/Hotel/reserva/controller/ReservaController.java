package Hotel.reserva.controller;

import Hotel.reserva.entity.Reserva;
import Hotel.reserva.service.ReservaService;
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
@RequestMapping("/api/v1/reserva")
public class ReservaController {
    
    @Autowired
    private ReservaService ser;
    
    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public Reserva rc_guardar(@RequestBody Reserva req){
        return ser.r_guardar(req);
    }
    
    @GetMapping("/get/{id}")
    public Reserva rc_recuperar(@PathVariable Integer id){
        return ser.r_recuperar(id);
    }
    
    @GetMapping("/list")
    public List<Reserva> rc_listar(){
        return ser.r_listar();
    }
    
    @GetMapping("/usuario/{idUsuario}")
    public List<Reserva> rc_listar_por_usuario(@PathVariable Integer idUsuario){
        return ser.r_listar_por_usuario(idUsuario);
    }
    
    @PutMapping("/put/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Reserva rc_modificar(@PathVariable Integer id, @RequestBody Reserva req){
        return ser.r_modificar(id, req);
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean rc_eliminar(@PathVariable Integer id){
        return ser.r_eliminar(id);
    }
}