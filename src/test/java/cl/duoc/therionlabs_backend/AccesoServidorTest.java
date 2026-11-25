package cl.duoc.therionlabs_backend;

import static org.junit.jupiter.api.Assertions.*;

import cl.duoc.therionlabs_backend.repository.SesionAccesoRepository;
import cl.duoc.therionlabs_backend.service.ClavesAcceso;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccesoServidorTest {
    @LocalServerPort int puerto;
    @Autowired SesionAccesoRepository sesiones;
    private final HttpClient cliente = HttpClient.newHttpClient();
    private static final AtomicInteger NUMERO = new AtomicInteger(6000);

    @Test
    void autenticaYRevocaSinClavesAdministrativasManuales() throws Exception {
        assertEquals(
                401,
                llamar(
                                "POST",
                                "/api/acceso/ingresar",
                                "{\"usuario\":\"administrativo\",\"clave\":\"incorrecta\"}",
                                null,
                                null)
                        .statusCode());
        String admin = ingresar("administrativo", "AdminPruebas2026!");
        assertEquals(200, llamar("GET", "/api/admin/cuentas", null, admin, null).statusCode());
        assertEquals(204, llamar("POST", "/api/acceso/salir", null, admin, null).statusCode());
        assertEquals(401, llamar("GET", "/api/admin/cuentas", null, admin, null).statusCode());
    }

    @Test
    void asignaCaptadorYEvitaQueObtengaPermisosAdministrativos() throws Exception {
        String admin = ingresar("administrativo", "AdminPruebas2026!");
        String usuario = "captador" + NUMERO.incrementAndGet();
        String cuenta = crearCaptador(admin, usuario);
        String token = ingresar(usuario, "CaptadorPruebas!");
        assertEquals(403, llamar("GET", "/api/admin/cuentas", null, token, null).statusCode());
        assertEquals(403, llamar("POST", "/api/eventos", evento(), token, null).statusCode());
        var dispositivo = llamar("GET", "/api/dispositivos/verificar", null, null, token);
        assertEquals(200, dispositivo.statusCode());
        assertEquals(campo(cuenta, "captadorId"), campo(dispositivo.body(), "captadorId"));
        assertEquals(
                400,
                llamar(
                                "POST",
                                "/api/admin/cuentas",
                                "{\"usuario\":\"administradorfalso\",\"clave\":\"ClaveLarga!\",\"nombre\":\"Usuario"
                                    + " inválido\",\"rol\":\"SUPERUSUARIO\"}",
                                admin,
                                null)
                        .statusCode());
    }

    @Test
    void lasSesionesDeDosDispositivosSonIndependientesYVencen() throws Exception {
        String primero = ingresar("administrativo", "AdminPruebas2026!");
        String segundo = ingresar("administrativo", "AdminPruebas2026!");
        assertNotEquals(primero, segundo);
        var vencida = sesiones.findByResumen(ClavesAcceso.resumen(primero)).orElseThrow();
        vencida.setVence(Instant.now().minusSeconds(1));
        sesiones.save(vencida);
        assertEquals(401, llamar("GET", "/api/admin/cuentas", null, primero, null).statusCode());
        assertEquals(200, llamar("GET", "/api/admin/cuentas", null, segundo, null).statusCode());
    }

    @Test
    void cerrarConservaElHistorialYAceptaCapturasAnterioresPendientes() throws Exception {
        String admin = ingresar("administrativo", "AdminPruebas2026!");
        String usuario = "captador" + NUMERO.incrementAndGet();
        String cuenta = crearCaptador(admin, usuario);
        String token = ingresar(usuario, "CaptadorPruebas!");
        var respuesta = llamar("POST", "/api/eventos", evento(), admin, null);
        assertEquals(201, respuesta.statusCode());
        String evento = campo(respuesta.body(), "id");
        long capturada = System.currentTimeMillis() - 1000;
        assertEquals(
                200,
                llamar("POST", "/api/eventos/" + evento + "/cerrar", null, admin, null)
                        .statusCode());
        assertEquals(
                200,
                llamar("POST", "/api/eventos/" + evento + "/cerrar", null, admin, null)
                        .statusCode());
        assertEquals(
                201,
                llamar(
                                "POST",
                                "/api/participaciones/unirse?captadorId="
                                        + campo(cuenta, "captadorId")
                                        + "&eventoId="
                                        + evento,
                                null,
                                null,
                                token)
                        .statusCode());
        String id = UUID.randomUUID().toString();
        String ficha = ficha(id, evento, capturada);
        assertEquals(200, llamar("POST", "/api/prospectos/sync", ficha, null, token).statusCode());
        assertEquals(200, llamar("POST", "/api/prospectos/sync", ficha, null, token).statusCode());
        assertEquals(
                400,
                llamar(
                                "POST",
                                "/api/prospectos/sync",
                                ficha(
                                        UUID.randomUUID().toString(),
                                        evento,
                                        System.currentTimeMillis() + 10000),
                                null,
                                token)
                        .statusCode());
        assertTrue(
                llamar("GET", "/api/admin/eventos/" + evento + "/prospectos", null, admin, null)
                        .body()
                        .contains(id));
        assertFalse(llamar("GET", "/api/eventos", null, null, null).body().contains(evento));
        assertTrue(
                llamar("GET", "/api/eventos?incluirEliminados=true", null, null, null)
                        .body()
                        .contains(evento));
        assertEquals(
                409, llamar("DELETE", "/api/eventos/" + evento, null, admin, null).statusCode());
    }

    private String ingresar(String usuario, String clave) throws Exception {
        var respuesta =
                llamar(
                        "POST",
                        "/api/acceso/ingresar",
                        "{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}",
                        null,
                        null);
        assertEquals(200, respuesta.statusCode(), respuesta.body());
        return campo(respuesta.body(), "token");
    }

    private String crearCaptador(String token, String usuario) throws Exception {
        var r =
                llamar(
                        "POST",
                        "/api/admin/cuentas",
                        "{\"usuario\":\""
                                + usuario
                                + "\",\"clave\":\"CaptadorPruebas!\",\"nombre\":\"Captador de"
                                + " pruebas\",\"rol\":\"CAPTADOR\",\"codigoCaptador\":\"CAP-"
                                + NUMERO.incrementAndGet()
                                + "\"}",
                        token,
                        null);
        assertEquals(201, r.statusCode(), r.body());
        assertFalse(r.body().contains("CaptadorPruebas!"));
        return r.body();
    }

    private String evento() {
        return "{\"nombre\":\"Evento con acceso\",\"colegio\":\"Colegio de"
                   + " prueba\",\"fecha\":\"2026-12-10\",\"actividad\":\"Orientación"
                   + " académica\",\"comuna\":\"Quilpué\",\"activo\":true}";
    }

    private String ficha(String id, String evento, long fecha) {
        return """
        {"id":"%s","eventoId":"%s","rut":"12.345.678-5","nombre":"Persona","apellidos":"De Prueba",
        "fechaNacimiento":"01/01/2005","celular":"+56912345678","email":"persona@example.cl",
        "establecimiento":"Colegio de prueba","comunaResidencia":"Quilpué","comunaEstablecimiento":"Quilpué",
        "actividadActual":"4to Medio","jornada":"Diurna","region":"Región de Valparaíso","sede":"Valparaíso",
        "nivelInteres":"Interés Alto","carrera1":"Analista Programador","modalidad":"Presencial",
        "canalContacto":"Correo electrónico","autorizaContacto":true,"fechaCaptura":%d}
        """
                .formatted(id, evento, fecha);
    }

    private String campo(String cuerpo, String nombre) {
        var m = Pattern.compile("\"" + nombre + "\"\\s*:\\s*\"([^\"]+)\"").matcher(cuerpo);
        assertTrue(m.find(), cuerpo);
        return m.group(1);
    }

    private HttpResponse<String> llamar(
            String metodo, String ruta, String cuerpo, String token, String dispositivo)
            throws Exception {
        var r =
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                        .header("Content-Type", "application/json");
        if (token != null) r.header("Authorization", "Bearer " + token);
        if (dispositivo != null) r.header("X-Device-Token", dispositivo);
        return cliente.send(
                r.method(
                                metodo,
                                cuerpo == null
                                        ? HttpRequest.BodyPublishers.noBody()
                                        : HttpRequest.BodyPublishers.ofString(cuerpo))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
