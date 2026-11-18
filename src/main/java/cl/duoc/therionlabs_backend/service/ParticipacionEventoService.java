package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.Captador;
import cl.duoc.therionlabs_backend.entity.Evento;
import cl.duoc.therionlabs_backend.entity.ParticipacionEvento;

import cl.duoc.therionlabs_backend.repository.CaptadorRepository;
import cl.duoc.therionlabs_backend.repository.EventoRepository;
import cl.duoc.therionlabs_backend.repository.ParticipacionEventoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ParticipacionEventoService {

    private final ParticipacionEventoRepository participacionRepository;
    private final CaptadorRepository captadorRepository;
    private final EventoRepository eventoRepository;

    public ParticipacionEventoService(
            ParticipacionEventoRepository participacionRepository,
            CaptadorRepository captadorRepository,
            EventoRepository eventoRepository) {

        this.participacionRepository = participacionRepository;
        this.captadorRepository = captadorRepository;
        this.eventoRepository = eventoRepository;
    }

    @Transactional
    public ParticipacionEvento unirseEvento(
            UUID captadorId,
            UUID eventoId) {

        // 1. Buscar y bloquear al captador
        Captador captador = captadorRepository
                .buscarParaActualizar(captadorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Captador no encontrado"));

        if (!captador.isActivo()) {
            throw new IllegalArgumentException(
                    "El captador está deshabilitado");
        }

        // 2. Comprobar que el evento existe
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evento no encontrado"));

        if (!evento.isActivo()) {
            throw new IllegalArgumentException(
                    "El evento no está activo");
        }

        // 3. Buscar participación activa
        ParticipacionEvento participacionActual =
                participacionRepository
                        .findByCaptadorIdAndActivoTrue(captadorId)
                        .orElse(null);

        // 4. Si ya está en el mismo evento, no duplicar
        if (participacionActual != null &&
                participacionActual.getEvento()
                        .getId().equals(eventoId)) {
            return participacionActual;
        }

        LocalDateTime ahora = LocalDateTime.now();

        // 5. Cerrar participación anterior
        if (participacionActual != null) {
            participacionActual.setActivo(false);
            participacionActual.setFechaSalida(ahora);
            participacionRepository.save(participacionActual);
        }

        // 6. Buscar si ya participó en este evento
        ParticipacionEvento participacion =
                participacionRepository.findAll().stream()
                        .filter(p ->
                                p.getCaptador().getId()
                                        .equals(captadorId)
                                        && p.getEvento().getId()
                                        .equals(eventoId))
                        .findFirst()
                        .orElse(null);

        // 7. Crear una participación si no existe
        if (participacion == null) {
            participacion = new ParticipacionEvento();
            participacion.setCaptador(captador);
            participacion.setEvento(evento);
        }

        // 8. Activar participación
        participacion.setActivo(true);
        participacion.setFechaUnion(ahora);
        participacion.setFechaSalida(null);

        return participacionRepository.save(participacion);
    }
}
