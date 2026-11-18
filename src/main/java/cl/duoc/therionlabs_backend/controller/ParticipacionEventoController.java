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

    public ParticipacionEventoController(
            ParticipacionEventoService participacionService) {
        this.participacionService = participacionService;
    }

    @PostMapping("/unirse")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaParticipacion unirseEvento(
            @RequestParam UUID captadorId,
            @RequestParam UUID eventoId) {

        ParticipacionEvento participacion =
                participacionService.unirseEvento(captadorId, eventoId);

        return new RespuestaParticipacion(
                participacion.getId(),
                participacion.getCaptador().getId(),
                participacion.getEvento().getId(),
                participacion.isActivo()
        );
    }

    public record RespuestaParticipacion(
            UUID participacionId,
            UUID captadorId,
            UUID eventoId,
            boolean activo
    ) {}
}
