package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Prospecto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProspectoRepository extends JpaRepository<Prospecto, UUID> {

    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Prospecto p where p.id = :id")
    java.util.Optional<Prospecto> buscarParaActualizar(UUID id);

    boolean existsByEventoIdAndEliminadoFalse(UUID eventoId);

    boolean existsByEventoId(UUID eventoId);

    List<Prospecto> findByEventoIdAndEliminadoFalseOrderByFechaCapturaDesc(UUID eventoId);

    boolean existsByEventoIdAndRutNormalizadoAndIdNot(UUID eventoId, String rut, UUID id);

    boolean existsByEventoIdAndEmailNormalizadoAndIdNot(UUID eventoId, String correo, UUID id);

    List<Prospecto> findByEventoId(UUID eventoId);

    boolean existsByEventoIdAndRutIgnoreCase(UUID eventoId, String rut);

    boolean existsByEventoIdAndEmailIgnoreCase(UUID eventoId, String email);

    boolean existsByEventoIdAndRutNormalizado(UUID eventoId, String rutNormalizado);

    boolean existsByEventoIdAndEmailNormalizado(UUID eventoId, String emailNormalizado);
}
