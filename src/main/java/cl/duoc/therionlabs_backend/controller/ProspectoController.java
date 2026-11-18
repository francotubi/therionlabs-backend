package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.dto.ProspectoSyncRequest;
import cl.duoc.therionlabs_backend.entity.Prospecto;
import cl.duoc.therionlabs_backend.exception.ProspectoDuplicadoException;
import cl.duoc.therionlabs_backend.service.ProspectoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/prospectos")
public class ProspectoController {

    private final ProspectoService prospectoService;

    public ProspectoController(ProspectoService prospectoService) {
        this.prospectoService = prospectoService;
    }

    @PostMapping("/sync")
    public ResponseEntity<RespuestaSync> sincronizar(
            @RequestHeader("X-Device-Token") String token,
            @RequestBody ProspectoSyncRequest request) {

        try {
            Prospecto prospecto =
                    prospectoService.recibirProspecto(token, request);

            return ResponseEntity.ok(
                    new RespuestaSync(
                            prospecto.getId(),
                            "SINCRONIZADO",
                            "Prospecto recibido correctamente"
                    )
            );

        } catch (SecurityException e) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credencial de dispositivo inválida"
            );

        } catch (ProspectoDuplicadoException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new RespuestaSync(
                            request.id(),
                            "CONFLICTO",
                            e.getMessage()
                    ));

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    public record RespuestaSync(
            UUID prospectoId,
            String estado,
            String mensaje
    ) {}
}
