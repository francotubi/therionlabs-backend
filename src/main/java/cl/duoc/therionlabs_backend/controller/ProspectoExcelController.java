package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.service.ProspectoExcelService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/prospectos")
public class ProspectoExcelController {

    private final ProspectoExcelService excelService;
    private final String claveAdministrativa;

    public ProspectoExcelController(
            ProspectoExcelService excelService,
            @Value("${THERIONLABS_EXPORT_KEY}") String claveAdministrativa
    ) {
        this.excelService = excelService;
        this.claveAdministrativa = claveAdministrativa;
    }

    @GetMapping("/{eventoId}/excel")
    public ResponseEntity<byte[]> descargarExcel(
            @PathVariable UUID eventoId,
            @RequestHeader(
                    value = "X-Admin-Key",
                    required = false
            ) String claveRecibida
    ) throws IOException {

        // Verificar credencial administrativa
        if (claveRecibida == null ||
                claveRecibida.isBlank() ||
                !MessageDigest.isEqual(
                        claveAdministrativa.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        claveRecibida.getBytes(
                                StandardCharsets.UTF_8
                        )
                )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        // Generar Excel del evento solicitado
        byte[] archivo = excelService.generarExcel(eventoId);

        String nombreArchivo =
                "therionlabs_evento_" + eventoId + ".xlsx";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                nombreArchivo + "\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(archivo);
    }
}
