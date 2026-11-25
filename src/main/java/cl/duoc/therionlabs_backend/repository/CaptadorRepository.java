package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Captador;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CaptadorRepository
        extends JpaRepository<Captador, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Captador c WHERE c.id = :id")
    Optional<Captador> buscarParaActualizar(
            @Param("id") UUID id
    );

    // Buscar captadores por su identificador de Android
    Optional<Captador> findByIdentificadorLocal(
            String identificadorLocal
    );

    // Comprobar si CAP-01, CAP-02, etc. ya existen
    boolean existsByIdentificadorLocal(
            String identificadorLocal
    );
}
