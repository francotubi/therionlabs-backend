package cl.duoc.therionlabs_backend.repository;

import cl.duoc.therionlabs_backend.entity.Cuenta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CuentaRepository extends JpaRepository<Cuenta, UUID> {
    Optional<Cuenta> findByUsuario(String usuario);

    boolean existsByCaptadorId(UUID captadorId);
}
