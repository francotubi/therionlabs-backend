package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {

    List<Evento> findByActivoTrue();
}
