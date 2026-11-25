package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.service.ProspectoExcelService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/prospectos")
public class ProspectoExcelController {
    private final ProspectoExcelService excel;

    public ProspectoExcelController(ProspectoExcelService excel) {
        this.excel = excel;
    }

    @GetMapping("/{eventoId}/excel")
    public ResponseEntity<byte[]> descargarExcel(@PathVariable UUID eventoId) throws IOException {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"therionlabs_evento_" + eventoId + ".xlsx\"")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel.generarExcel(eventoId));
    }
}
