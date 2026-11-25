package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.ProspectoRechazado;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProspectoRechazadoRepository extends JpaRepository<ProspectoRechazado, UUID> {

    List<ProspectoRechazado> findByEventoId(UUID eventoId);

    long countByEventoId(UUID eventoId);
}
