package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Evento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {

    List<Evento> findByActivoTrueAndEliminadoFalse();

    List<Evento> findAllByOrderByFechaAsc();

    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Evento e where e.id = :id")
    java.util.Optional<Evento> buscarParaActualizar(UUID id);
}
