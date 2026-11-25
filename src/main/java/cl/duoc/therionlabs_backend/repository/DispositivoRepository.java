package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Dispositivo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DispositivoRepository extends JpaRepository<Dispositivo, UUID> {
    @EntityGraph(attributePaths = "captador")
    Optional<Dispositivo> findByCredencialHash(String credencialHash);

    Optional<Dispositivo> findByCaptadorId(UUID captadorId);
}
