package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.Evento;
import cl.duoc.therionlabs_backend.repository.EventoRepository;
import cl.duoc.therionlabs_backend.repository.ProspectoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EventoService {
    private final EventoRepository eventos;
    private final ProspectoRepository prospectos;

    public EventoService(EventoRepository eventos, ProspectoRepository prospectos) {
        this.eventos = eventos;
        this.prospectos = prospectos;
    }

    @Transactional(readOnly = true)
    public List<Evento> listarEventosActivos() {
        return eventos.findByActivoTrueAndEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<Evento> listarCambios() {
        return eventos.findAllByOrderByFechaAsc();
    }

    @Transactional(readOnly = true)
    public Evento buscarPorId(UUID id) {
        return eventos.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    public Evento crearEvento(Evento evento) {
        validar(evento);
        if (evento.getId() == null) evento.setId(UUID.randomUUID());
        var anterior = eventos.findById(evento.getId());
        if (anterior.isPresent()) {
            Evento existente = anterior.get();
            if (existente.isEliminado())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Evento eliminado");
            if (!existente.getNombre().equals(evento.getNombre())
                    || !existente.getColegio().equals(evento.getColegio())
                    || !existente.getFecha().equals(evento.getFecha())
                    || !existente.getActividad().equals(evento.getActividad())
                    || !existente.getComuna().equals(evento.getComuna())
                    || existente.isActivo() != evento.isActivo())
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "El identificador ya pertenece a otro evento");
            return existente;
        }
        evento.setEliminado(false);
        return eventos.save(evento);
    }

    public Evento actualizar(UUID id, Evento datos) {
        validar(datos);
        Evento evento =
                eventos.buscarParaActualizar(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (evento.isEliminado())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Evento eliminado");
        evento.setNombre(datos.getNombre().trim());
        evento.setColegio(datos.getColegio().trim());
        evento.setFecha(datos.getFecha());
        evento.setActividad(datos.getActividad().trim());
        evento.setComuna(datos.getComuna());
        if (datos.isActivo() != evento.isActivo())
            throw new IllegalArgumentException(
                    "Utiliza la opción Cerrar evento para terminar la captación");
        return eventos.save(evento);
    }

    public Evento cerrar(UUID id) {
        Evento evento =
                eventos.buscarParaActualizar(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (evento.isEliminado())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Evento eliminado");
        if (evento.isActivo()) {
            evento.setActivo(false);
            evento.setCerradoEn(java.time.Instant.now());
        }
        return eventos.save(evento);
    }

    public void eliminar(UUID id) {
        Evento evento =
                eventos.buscarParaActualizar(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (prospectos.existsByEventoId(id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El evento tiene prospectos registrados");
        evento.setActivo(false);
        evento.setEliminado(true);
        eventos.save(evento);
    }

    private void validar(Evento evento) {
        ValidacionesServidor.texto(evento.getNombre(), 3, 100, "Nombre del evento");
        ValidacionesServidor.texto(evento.getColegio(), 3, 150, "Institución");
        ValidacionesServidor.texto(evento.getActividad(), 3, 300, "Descripción");
        if (evento.getFecha() == null)
            throw new IllegalArgumentException("La fecha es obligatoria");
        if (!CatalogoRegional.comunas.contains(evento.getComuna()))
            throw new IllegalArgumentException("Selecciona una comuna de Valparaíso");
    }
}
