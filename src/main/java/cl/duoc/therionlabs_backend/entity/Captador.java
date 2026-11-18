package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "captadores")
public class Captador {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "identificador_local")
    private String identificadorLocal;

    @Column(nullable = false)
    private boolean activo = true;

    public Captador() {
    }

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

    public String getIdentificadorLocal() {
        return identificadorLocal;
    }

    public void setIdentificadorLocal(String identificadorLocal) {
        this.identificadorLocal = identificadorLocal;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
