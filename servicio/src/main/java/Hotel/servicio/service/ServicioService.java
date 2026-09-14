package Hotel.servicio.service;

import Hotel.servicio.entity.Servicio;
import Hotel.servicio.exception.EntidadNoEncontradaException;
import Hotel.servicio.repository.ServicioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {
    
    @Autowired
    private ServicioRepository rep;

    public Servicio s_guardar(Servicio req){
        return rep.save(req);
    }
    
    public Servicio s_recuperar(Integer id){
        return rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("SER-001",
                        "No existe un servicio con id: " + id));
    }
    
    public List<Servicio> s_listar(){
        return rep.findAll();
    }
    
    public Servicio s_modificar(Integer id, Servicio req){
        Servicio s_mod = rep.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("SER-001",
                        "No existe un servicio con id: " + id));
        s_mod.setNombre(req.getNombre());
        s_mod.setDescripcion(req.getDescripcion());
        s_mod.setTipoServicio(req.getTipoServicio());
        s_mod.setPrecio(req.getPrecio());
        s_mod.setNumHabitacion(req.getNumHabitacion());
        s_mod.setCapacidad(req.getCapacidad());
        s_mod.setDisponible(req.getDisponible());
        s_mod.setNivelServicio(req.getNivelServicio());
        return rep.save(s_mod);
    }
    
    public Boolean s_eliminar(Integer id){
        if (!rep.existsById(id)) {
            throw new EntidadNoEncontradaException("SER-001",
                    "No existe un servicio con id: " + id);
        }
        rep.deleteById(id);
        return true;
    }
}