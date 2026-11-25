package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones_acceso")
@Getter
@Setter
public class SesionAcceso {
    @Id private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String resumen;

    @ManyToOne(optional = false)
    private Cuenta cuenta;

    @Column(nullable = false)
    private Instant vence;
}
