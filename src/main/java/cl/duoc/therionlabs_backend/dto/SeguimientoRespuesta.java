package cl.duoc.therionlabs_backend.dto;

import cl.duoc.therionlabs_backend.entity.Seguimiento;

import java.time.Instant;
import java.util.UUID;

public record SeguimientoRespuesta(
        UUID id,
        UUID prospectoId,
        String responsable,
        String estado,
        String canal,
        String observacion,
        Instant fecha) {
    public static SeguimientoRespuesta desde(Seguimiento datos) {
        return new SeguimientoRespuesta(
                datos.getId(),
                datos.getProspecto().getId(),
                datos.getResponsable(),
                datos.getEstado(),
                datos.getCanal(),
                datos.getObservacion(),
                datos.getFecha());
    }
}
