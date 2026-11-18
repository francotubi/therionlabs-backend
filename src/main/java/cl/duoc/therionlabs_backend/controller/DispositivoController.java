package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.entity.Dispositivo;
import cl.duoc.therionlabs_backend.service.DispositivoService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final DispositivoService dispositivoService;

    public DispositivoController(
            DispositivoService dispositivoService) {
        this.dispositivoService = dispositivoService;
    }

    @GetMapping("/verificar")
    public RespuestaDispositivo verificar(
            @RequestHeader("X-Device-Token") String token) {

        try {
            Dispositivo dispositivo =
                    dispositivoService.validarCredencial(token);

            return new RespuestaDispositivo(
                    dispositivo.getId(),
                    dispositivo.getCaptador().getId(),
                    dispositivo.getCaptador().getIdentificadorLocal(),
                    true
            );


        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credencial no válida"
            );
        }
    }


    public record RespuestaDispositivo(
            UUID dispositivoId,
            UUID captadorId,
            String identificadorLocal,
            boolean autorizado
    ) {}
}
