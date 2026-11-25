package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "participaciones_evento",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"captador_id", "evento_id"})})
public class ParticipacionEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captador_id", nullable = false)
    private Captador captador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private LocalDateTime fechaUnion;

    private LocalDateTime fechaSalida;

    @Column(nullable = false)
    private boolean activo = true;

    public ParticipacionEvento() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Captador getCaptador() {
        return captador;
    }

    public void setCaptador(Captador captador) {
        this.captador = captador;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public LocalDateTime getFechaUnion() {
        return fechaUnion;
    }

    public void setFechaUnion(LocalDateTime fechaUnion) {
        this.fechaUnion = fechaUnion;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
