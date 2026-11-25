package cl.duoc.therionlabs_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class AutorizacionFiltro extends OncePerRequestFilter {
    private final byte[] clave;

    public AutorizacionFiltro(@Value("${THERIONLABS_EXPORT_KEY}") String clave) {
        if (clave == null || clave.isBlank())
            throw new IllegalArgumentException("Configura THERIONLABS_EXPORT_KEY");
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String ruta = solicitud.getRequestURI().substring(solicitud.getContextPath().length());
        boolean escrituraEvento =
                (ruta.equals("/api/eventos") || ruta.startsWith("/api/eventos/"))
                        && !solicitud.getMethod().equals("GET")
                        && !solicitud.getMethod().equals("HEAD");
        if (ruta.startsWith("/api/admin/") || escrituraEvento) {
            String recibida = solicitud.getHeader("X-Admin-Key");
            if (recibida == null
                    || !MessageDigest.isEqual(clave, recibida.getBytes(StandardCharsets.UTF_8))) {
                respuesta.setStatus(403);
                respuesta.setContentType("application/json;charset=UTF-8");
                respuesta
                        .getWriter()
                        .write(
                                "{\"estado\":\"PROHIBIDO\",\"mensaje\":\"Clave administrativa"
                                    + " incorrecta\"}");
                return;
            }
        }
        cadena.doFilter(solicitud, respuesta);
    }
}
