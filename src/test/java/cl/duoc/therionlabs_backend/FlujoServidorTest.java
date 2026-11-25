package cl.duoc.therionlabs_backend;

import static org.junit.jupiter.api.Assertions.*;

import cl.duoc.therionlabs_backend.service.CaptadorAdminService;
import cl.duoc.therionlabs_backend.service.DispositivoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlujoServidorTest {
    private static final java.util.concurrent.atomic.AtomicInteger CODIGO =
            new java.util.concurrent.atomic.AtomicInteger(1000);
    private static final String CLAVE = "clave-exclusiva-pruebas";
    @LocalServerPort int puerto;
    @Autowired CaptadorAdminService captadores;
    @Autowired DispositivoService dispositivos;
    private final HttpClient cliente = HttpClient.newHttpClient();

    @Test
    void exigeCredencialesEnLasOperacionesProtegidas() throws Exception {
        assertEquals(403, peticion("POST", "/api/eventos", evento(), null, null).statusCode());
        assertEquals(
                403,
                peticion(
                                "GET",
                                "/api/admin/eventos/" + UUID.randomUUID() + "/prospectos",
                                null,
                                null,
                                null)
                        .statusCode());
        assertEquals(
                403,
                peticion("GET", "/api/admin/captadores", null, "incorrecta", null).statusCode());
        assertEquals(200, peticion("GET", "/api/eventos", null, null, null).statusCode());
    }

    @Test
    void elEventoSeEditaYLaBajaSePuedeDescargar() throws Exception {
        String id = crearEvento();
        assertEquals(
                200,
                peticion(
                                "PUT",
                                "/api/eventos/" + id,
                                evento().replace("Feria vocacional", "Feria actualizada"),
                                CLAVE,
                                null)
                        .statusCode());
        assertTrue(
                peticion("GET", "/api/eventos/" + id, null, null, null)
                        .body()
                        .contains("Feria actualizada"));
        assertEquals(204, peticion("DELETE", "/api/eventos/" + id, null, CLAVE, null).statusCode());
        assertEquals(204, peticion("DELETE", "/api/eventos/" + id, null, CLAVE, null).statusCode());
        assertFalse(peticion("GET", "/api/eventos", null, null, null).body().contains(id));
        assertTrue(
                peticion("GET", "/api/eventos?incluirEliminados=true", null, null, null)
                        .body()
                        .contains(id));
    }

    @Test
    void sincronizaSinDuplicarYConservaLasGestionesEntreClientes() throws Exception {
        String evento = crearEvento();
        var captador =
                captadores.registrarCaptador(
                        "CAP-" + CODIGO.incrementAndGet(), "Captador de prueba");
        String token = dispositivos.registrarDispositivo(captador.getId()).credencial();
        assertEquals(
                201,
                peticion(
                                "POST",
                                "/api/participaciones/unirse?captadorId="
                                        + captador.getId()
                                        + "&eventoId="
                                        + evento,
                                null,
                                null,
                                token)
                        .statusCode());
        String id = UUID.randomUUID().toString();
        String ficha = ficha(id, evento);
        assertEquals(
                200, peticion("POST", "/api/prospectos/sync", ficha, null, token).statusCode());
        assertEquals(
                200, peticion("POST", "/api/prospectos/sync", ficha, null, token).statusCode());
        var lista =
                peticion("GET", "/api/admin/eventos/" + evento + "/prospectos", null, CLAVE, null);
        assertEquals(200, lista.statusCode());
        assertTrue(lista.body().contains(id));
        assertTrue(lista.body().contains(captador.getIdentificadorLocal()));
        assertEquals(
                409,
                peticion(
                                "POST",
                                "/api/prospectos/sync",
                                ficha(UUID.randomUUID().toString(), evento),
                                null,
                                token)
                        .statusCode());
        assertEquals(
                409, peticion("DELETE", "/api/eventos/" + evento, null, CLAVE, null).statusCode());
        String editada =
                ficha.replace("\"nombre\":\"Persona\"", "\"nombre\":\"Persona corregida\"");
        assertEquals(
                200,
                peticion("PUT", "/api/admin/prospectos/" + id, editada, CLAVE, null).statusCode());
        assertTrue(
                peticion("GET", "/api/admin/prospectos/" + id, null, CLAVE, null)
                        .body()
                        .contains("Persona corregida"));
        assertEquals(
                409, peticion("POST", "/api/prospectos/sync", ficha, null, token).statusCode());
        String gestion =
                """
                {"id":"%s","responsable":"Administración","estado":"EN_SEGUIMIENTO","canal":"Correo","observacion":"Se entregó información académica"}
                """
                        .formatted(UUID.randomUUID());
        String ruta = "/api/admin/prospectos/" + id + "/seguimientos";
        assertEquals(201, peticion("POST", ruta, gestion, CLAVE, null).statusCode());
        assertEquals(201, peticion("POST", ruta, gestion, CLAVE, null).statusCode());
        var historial = peticion("GET", ruta, null, CLAVE, null);
        assertEquals(200, historial.statusCode());
        assertEquals(1, historial.body().split("Se entregó información académica", -1).length - 1);
        assertEquals(
                409,
                peticion("DELETE", "/api/admin/prospectos/" + id, null, CLAVE, null).statusCode());
        var excel =
                peticion("GET", "/api/admin/prospectos/" + evento + "/excel", null, CLAVE, null);
        assertEquals(200, excel.statusCode());
        assertTrue(excel.headers().firstValue("Content-Type").orElse("").contains("spreadsheetml"));
    }

    @Test
    void noPermiteSuplantarAlCaptadorNiAceptarDatosInvalidos() throws Exception {
        String evento = crearEvento();
        var captador =
                captadores.registrarCaptador("CAP-" + CODIGO.incrementAndGet(), "Otro captador");
        String token = dispositivos.registrarDispositivo(captador.getId()).credencial();
        assertEquals(
                403,
                peticion(
                                "POST",
                                "/api/participaciones/unirse?captadorId="
                                        + UUID.randomUUID()
                                        + "&eventoId="
                                        + evento,
                                null,
                                null,
                                token)
                        .statusCode());
        assertEquals(
                201,
                peticion(
                                "POST",
                                "/api/participaciones/unirse?captadorId="
                                        + captador.getId()
                                        + "&eventoId="
                                        + evento,
                                null,
                                null,
                                token)
                        .statusCode());
        String ficha =
                ficha(UUID.randomUUID().toString(), evento).replace("12.345.678-5", "12.345.678-4");
        assertEquals(
                400, peticion("POST", "/api/prospectos/sync", ficha, null, token).statusCode());
        String sinAutorizacion =
                ficha(UUID.randomUUID().toString(), evento)
                        .replace("\"autorizaContacto\":true", "\"autorizaContacto\":false");
        var respuesta = peticion("POST", "/api/prospectos/sync", sinAutorizacion, null, token);
        assertEquals(200, respuesta.statusCode());
        String id = campo(respuesta.body(), "prospectoId");
        assertEquals(
                400,
                peticion(
                                "POST",
                                "/api/admin/prospectos/" + id + "/seguimientos",
                                "{\"responsable\":\"Administración\",\"estado\":\"CONTACTADO\",\"canal\":\"Correo\",\"observacion\":\"Se"
                                    + " realizó una gestión de prueba\"}",
                                CLAVE,
                                null)
                        .statusCode());
        assertEquals(
                204,
                peticion("DELETE", "/api/admin/prospectos/" + id, null, CLAVE, null).statusCode());
        assertEquals(
                409,
                peticion("POST", "/api/prospectos/sync", sinAutorizacion, null, token)
                        .statusCode());
        assertEquals(
                "[]",
                peticion("GET", "/api/admin/eventos/" + evento + "/prospectos", null, CLAVE, null)
                        .body());
        assertTrue(
                peticion(
                                "GET",
                                "/api/admin/eventos/"
                                        + evento
                                        + "/prospectos?incluirEliminados=true",
                                null,
                                CLAVE,
                                null)
                        .body()
                        .contains(id));
    }

    private String crearEvento() throws Exception {
        var respuesta = peticion("POST", "/api/eventos", evento(), CLAVE, null);
        assertEquals(201, respuesta.statusCode(), respuesta.body());
        return campo(respuesta.body(), "id");
    }

    private String evento() {
        return "{\"nombre\":\"Feria vocacional\",\"colegio\":\"Colegio de"
                + " prueba\",\"fecha\":\"2026-12-10\",\"actividad\":\"Orientación"
                + " académica\",\"comuna\":\"Quilpué\",\"activo\":true}";
    }

    private String ficha(String id, String evento) {
        return """
        {"id":"%s","eventoId":"%s","rut":"12.345.678-5","nombre":"Persona","apellidos":"De Prueba",
        "fechaNacimiento":"01/01/2005","celular":"+56912345678","email":"persona@example.cl",
        "establecimiento":"Colegio de prueba","comunaResidencia":"Quilpué","comunaEstablecimiento":"Quilpué",
        "actividadActual":"4to Medio","jornada":"Diurna","region":"Región de Valparaíso","sede":"Valparaíso",
        "nivelInteres":"Interés Alto","carrera1":"Analista Programador","modalidad":"Presencial",
        "canalContacto":"Correo electrónico","autorizaContacto":true,"fechaCaptura":1790000000000}
        """
                .formatted(id, evento);
    }

    private String campo(String json, String nombre) {
        var resultado = Pattern.compile("\"" + nombre + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        assertTrue(resultado.find(), json);
        return resultado.group(1);
    }

    private HttpResponse<String> peticion(
            String metodo, String ruta, String cuerpo, String clave, String token)
            throws Exception {
        var solicitud =
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                        .header("Content-Type", "application/json");
        if (clave != null) solicitud.header("X-Admin-Key", clave);
        if (token != null) solicitud.header("X-Device-Token", token);
        return cliente.send(
                solicitud
                        .method(
                                metodo,
                                cuerpo == null
                                        ? HttpRequest.BodyPublishers.noBody()
                                        : HttpRequest.BodyPublishers.ofString(cuerpo))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
