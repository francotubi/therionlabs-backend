package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
public class Cuenta {
    @Id private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String usuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private String sal;

    @Column(nullable = false)
    private String resumenClave;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "captador_id", unique = true)
    private Captador captador;
}
