package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.SesionAcceso;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.*;

public interface SesionAccesoRepository extends JpaRepository<SesionAcceso, UUID> {
    Optional<SesionAcceso> findByResumen(String resumen);

    void deleteByVenceBefore(Instant fecha);
}
