package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prospectos_rechazados")
@Getter
@Setter
@NoArgsConstructor
public class ProspectoRechazado {

    @Id private UUID prospectoId;

    @Column(nullable = false)
    private UUID eventoId;

    @Column(nullable = false)
    private UUID captadorId;

    private String rut;
    private String nombre;
    private String apellidos;
    private String email;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private Instant fechaRechazo;
}
