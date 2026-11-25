package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "eventos")
public class Evento {

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean eliminado = false;

    private java.time.Instant cerradoEn;

    public java.time.Instant getCerradoEn() {
        return cerradoEn;
    }

    public void setCerradoEn(java.time.Instant valor) {
        cerradoEn = valor;
    }

    @Id private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String colegio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String actividad;

    @Column(nullable = false)
    private String comuna;

    @Column(nullable = false)
    private boolean activo = true;

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Evento() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColegio() {
        return colegio;
    }

    public void setColegio(String colegio) {
        this.colegio = colegio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
