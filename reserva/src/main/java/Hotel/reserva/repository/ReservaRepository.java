package Hotel.reserva.repository;

import Hotel.reserva.entity.Reserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Integer>{

    List<Reserva> findByIdUsuario(Integer idUsuario);
}