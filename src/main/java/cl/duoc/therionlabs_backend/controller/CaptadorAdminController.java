package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.entity.Captador;
import cl.duoc.therionlabs_backend.service.CaptadorAdminService;
import cl.duoc.therionlabs_backend.service.DispositivoService;
import cl.duoc.therionlabs_backend.repository.CaptadorRepository;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/captadores")
public class CaptadorAdminController {

    private final CaptadorRepository captadorRepository;
    private final CaptadorAdminService captadorService;
    private final DispositivoService dispositivoService;
    private final byte[] claveAdministrativa;

    public CaptadorAdminController(
            CaptadorAdminService captadorService,
            DispositivoService dispositivoService,
            @Value("${THERIONLABS_EXPORT_KEY}")
            String claveAdministrativa,
            CaptadorRepository captadorRepository
    ) {
        this.captadorService = captadorService;
        this.dispositivoService = dispositivoService;
        this.claveAdministrativa =
                claveAdministrativa.getBytes(StandardCharsets.UTF_8);
        this.captadorRepository = captadorRepository;
    }

    @PostMapping
    public ResponseEntity<?> registrar(
            @RequestHeader(
                    value = "X-Admin-Key",
                    required = false
            ) String claveRecibida,
            @RequestBody RegistroCaptadorRequest request
    ) {

        if (claveRecibida == null ||
                !MessageDigest.isEqual(
                        claveAdministrativa,
                        claveRecibida.getBytes(StandardCharsets.UTF_8)
                )) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        try {
            Captador captador = captadorService.registrarCaptador(
                    request.identificadorLocal(),
                    request.nombre()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new CaptadorRespuesta(
                            captador.getId(),
                            captador.getIdentificadorLocal(),
                            captador.getNombre(),
                            captador.isActivo()
                    ));

        } catch (IllegalArgumentException e) {

            HttpStatus estado = "El captador ya está registrado"
                    .equals(e.getMessage())
                    ? HttpStatus.CONFLICT
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity
                    .status(estado)
                    .body(new ErrorRespuesta(e.getMessage()));
        }
    }

    public record RegistroCaptadorRequest(
            String identificadorLocal,
            String nombre
    ) {}

    public record CaptadorRespuesta(
            UUID id,
            String identificadorLocal,
            String nombre,
            boolean activo
    ) {}


    @PostMapping("/{captadorId}/dispositivo")
    public ResponseEntity<?> generarToken(
            @RequestHeader(
                    value = "X-Admin-Key",
                    required = false
            ) String claveRecibida,
            @PathVariable UUID captadorId
    ) {

        // Solo Administración puede generar tokens.
        if (claveRecibida == null ||
                !MessageDigest.isEqual(
                        claveAdministrativa,
                        claveRecibida.getBytes(StandardCharsets.UTF_8)
                )) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        try {
            var resultado =
                    dispositivoService.registrarDispositivo(captadorId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorRespuesta(e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorRespuesta(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<CaptadorRespuesta>> listarCaptadores(
            @RequestHeader(
                    value = "X-Admin-Key",
                    required = false
            ) String claveRecibida
    ) {
        if (claveRecibida == null ||
                !MessageDigest.isEqual(
                        claveAdministrativa,
                        claveRecibida.getBytes(StandardCharsets.UTF_8)
                )) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<CaptadorRespuesta> captadores =
                captadorRepository.findAll()
                        .stream()
                        .sorted(Comparator.comparing(
                                Captador::getIdentificadorLocal
                        ))
                        .map(c -> new CaptadorRespuesta(
                                c.getId(),
                                c.getIdentificadorLocal(),
                                c.getNombre(),
                                c.isActivo()
                        ))
                        .toList();
        return ResponseEntity.ok(captadores);
    }

    @PostMapping("/{captadorId}/renovar-credencial")
    public ResponseEntity<?> renovarCredencial(
            @PathVariable UUID captadorId,
            @RequestHeader(
                    value = "X-Admin-Key",
                    required = false
            ) String claveRecibida
    ) {

        if (claveRecibida == null ||
                !MessageDigest.isEqual(
                        claveAdministrativa,
                        claveRecibida.getBytes(StandardCharsets.UTF_8)
                )) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorRespuesta(
                            "Clave administrativa incorrecta"
                    ));
        }
        try {
            // Renovar la credencial del dispositivo existente
            var resultado = dispositivoService
                    .renovarCredencial(captadorId);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorRespuesta(e.getMessage()));
        }
    }
    public record ErrorRespuesta(String mensaje) {}
}
