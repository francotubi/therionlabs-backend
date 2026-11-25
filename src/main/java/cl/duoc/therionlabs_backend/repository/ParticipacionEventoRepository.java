package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.ParticipacionEvento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParticipacionEventoRepository extends JpaRepository<ParticipacionEvento, UUID> {

    Optional<ParticipacionEvento> findByCaptadorIdAndActivoTrue(UUID captadorId);

    boolean existsByCaptadorIdAndEventoId(UUID captadorId, UUID eventoId);
}
