
package cl.duoc.therionlabs_backend.dto;

import java.util.UUID;

public record ProspectoSyncRequest(

        UUID id,
        UUID eventoId,

        // Datos personales
        String rut,
        String nombre,
        String apellidos,
        String fechaNacimiento,
        String celular,
        String email,

        // Datos academicos
        String establecimiento,
        String comunaResidencia,
        String comunaEstablecimiento,
        String actividadActual,
        String jornada,

        // Preferencias
        String region,
        String sede,
        String nivelInteres,
        String carrera1,
        String carrera2,
        String carrera3,
        String modalidad,
        String canalContacto,

        // Informacion adicional
        String observaciones,
        boolean autorizaContacto,

        Long fechaCaptura,
        String fotoRuta,
        Double latitud,
        Double longitud,
        Float precisionMetros,
        Long fechaUbicacion,
        String direccion
) {}
