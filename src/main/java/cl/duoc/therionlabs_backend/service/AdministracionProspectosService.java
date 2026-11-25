package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.dto.*;
import cl.duoc.therionlabs_backend.entity.*;
import cl.duoc.therionlabs_backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AdministracionProspectosService {
    private final ProspectoRepository prospectos;
    private final EventoRepository eventos;
    private final SeguimientoRepository seguimientos;

    public AdministracionProspectosService(
            ProspectoRepository prospectos,
            EventoRepository eventos,
            SeguimientoRepository seguimientos) {
        this.prospectos = prospectos;
        this.eventos = eventos;
        this.seguimientos = seguimientos;
    }

    @Transactional(readOnly = true)
    public List<ProspectoRespuesta> listar(UUID eventoId, boolean incluirEliminados) {
        if (!eventos.existsById(eventoId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        return (incluirEliminados
                        ? prospectos.findByEventoId(eventoId)
                        : prospectos.findByEventoIdAndEliminadoFalseOrderByFechaCapturaDesc(
                                eventoId))
                .stream().map(ProspectoRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProspectoRespuesta consultar(UUID id) {
        return ProspectoRespuesta.desde(buscar(id));
    }

    public ProspectoRespuesta actualizar(UUID id, ProspectoSyncRequest datos) {
        Prospecto anterior = bloquear(id);
        if (anterior.isEliminado())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El prospecto fue eliminado");
        if (!id.equals(datos.id()) || !anterior.getEvento().getId().equals(datos.eventoId()))
            throw new IllegalArgumentException(
                    "No se puede cambiar el identificador o evento de la ficha");
        anterior.setRut(datos.rut());
        anterior.setNombre(datos.nombre());
        anterior.setApellidos(datos.apellidos());
        anterior.setFechaNacimiento(datos.fechaNacimiento());
        anterior.setCelular(datos.celular());
        anterior.setEmail(datos.email());
        anterior.setEstablecimiento(datos.establecimiento());
        anterior.setComunaResidencia(datos.comunaResidencia());
        anterior.setComunaEstablecimiento(datos.comunaEstablecimiento());
        anterior.setActividadActual(datos.actividadActual());
        anterior.setJornada(datos.jornada());
        anterior.setRegion(datos.region());
        anterior.setSede(datos.sede());
        anterior.setNivelInteres(datos.nivelInteres());
        anterior.setCarrera1(datos.carrera1());
        anterior.setCarrera2(datos.carrera2());
        anterior.setCarrera3(datos.carrera3());
        anterior.setModalidad(datos.modalidad());
        anterior.setCanalContacto(datos.canalContacto());
        anterior.setObservaciones(datos.observaciones());
        anterior.setAutorizaContacto(datos.autorizaContacto());
        anterior.setFechaCaptura(datos.fechaCaptura());
        anterior.setFotoRuta(datos.fotoRuta());
        anterior.setLatitud(datos.latitud());
        anterior.setLongitud(datos.longitud());
        anterior.setPrecisionMetros(datos.precisionMetros());
        anterior.setFechaUbicacion(datos.fechaUbicacion());
        anterior.setDireccion(datos.direccion());
        ValidacionesServidor.prospecto(anterior);
        String rut = ValidacionesServidor.rut(anterior.getRut());
        String correo = anterior.getEmail().trim().toLowerCase(Locale.ROOT);
        UUID eventoId = anterior.getEvento().getId();
        if (prospectos.existsByEventoIdAndRutNormalizadoAndIdNot(eventoId, rut, id)
                || prospectos.existsByEventoIdAndEmailNormalizadoAndIdNot(eventoId, correo, id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "RUT o correo repetido dentro del evento");
        anterior.setRutNormalizado(rut);
        anterior.setEmailNormalizado(correo);
        return ProspectoRespuesta.desde(prospectos.save(anterior));
    }

    public void eliminar(UUID id) {
        Prospecto prospecto = bloquear(id);
        if (seguimientos.existsByProspectoId(id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La ficha tiene gestiones y debe conservarse");
        prospecto.setEliminado(true);
        prospectos.save(prospecto);
    }

    @Transactional(readOnly = true)
    public List<SeguimientoRespuesta> gestiones(UUID id) {
        buscar(id);
        return seguimientos.findByProspectoIdOrderByFechaDescIdDesc(id).stream()
                .map(SeguimientoRespuesta::desde)
                .toList();
    }

    public SeguimientoRespuesta registrarGestion(UUID id, GestionSolicitud datos) {
        Prospecto prospecto = bloquear(id);
        if (prospecto.isEliminado())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El prospecto fue eliminado");
        ValidacionesServidor.texto(datos.responsable(), 2, 100, "Responsable");
        ValidacionesServidor.texto(datos.observacion(), 5, 500, "Observación");
        if (!Set.of("SIN_CONTACTAR", "EN_SEGUIMIENTO", "CONTACTADO", "CERRADO")
                        .contains(Objects.toString(datos.estado(), ""))
                || !Set.of("Teléfono", "Correo", "Otro")
                        .contains(Objects.toString(datos.canal(), "")))
            throw new IllegalArgumentException("Estado o canal inválido");
        if (!prospecto.isAutorizaContacto() && !"Otro".equals(datos.canal()))
            throw new IllegalArgumentException("El prospecto no autorizó contacto");
        UUID identificador = datos.id() == null ? UUID.randomUUID() : datos.id();
        var anterior = seguimientos.findById(identificador);
        if (anterior.isPresent()) {
            Seguimiento gestion = anterior.get();
            if (!gestion.getProspecto().getId().equals(id)
                    || !gestion.getEstado().equals(datos.estado())
                    || !gestion.getCanal().equals(datos.canal())
                    || !gestion.getObservacion().equals(datos.observacion().trim())
                    || !gestion.getResponsable().equals(datos.responsable().trim()))
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "El identificador pertenece a otra gestión");
            return SeguimientoRespuesta.desde(gestion);
        }
        Seguimiento gestion = new Seguimiento();
        gestion.setId(identificador);
        gestion.setProspecto(prospecto);
        gestion.setResponsable(datos.responsable().trim());
        gestion.setEstado(datos.estado());
        gestion.setCanal(datos.canal());
        gestion.setObservacion(datos.observacion().trim());
        gestion.setFecha(Instant.now());
        return SeguimientoRespuesta.desde(seguimientos.save(gestion));
    }

    private Prospecto bloquear(UUID id) {
        return prospectos
                .buscarParaActualizar(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Prospecto no encontrado"));
    }

    private Prospecto buscar(UUID id) {
        return prospectos
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Prospecto no encontrado"));
    }
}
