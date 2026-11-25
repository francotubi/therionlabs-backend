package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.repository.CaptadorRepository;
import cl.duoc.therionlabs_backend.service.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/captadores")
public class CaptadorAdminController {
    private final CaptadorRepository captadores;
    private final CaptadorAdminService servicio;
    private final DispositivoService dispositivos;

    public CaptadorAdminController(
            CaptadorRepository captadores,
            CaptadorAdminService servicio,
            DispositivoService dispositivos) {
        this.captadores = captadores;
        this.servicio = servicio;
        this.dispositivos = dispositivos;
    }

    @GetMapping
    public List<CaptadorRespuesta> listarCaptadores() {
        return captadores.findAll().stream()
                .sorted(Comparator.comparing(c -> c.getIdentificadorLocal()))
                .map(c ->
                        new CaptadorRespuesta(
                                c.getId(), c.getIdentificadorLocal(), c.getNombre(), c.isActivo()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CaptadorRespuesta registrar(@RequestBody RegistroCaptadorRequest datos) {
        var c = servicio.registrarCaptador(datos.identificadorLocal(), datos.nombre());
        return new CaptadorRespuesta(
                c.getId(), c.getIdentificadorLocal(), c.getNombre(), c.isActivo());
    }

    @PostMapping("/{captadorId}/dispositivo")
    @ResponseStatus(HttpStatus.CREATED)
    public DispositivoService.CredencialActivacion generarToken(@PathVariable UUID captadorId) {
        return dispositivos.registrarDispositivo(captadorId);
    }

    @PostMapping("/{captadorId}/renovar-credencial")
    public DispositivoService.CredencialActivacion renovarCredencial(
            @PathVariable UUID captadorId) {
        return dispositivos.renovarCredencial(captadorId);
    }

    public record RegistroCaptadorRequest(String identificadorLocal, String nombre) {}

    public record CaptadorRespuesta(
            UUID id, String identificadorLocal, String nombre, boolean activo) {}
}
