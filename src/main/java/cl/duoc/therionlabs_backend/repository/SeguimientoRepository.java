package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Seguimiento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, UUID> {
    List<Seguimiento> findByProspectoIdOrderByFechaDescIdDesc(UUID prospectoId);

    boolean existsByProspectoId(UUID prospectoId);
}
