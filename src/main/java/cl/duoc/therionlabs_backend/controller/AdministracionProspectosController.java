package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.dto.*;
import cl.duoc.therionlabs_backend.service.AdministracionProspectosService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdministracionProspectosController {
    private final AdministracionProspectosService servicio;

    public AdministracionProspectosController(AdministracionProspectosService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/eventos/{eventoId}/prospectos")
    public List<ProspectoRespuesta> listar(
            @PathVariable UUID eventoId,
            @RequestParam(defaultValue = "false") boolean incluirEliminados) {
        return servicio.listar(eventoId, incluirEliminados);
    }

    @GetMapping("/prospectos/{id}")
    public ProspectoRespuesta consultar(@PathVariable UUID id) {
        return servicio.consultar(id);
    }

    @PutMapping("/prospectos/{id}")
    public ProspectoRespuesta actualizar(
            @PathVariable UUID id, @RequestBody ProspectoSyncRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("/prospectos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        servicio.eliminar(id);
    }

    @GetMapping("/prospectos/{id}/seguimientos")
    public List<SeguimientoRespuesta> gestiones(@PathVariable UUID id) {
        return servicio.gestiones(id);
    }

    @PostMapping("/prospectos/{id}/seguimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public SeguimientoRespuesta registrar(
            @PathVariable UUID id, @RequestBody GestionSolicitud datos) {
        return servicio.registrarGestion(id, datos);
    }
}
