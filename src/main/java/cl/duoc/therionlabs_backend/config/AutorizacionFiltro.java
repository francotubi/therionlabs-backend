package cl.duoc.therionlabs_backend.config;

import cl.duoc.therionlabs_backend.service.AccesoService;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class AutorizacionFiltro extends OncePerRequestFilter {
    private final byte[] clave;
    private final AccesoService acceso;

    public AutorizacionFiltro(
            @Value("${THERIONLABS_EXPORT_KEY:}") String clave, AccesoService acceso) {
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
        this.acceso = acceso;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String ruta = solicitud.getRequestURI().substring(solicitud.getContextPath().length());
        boolean admin =
                ruta.startsWith("/api/admin/")
                        || ((ruta.equals("/api/eventos") || ruta.startsWith("/api/eventos/"))
                                && !solicitud.getMethod().equals("GET")
                                && !solicitud.getMethod().equals("HEAD"));
        if (admin) {
            String autorizacion = solicitud.getHeader("Authorization");
            if (autorizacion != null) {
                try {
                    if (!autorizacion.startsWith("Bearer ")) throw new SecurityException();
                    if (!"ADMINISTRATIVO"
                            .equals(
                                    acceso.validar(autorizacion.substring(7))
                                            .getCuenta()
                                            .getRol())) {
                        rechazar(respuesta, 403, "Esta función requiere un perfil administrativo");
                        return;
                    }
                } catch (SecurityException error) {
                    rechazar(respuesta, 401, "La sesión venció. Vuelve a ingresar");
                    return;
                }
            } else {
                String recibida = solicitud.getHeader("X-Admin-Key");
                if (clave.length == 0
                        || recibida == null
                        || !MessageDigest.isEqual(
                                clave, recibida.getBytes(StandardCharsets.UTF_8))) {
                    rechazar(respuesta, 403, "Inicia sesión con una cuenta administrativa");
                    return;
                }
            }
        }
        cadena.doFilter(solicitud, respuesta);
    }

    private void rechazar(HttpServletResponse respuesta, int codigo, String mensaje)
            throws IOException {
        respuesta.setStatus(codigo);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
