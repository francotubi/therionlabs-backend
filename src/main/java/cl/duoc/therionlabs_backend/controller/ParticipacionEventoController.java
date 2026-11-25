package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.entity.ParticipacionEvento;
import cl.duoc.therionlabs_backend.service.ParticipacionEventoService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/participaciones")
public class ParticipacionEventoController {

    private final ParticipacionEventoService participacionService;
    private final cl.duoc.therionlabs_backend.service.DispositivoService dispositivos;

    public ParticipacionEventoController(
            ParticipacionEventoService participacionService,
            cl.duoc.therionlabs_backend.service.DispositivoService dispositivos) {
        this.dispositivos = dispositivos;
        this.participacionService = participacionService;
    }

    @PostMapping("/unirse")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaParticipacion unirseEvento(
            @RequestHeader("X-Device-Token") String token,
            @RequestParam UUID captadorId,
            @RequestParam UUID eventoId) {

        if (!dispositivos.validarCredencial(token).getCaptador().getId().equals(captadorId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "El token pertenece a otro captador");
        }
        ParticipacionEvento participacion = participacionService.unirseEvento(captadorId, eventoId);

        return new RespuestaParticipacion(
                participacion.getId(),
                participacion.getCaptador().getId(),
                participacion.getEvento().getId(),
                participacion.isActivo());
    }

    public record RespuestaParticipacion(
            UUID participacionId, UUID captadorId, UUID eventoId, boolean activo) {}
}
