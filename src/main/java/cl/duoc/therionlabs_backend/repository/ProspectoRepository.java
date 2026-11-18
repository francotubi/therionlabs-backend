package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Prospecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProspectoRepository
        extends JpaRepository<Prospecto, UUID> {

    // Buscar prospectos de un evento
    List<Prospecto> findByEventoId(UUID eventoId);

    // Comprobar RUT duplicado dentro de un evento
    boolean existsByEventoIdAndRutIgnoreCase(
            UUID eventoId,
            String rut
    );

    // Comprobar correo duplicado dentro de un evento
    boolean existsByEventoIdAndEmailIgnoreCase(
            UUID eventoId,
            String email
    );

    boolean existsByEventoIdAndRutNormalizado(
            UUID eventoId,
            String rutNormalizado
    );

    boolean existsByEventoIdAndEmailNormalizado(
            UUID eventoId,
            String emailNormalizado
    );

}
