package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.ParticipacionEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParticipacionEventoRepository
        extends JpaRepository<ParticipacionEvento, UUID> {

    // Buscar la participación activa de un captador
    Optional<ParticipacionEvento> findByCaptadorIdAndActivoTrue(
            UUID captadorId
    );

    // Comprobar si un captador ya participa en un evento
    boolean existsByCaptadorIdAndEventoId(
            UUID captadorId,
            UUID eventoId
    );
}
