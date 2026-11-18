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

    // Obtener todos los eventos activos
    @GetMapping
    public List<Evento> listarEventos() {
        return eventoService.listarEventosActivos();
    }

    // Buscar un evento por su UUID
    @GetMapping("/{id}")
    public Evento obtenerEvento(@PathVariable UUID id) {
        return eventoService.buscarPorId(id);
    }

    // Crear un evento
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Evento crearEvento(@RequestBody Evento evento) {
        return eventoService.crearEvento(evento);
    }
}
