package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "prospectos")
@Getter
@Setter
@NoArgsConstructor
public class Prospecto {

    @Id
    private UUID id;

    // Relaciones con las tablas existentes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captador_id", nullable = false)
    private Captador captador;

    // Datos personales
    @Column(nullable = false)
    private String rut;

    @Column(name = "rut_normalizado", length = 20)
    private String rutNormalizado;

    @Column(name = "email_normalizado", length = 255)
    private String emailNormalizado;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    private String fechaNacimiento;
    private String celular;
    private String email;

    // Datos academicos
    private String establecimiento;
    private String comunaResidencia;
    private String comunaEstablecimiento;
    private String actividadActual;
    private String jornada;

    // Preferencias
    private String region;
    private String sede;
    private String nivelInteres;
    private String carrera1;
    private String carrera2;
    private String carrera3;
    private String modalidad;
    private String canalContacto;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private boolean autorizaContacto;

    // Fecha de captura en milisegundos
    private Long fechaCaptura;

    // Informacion opcional del telefono
    private String fotoRuta;
    private Double latitud;
    private Double longitud;
    private Float precisionMetros;
    private Long fechaUbicacion;
    private String direccion;
}
