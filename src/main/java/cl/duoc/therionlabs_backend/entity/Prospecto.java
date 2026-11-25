package cl.duoc.therionlabs_backend.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "prospectos")
@Getter
@Setter
@NoArgsConstructor
public class Prospecto {

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean eliminado = false;

    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captador_id", nullable = false)
    private Captador captador;

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
    private String establecimiento;
    private String comunaResidencia;
    private String comunaEstablecimiento;
    private String actividadActual;
    private String jornada;
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
    private Long fechaCaptura;
    private String fotoRuta;
    private Double latitud;
    private Double longitud;
    private Float precisionMetros;
    private Long fechaUbicacion;
    private String direccion;
}
