package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.dto.ProspectoSyncRequest;
import cl.duoc.therionlabs_backend.entity.Captador;
import cl.duoc.therionlabs_backend.entity.Dispositivo;
import cl.duoc.therionlabs_backend.entity.ProspectoRechazado;
import cl.duoc.therionlabs_backend.repository.ParticipacionEventoRepository;
import cl.duoc.therionlabs_backend.repository.ProspectoRechazadoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProspectoRechazoService {

    private final ProspectoRechazadoRepository rechazoRepository;
    private final DispositivoService dispositivoService;
    private final ParticipacionEventoRepository participacionRepository;

    public ProspectoRechazoService(
            ProspectoRechazadoRepository rechazoRepository,
            DispositivoService dispositivoService,
            ParticipacionEventoRepository participacionRepository
    ) {
        this.rechazoRepository = rechazoRepository;
        this.dispositivoService = dispositivoService;
        this.participacionRepository = participacionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarRechazo(
            String token,
            ProspectoSyncRequest request,
            String motivo
    ) {

        // Identificar al captador mediante su token
        Dispositivo dispositivo =
                dispositivoService.validarCredencial(token);

        Captador captador = dispositivo.getCaptador();

        // Comprobar que pertenece al evento
        boolean participa = participacionRepository
                .existsByCaptadorIdAndEventoId(
                        captador.getId(),
                        request.eventoId()
                );

        if (!participa) {
            throw new SecurityException(
                    "Captador no autorizado para este evento"
            );
        }

        // Buscar un rechazo anterior del mismo prospecto
        ProspectoRechazado rechazo = rechazoRepository
                .findById(request.id())
                .orElseGet(ProspectoRechazado::new);

        // Evitar modificar un rechazo de otro captador
        if (rechazo.getProspectoId() != null &&
                (!rechazo.getCaptadorId().equals(captador.getId()) ||
                        !rechazo.getEventoId().equals(request.eventoId()))) {
            throw new SecurityException(
                    "El identificador pertenece a otro registro"
            );
        }

        rechazo.setProspectoId(request.id());
        rechazo.setEventoId(request.eventoId());
        rechazo.setCaptadorId(captador.getId());

        rechazo.setRut(request.rut());
        rechazo.setNombre(request.nombre());
        rechazo.setApellidos(request.apellidos());
        rechazo.setEmail(request.email());

        rechazo.setMotivo(motivo);
        rechazo.setFechaRechazo(Instant.now());

        rechazoRepository.save(rechazo);
    }
}
