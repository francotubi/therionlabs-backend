package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.service.AccesoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/acceso")
public class AccesoController {
    private final AccesoService acceso;
    private final ConcurrentHashMap<String, Intentos> intentos = new ConcurrentHashMap<>();

    public AccesoController(AccesoService acceso) {
        this.acceso = acceso;
    }

    @PostMapping("/ingresar")
    public AccesoService.AccesoRespuesta ingresar(
            @RequestBody Solicitud datos, HttpServletRequest solicitud) {
        long ahora = Instant.now().getEpochSecond();
        intentos.entrySet().removeIf(e -> ahora - e.getValue().inicio() >= 60);
        Intentos actual =
                intentos.compute(
                        solicitud.getRemoteAddr(),
                        (ip, anterior) ->
                                anterior == null
                                        ? new Intentos(ahora, 1)
                                        : new Intentos(anterior.inicio(), anterior.total() + 1));
        if (actual.total() > 20)
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos. Espera un minuto");
        return acceso.ingresar(datos.usuario(), datos.clave());
    }

    @PostMapping("/salir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(@RequestHeader("Authorization") String autorizacion) {
        if (!autorizacion.startsWith("Bearer ")) throw new SecurityException();
        acceso.cerrar(autorizacion.substring(7));
    }

    public record Solicitud(String usuario, String clave) {}

    private record Intentos(long inicio, int total) {}
}
