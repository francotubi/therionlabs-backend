package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seguimientos")
@Getter
@Setter
public class Seguimiento {
    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prospecto_id", nullable = false)
    private Prospecto prospecto;

    @Column(nullable = false)
    private String responsable;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String canal;

    @Column(nullable = false, length = 500)
    private String observacion;

    @Column(nullable = false)
    private Instant fecha;
}
