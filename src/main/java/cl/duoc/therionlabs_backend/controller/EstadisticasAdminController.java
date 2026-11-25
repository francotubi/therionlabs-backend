package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.entity.Prospecto;
import cl.duoc.therionlabs_backend.repository.ProspectoRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/estadisticas")
public class EstadisticasAdminController {

    private final ProspectoRepository prospectoRepository;
    private final byte[] claveAdministrativa;

    public EstadisticasAdminController(
            ProspectoRepository prospectoRepository,
            @Value("${THERIONLABS_EXPORT_KEY}") String claveAdministrativa) {
        this.prospectoRepository = prospectoRepository;
        this.claveAdministrativa = claveAdministrativa.getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping("/prospectos-por-evento")
    public ResponseEntity<?> prospectosPorEvento(
            @RequestHeader(value = "X-Admin-Key", required = false) String claveRecibida) {
        if (claveRecibida == null
                || !MessageDigest.isEqual(
                        claveAdministrativa, claveRecibida.getBytes(StandardCharsets.UTF_8))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Prospecto> prospectos = prospectoRepository.findAll();

        Map<UUID, Long> totales =
                prospectos.stream()
                        .filter(p -> p.getEvento() != null && !p.isEliminado())
                        .collect(
                                Collectors.groupingBy(
                                        p -> p.getEvento().getId(), Collectors.counting()));

        return ResponseEntity.ok(totales);
    }
}
