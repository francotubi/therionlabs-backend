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
    public ParticipacionEvento unirseEvento(UUID captadorId, UUID eventoId) {
        Captador captador =
                captadorRepository
                        .buscarParaActualizar(captadorId)
                        .orElseThrow(() -> new IllegalArgumentException("Captador no encontrado"));

        if (!captador.isActivo()) {
            throw new IllegalArgumentException("El captador está deshabilitado");
        }
        Evento evento =
                eventoRepository
                        .findById(eventoId)
                        .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (!evento.isActivo() || evento.isEliminado()) {
            throw new IllegalArgumentException("El evento no está activo");
        }
        ParticipacionEvento participacionActual =
                participacionRepository.findByCaptadorIdAndActivoTrue(captadorId).orElse(null);
        if (participacionActual != null
                && participacionActual.getEvento().getId().equals(eventoId)) {
            return participacionActual;
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (participacionActual != null) {
            participacionActual.setActivo(false);
            participacionActual.setFechaSalida(ahora);
            participacionRepository.save(participacionActual);
        }
        ParticipacionEvento participacion =
                participacionRepository.findAll().stream()
                        .filter(p ->
                                p.getCaptador().getId().equals(captadorId)
                                        && p.getEvento().getId().equals(eventoId))
                        .findFirst()
                        .orElse(null);
        if (participacion == null) {
            participacion = new ParticipacionEvento();
            participacion.setCaptador(captador);
            participacion.setEvento(evento);
        }
        participacion.setActivo(true);
        participacion.setFechaUnion(ahora);
        participacion.setFechaSalida(null);

        return participacionRepository.save(participacion);
    }
}
