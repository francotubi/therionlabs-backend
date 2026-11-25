package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.*;
import cl.duoc.therionlabs_backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class AccesoService {
    private final CuentaRepository cuentas;
    private final SesionAccesoRepository sesiones;
    private final CaptadorRepository captadores;
    private final CaptadorAdminService administracion;

    public AccesoService(
            CuentaRepository cuentas,
            SesionAccesoRepository sesiones,
            CaptadorRepository captadores,
            CaptadorAdminService administracion) {
        this.cuentas = cuentas;
        this.sesiones = sesiones;
        this.captadores = captadores;
        this.administracion = administracion;
    }

    public CuentaResumen crear(CrearCuenta datos) {
        String usuario = normalizar(datos.usuario());
        if (!usuario.matches("[a-z0-9][a-z0-9._-]{2,39}"))
            throw new IllegalArgumentException(
                    "Usa un usuario de 3 a 40 letras, números, puntos o guiones");
        if (datos.clave() == null || datos.clave().length() < 8 || datos.clave().length() > 128)
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres");
        ValidacionesServidor.texto(datos.nombre(), 3, 100, "Nombre");
        if (!Set.of("ADMINISTRATIVO", "CAPTADOR").contains(Objects.toString(datos.rol(), "")))
            throw new IllegalArgumentException("Perfil no válido");
        if (cuentas.findByUsuario(usuario).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        Cuenta cuenta = new Cuenta();
        cuenta.setId(UUID.randomUUID());
        cuenta.setUsuario(usuario);
        cuenta.setNombre(datos.nombre().trim());
        cuenta.setRol(datos.rol());
        cuenta.setSal(ClavesAcceso.aleatoria());
        cuenta.setResumenClave(ClavesAcceso.derivar(datos.clave(), cuenta.getSal()));
        if ("CAPTADOR".equals(datos.rol())) {
            String codigo =
                    Objects.toString(datos.codigoCaptador(), "").trim().toUpperCase(Locale.ROOT);
            if (!codigo.matches("CAP-[0-9]{2,}"))
                throw new IllegalArgumentException("El identificador debe tener formato CAP-03");
            Captador captador = captadores.findByIdentificadorLocal(codigo).orElse(null);
            if (captador == null)
                captador = administracion.registrarCaptador(codigo, datos.nombre().trim());
            if (!captador.isActivo() || cuentas.existsByCaptadorId(captador.getId()))
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El captador ya tiene una cuenta o está deshabilitado");
            cuenta.setCaptador(captador);
        }
        return CuentaResumen.desde(cuentas.save(cuenta));
    }

    public AccesoRespuesta ingresar(String usuario, String clave) {
        if (clave == null || clave.length() > 128) throw incorrecta();
        Cuenta cuenta = cuentas.findByUsuario(normalizar(usuario)).orElse(null);
        if (cuenta == null
                || !ClavesAcceso.coincide(clave, cuenta.getSal(), cuenta.getResumenClave())
                || !habilitada(cuenta)) throw incorrecta();
        sesiones.deleteByVenceBefore(Instant.now());
        String token = ClavesAcceso.aleatoria();
        SesionAcceso sesion = new SesionAcceso();
        sesion.setId(UUID.randomUUID());
        sesion.setCuenta(cuenta);
        sesion.setResumen(ClavesAcceso.resumen(token));
        sesion.setVence(Instant.now().plus(30, ChronoUnit.DAYS));
        sesiones.save(sesion);
        String tokenCaptura = null;
        if ("CAPTADOR".equals(cuenta.getRol())) {
            tokenCaptura = ClavesAcceso.aleatoria();
            SesionAcceso captura = new SesionAcceso();
            captura.setId(UUID.randomUUID());
            captura.setCuenta(cuenta);
            captura.setResumen(ClavesAcceso.resumen(tokenCaptura));
            captura.setVence(sesion.getVence());
            sesiones.save(captura);
        }
        return new AccesoRespuesta(
                CuentaResumen.desde(cuenta), token, sesion.getVence().toEpochMilli(), tokenCaptura);
    }

    @Transactional(readOnly = true, noRollbackFor = SecurityException.class)
    public SesionAcceso validar(String token) {
        if (token == null || token.length() > 200 || token.isBlank())
            throw new SecurityException("Sesión no válida");
        SesionAcceso sesion =
                sesiones.findByResumen(ClavesAcceso.resumen(token))
                        .orElseThrow(() -> new SecurityException("Sesión no válida"));
        if (!sesion.getVence().isAfter(Instant.now()) || !habilitada(sesion.getCuenta()))
            throw new SecurityException("La sesión venció. Vuelve a ingresar");
        return sesion;
    }

    public void cerrar(String token) {
        sesiones.findByResumen(ClavesAcceso.resumen(token)).ifPresent(sesiones::delete);
    }

    @Transactional(readOnly = true)
    public List<CuentaResumen> listar() {
        return cuentas.findAll().stream()
                .sorted(Comparator.comparing(Cuenta::getUsuario))
                .map(CuentaResumen::desde)
                .toList();
    }

    private boolean habilitada(Cuenta cuenta) {
        return cuenta.isActivo()
                && (cuenta.getCaptador() == null || cuenta.getCaptador().isActivo());
    }

    private String normalizar(String nombre) {
        return Objects.toString(nombre, "").trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException incorrecta() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
    }

    public record CrearCuenta(
            String usuario, String clave, String nombre, String rol, String codigoCaptador) {}

    public record AccesoRespuesta(
            CuentaResumen cuenta, String token, long vence, String tokenCaptura) {}

    public record CuentaResumen(
            UUID id,
            String usuario,
            String nombre,
            String rol,
            UUID captadorId,
            String codigoCaptador,
            boolean activo) {
        public static CuentaResumen desde(Cuenta c) {
            return new CuentaResumen(
                    c.getId(),
                    c.getUsuario(),
                    c.getNombre(),
                    c.getRol(),
                    c.getCaptador() == null ? null : c.getCaptador().getId(),
                    c.getCaptador() == null ? null : c.getCaptador().getIdentificadorLocal(),
                    c.isActivo());
        }
    }
}
