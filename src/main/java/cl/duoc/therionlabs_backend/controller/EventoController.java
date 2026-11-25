package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.entity.Evento;
import cl.duoc.therionlabs_backend.service.EventoService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public List<Evento> listarEventos(
            @RequestParam(defaultValue = "false") boolean incluirEliminados) {
        return incluirEliminados
                ? eventoService.listarCambios()
                : eventoService.listarEventosActivos();
    }

    @GetMapping("/{id}")
    public Evento obtenerEvento(@PathVariable UUID id) {
        return eventoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Evento actualizar(@PathVariable UUID id, @RequestBody Evento datos) {
        return eventoService.actualizar(id, datos);
    }

    @PostMapping("/{id}/cerrar")
    public Evento cerrar(@PathVariable UUID id) {
        return eventoService.cerrar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        eventoService.eliminar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Evento crearEvento(@RequestBody Evento evento) {
        return eventoService.crearEvento(evento);
    }
}
