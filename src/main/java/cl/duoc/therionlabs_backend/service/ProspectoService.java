package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.dto.ProspectoSyncRequest;
import cl.duoc.therionlabs_backend.entity.Captador;
import cl.duoc.therionlabs_backend.entity.Dispositivo;
import cl.duoc.therionlabs_backend.entity.Evento;
import cl.duoc.therionlabs_backend.entity.Prospecto;
import cl.duoc.therionlabs_backend.exception.ProspectoDuplicadoException;
import cl.duoc.therionlabs_backend.repository.EventoRepository;
import cl.duoc.therionlabs_backend.repository.ParticipacionEventoRepository;
import cl.duoc.therionlabs_backend.repository.ProspectoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class ProspectoService {

    private final ProspectoRepository prospectoRepository;
    private final EventoRepository eventoRepository;
    private final ParticipacionEventoRepository participacionRepository;
    private final DispositivoService dispositivoService;

    public ProspectoService(
            ProspectoRepository prospectoRepository,
            EventoRepository eventoRepository,
            ParticipacionEventoRepository participacionRepository,
            DispositivoService dispositivoService) {

        this.prospectoRepository = prospectoRepository;
        this.eventoRepository = eventoRepository;
        this.participacionRepository = participacionRepository;
        this.dispositivoService = dispositivoService;
    }

    @Transactional
    public Prospecto recibirProspecto(String token, UUID eventoId, Prospecto datos) {
        Dispositivo dispositivo = dispositivoService.validarCredencial(token);

        Captador captador = dispositivo.getCaptador();
        Evento evento =
                eventoRepository
                        .buscarParaActualizar(eventoId)
                        .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (evento.isEliminado())
            throw new IllegalArgumentException("El evento no está disponible");
        boolean participo =
                participacionRepository.existsByCaptadorIdAndEventoId(captador.getId(), eventoId);

        if (!participo) {
            throw new IllegalArgumentException("El captador no pertenece a este evento");
        }
        if (datos.getId() == null) {
            throw new IllegalArgumentException("El UUID del prospecto es obligatorio");
        }
        var existente = prospectoRepository.findById(datos.getId());

        if (existente.isPresent()) {
            Prospecto anterior = existente.get();
            if (anterior.isEliminado())
                throw new ProspectoDuplicadoException(
                        "El registro fue eliminado por administración");

            if (!anterior.getEvento().getId().equals(eventoId)
                    || !anterior.getCaptador().getId().equals(captador.getId())) {
                throw new ProspectoDuplicadoException("UUID asociado a otro registro");
            }

            if (!mismosDatos(anterior, datos)) {
                throw new ProspectoDuplicadoException("El UUID ya existe con datos diferentes");
            }

            return anterior;
        }

        if (!evento.isActivo()
                && (evento.getCerradoEn() == null
                        || datos.getFechaCaptura() == null
                        || datos.getFechaCaptura() > evento.getCerradoEn().toEpochMilli()))
            throw new IllegalArgumentException("El evento está cerrado para nuevas capturas");
        ValidacionesServidor.prospecto(datos);
        if (datos.getRut() == null || datos.getRut().isBlank()) {
            throw new IllegalArgumentException("El RUT es obligatorio");
        }

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (datos.getApellidos() == null || datos.getApellidos().isBlank()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }
        String rutNormalizado = normalizarRut(datos.getRut());
        String emailNormalizado = normalizarEmail(datos.getEmail());

        datos.setRutNormalizado(rutNormalizado);
        datos.setEmailNormalizado(emailNormalizado);
        if (prospectoRepository.existsByEventoIdAndRutNormalizado(eventoId, rutNormalizado)) {

            throw new ProspectoDuplicadoException("RUT ya registrado en este evento");
        }

        if (emailNormalizado != null
                && prospectoRepository.existsByEventoIdAndEmailNormalizado(
                        eventoId, emailNormalizado)) {

            throw new ProspectoDuplicadoException("Correo ya registrado en este evento");
        }
        datos.setEvento(evento);
        datos.setCaptador(captador);
        return prospectoRepository.save(datos);
    }

    @Transactional
    public Prospecto recibirProspecto(String token, ProspectoSyncRequest request) {

        Prospecto prospecto = new Prospecto();
        prospecto.setId(request.id());
        prospecto.setRut(request.rut());
        prospecto.setNombre(request.nombre());
        prospecto.setApellidos(request.apellidos());
        prospecto.setFechaNacimiento(request.fechaNacimiento());
        prospecto.setCelular(request.celular());
        prospecto.setEmail(request.email());
        prospecto.setEstablecimiento(request.establecimiento());
        prospecto.setComunaResidencia(request.comunaResidencia());
        prospecto.setComunaEstablecimiento(request.comunaEstablecimiento());
        prospecto.setActividadActual(request.actividadActual());
        prospecto.setJornada(request.jornada());
        prospecto.setRegion(request.region());
        prospecto.setSede(request.sede());
        prospecto.setNivelInteres(request.nivelInteres());
        prospecto.setCarrera1(request.carrera1());
        prospecto.setCarrera2(request.carrera2());
        prospecto.setCarrera3(request.carrera3());
        prospecto.setModalidad(request.modalidad());
        prospecto.setCanalContacto(request.canalContacto());
        prospecto.setObservaciones(request.observaciones());
        prospecto.setAutorizaContacto(request.autorizaContacto());
        prospecto.setFechaCaptura(request.fechaCaptura());
        prospecto.setFotoRuta(request.fotoRuta());
        prospecto.setLatitud(request.latitud());
        prospecto.setLongitud(request.longitud());
        prospecto.setPrecisionMetros(request.precisionMetros());
        prospecto.setFechaUbicacion(request.fechaUbicacion());
        prospecto.setDireccion(request.direccion());
        return recibirProspecto(token, request.eventoId(), prospecto);
    }

    private String normalizarRut(String rut) {
        if (rut == null || rut.isBlank()) {
            return null;
        }

        return rut.replaceAll("[^0-9kK]", "").toUpperCase(Locale.ROOT);
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean mismosDatos(Prospecto anterior, Prospecto nuevo) {
        return java.util.Objects.equals(
                        normalizarRut(anterior.getRut()), normalizarRut(nuevo.getRut()))
                && java.util.Objects.equals(
                        normalizarEmail(anterior.getEmail()), normalizarEmail(nuevo.getEmail()))
                && java.util.Objects.equals(anterior.getNombre(), nuevo.getNombre())
                && java.util.Objects.equals(anterior.getApellidos(), nuevo.getApellidos())
                && java.util.Objects.equals(
                        anterior.getFechaNacimiento(), nuevo.getFechaNacimiento())
                && java.util.Objects.equals(anterior.getCelular(), nuevo.getCelular())
                && java.util.Objects.equals(
                        anterior.getEstablecimiento(), nuevo.getEstablecimiento())
                && java.util.Objects.equals(
                        anterior.getComunaResidencia(), nuevo.getComunaResidencia())
                && java.util.Objects.equals(
                        anterior.getComunaEstablecimiento(), nuevo.getComunaEstablecimiento())
                && java.util.Objects.equals(
                        anterior.getActividadActual(), nuevo.getActividadActual())
                && java.util.Objects.equals(anterior.getJornada(), nuevo.getJornada())
                && java.util.Objects.equals(anterior.getRegion(), nuevo.getRegion())
                && java.util.Objects.equals(anterior.getSede(), nuevo.getSede())
                && java.util.Objects.equals(anterior.getNivelInteres(), nuevo.getNivelInteres())
                && java.util.Objects.equals(anterior.getCarrera1(), nuevo.getCarrera1())
                && java.util.Objects.equals(anterior.getCarrera2(), nuevo.getCarrera2())
                && java.util.Objects.equals(anterior.getCarrera3(), nuevo.getCarrera3())
                && java.util.Objects.equals(anterior.getModalidad(), nuevo.getModalidad())
                && java.util.Objects.equals(anterior.getCanalContacto(), nuevo.getCanalContacto())
                && java.util.Objects.equals(anterior.getObservaciones(), nuevo.getObservaciones())
                && anterior.isAutorizaContacto() == nuevo.isAutorizaContacto()
                && java.util.Objects.equals(anterior.getFechaCaptura(), nuevo.getFechaCaptura())
                && java.util.Objects.equals(anterior.getFotoRuta(), nuevo.getFotoRuta())
                && java.util.Objects.equals(anterior.getLatitud(), nuevo.getLatitud())
                && java.util.Objects.equals(anterior.getLongitud(), nuevo.getLongitud())
                && java.util.Objects.equals(
                        anterior.getPrecisionMetros(), nuevo.getPrecisionMetros())
                && java.util.Objects.equals(anterior.getFechaUbicacion(), nuevo.getFechaUbicacion())
                && java.util.Objects.equals(anterior.getDireccion(), nuevo.getDireccion());
    }
}
