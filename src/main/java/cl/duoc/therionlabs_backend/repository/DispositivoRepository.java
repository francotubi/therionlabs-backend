package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DispositivoRepository
        extends JpaRepository<Dispositivo, UUID> {

    // Identificar un dispositivo por el hash de su credencial
    Optional<Dispositivo> findByCredencialHash(String credencialHash);

    // Buscar dispositivos asociados a un captador
    Optional<Dispositivo> findByCaptadorId(UUID captadorId);
}
