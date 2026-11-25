package cl.duoc.therionlabs_backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException error) {
        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "estado",
                                "INVALIDO",
                                "mensaje",
                                error.getMessage() == null
                                        ? "Datos inválidos"
                                        : error.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> noAutorizado(SecurityException error) {
        return ResponseEntity.status(401)
                .body(Map.of("estado", "NO_AUTORIZADO", "mensaje", "Credencial no válida"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarDuplicado(
            DataIntegrityViolationException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "estado",
                                "CONFLICTO",
                                "mensaje",
                                "El registro incumple una restricción de la base de datos"));
    }
}
