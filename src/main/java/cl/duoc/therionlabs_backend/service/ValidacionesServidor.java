package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.Prospecto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;

public final class ValidacionesServidor {
    private ValidacionesServidor() {}

    public static void texto(String valor, int minimo, int maximo, String campo) {
        if (valor == null || valor.trim().length() < minimo || valor.trim().length() > maximo)
            throw new IllegalArgumentException(campo + ": longitud inválida");
    }

    public static String rut(String valor) {
        if (valor == null) throw new IllegalArgumentException("El RUT es obligatorio");
        String limpio = valor.replaceAll("[.\\s-]", "").toUpperCase(Locale.ROOT);
        if (!limpio.matches("[0-9]{1,8}[0-9K]")
                || Long.parseLong(limpio.substring(0, limpio.length() - 1)) == 0)
            throw new IllegalArgumentException("RUT inválido");
        int suma = 0, factor = 2;
        for (int i = limpio.length() - 2; i >= 0; i--) {
            suma += (limpio.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - suma % 11;
        char digito = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        if (limpio.charAt(limpio.length() - 1) != digito)
            throw new IllegalArgumentException("Dígito verificador inválido");
        return limpio;
    }

    public static void prospecto(Prospecto datos) {
        rut(datos.getRut());
        texto(datos.getNombre(), 2, 80, "Nombre");
        texto(datos.getApellidos(), 2, 100, "Apellidos");
        String correo = datos.getEmail();
        if (correo == null
                || correo.length() > 254
                || !correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                || correo.substring(0, correo.indexOf('@')).contains(".."))
            throw new IllegalArgumentException("Correo inválido");
        if (datos.getCelular() == null
                || !datos.getCelular().replaceAll("[\\s()-]", "").matches("(?:\\+?56)?9[0-9]{8}"))
            throw new IllegalArgumentException("Celular inválido");
        try {
            LocalDate fecha =
                    LocalDate.parse(
                            datos.getFechaNacimiento(),
                            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                    .withResolverStyle(ResolverStyle.STRICT));
            if (fecha.isAfter(LocalDate.now()) || fecha.getYear() < 1)
                throw new IllegalArgumentException();
        } catch (Exception error) {
            throw new IllegalArgumentException("Fecha de nacimiento inválida");
        }
        texto(datos.getEstablecimiento(), 3, 150, "Establecimiento");
        texto(datos.getComunaResidencia(), 2, 100, "Comuna de residencia");
        texto(datos.getComunaEstablecimiento(), 2, 100, "Comuna del establecimiento");
        if (!"Región de Valparaíso".equals(datos.getRegion()))
            throw new IllegalArgumentException("Región no admitida");
        Set<String> oferta = CatalogoRegional.carreras(datos.getSede());
        List<String> carreras = new ArrayList<>();
        if (datos.getCarrera1() != null) carreras.add(datos.getCarrera1());
        if (datos.getCarrera2() != null) carreras.add(datos.getCarrera2());
        if (datos.getCarrera3() != null) carreras.add(datos.getCarrera3());
        if (carreras.isEmpty()
                || new HashSet<>(carreras).size() != carreras.size()
                || !oferta.containsAll(carreras))
            throw new IllegalArgumentException("Las carreras no corresponden a la sede");
        if (!Set.of("Diurna", "Vespertina").contains(Objects.toString(datos.getJornada(), "")))
            throw new IllegalArgumentException("Jornada inválida");
        if (!Set.of("Presencial", "Online").contains(Objects.toString(datos.getModalidad(), "")))
            throw new IllegalArgumentException("Modalidad inválida");
        if (datos.getObservaciones() != null && datos.getObservaciones().length() > 500)
            throw new IllegalArgumentException("Observación demasiado extensa");
        if ((datos.getLatitud() == null) != (datos.getLongitud() == null))
            throw new IllegalArgumentException("Coordenadas incompletas");
        if (datos.getLatitud() != null
                && (!Double.isFinite(datos.getLatitud())
                        || Math.abs(datos.getLatitud()) > 90
                        || !Double.isFinite(datos.getLongitud())
                        || Math.abs(datos.getLongitud()) > 180))
            throw new IllegalArgumentException("Coordenadas inválidas");
    }
}
