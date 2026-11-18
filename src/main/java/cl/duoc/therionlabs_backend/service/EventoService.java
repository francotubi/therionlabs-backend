package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.Evento;
import cl.duoc.therionlabs_backend.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public List<Evento> listarEventosActivos() {
        return eventoRepository.findByActivoTrue();
    }

    public Evento buscarPorId(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Evento no encontrado"));
    }

    public Evento crearEvento(Evento evento) {

        if (evento.getNombre() == null ||
                evento.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del evento es obligatorio");
        }

        if (evento.getColegio() == null ||
                evento.getColegio().isBlank()) {
            throw new IllegalArgumentException(
                    "El colegio es obligatorio");
        }

        if (evento.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha del evento es obligatoria");
        }

        // El UUID se conservará desde Android.
        // Para pruebas locales permitimos generarlo aquí.
        if (evento.getId() == null) {
            evento.setId(UUID.randomUUID());
        }

        // Evita sobrescribir un evento al reenviarlo.
        if (eventoRepository.existsById(evento.getId())) {
            return eventoRepository.findById(evento.getId()).orElseThrow();
        }

        return eventoRepository.save(evento);
    }
}
