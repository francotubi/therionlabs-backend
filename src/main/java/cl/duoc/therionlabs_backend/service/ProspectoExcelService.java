package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.Prospecto;
import cl.duoc.therionlabs_backend.repository.ProspectoRechazadoRepository;
import cl.duoc.therionlabs_backend.repository.ProspectoRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ProspectoExcelService {

    private final ProspectoRepository prospectoRepository;
    private final ProspectoRechazadoRepository rechazoRepository;

    public ProspectoExcelService(
            ProspectoRepository prospectoRepository,
            ProspectoRechazadoRepository rechazoRepository) {
        this.prospectoRepository = prospectoRepository;
        this.rechazoRepository = rechazoRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generarExcel(UUID eventoId) throws IOException {

        if (eventoId == null) throw new IllegalArgumentException("Evento obligatorio");
        List<Prospecto> prospectos =
                prospectoRepository.findByEventoId(eventoId).stream()
                        .filter(p -> !p.isEliminado())
                        .toList();

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet hoja = workbook.createSheet("Prospectos");
            CellStyle estiloEncabezado = workbook.createCellStyle();

            Font fuente = workbook.createFont();
            fuente.setBold(true);
            estiloEncabezado.setFont(fuente);

            String[] columnas = {
                "ID Prospecto",
                "ID Evento",
                "ID Captador",
                "RUT",
                "Nombre",
                "Apellidos",
                "Fecha nacimiento",
                "Celular",
                "Correo",
                "Establecimiento",
                "Comuna residencia",
                "Comuna establecimiento",
                "Actividad actual",
                "Jornada",
                "Región",
                "Sede",
                "Nivel interés",
                "Carrera 1",
                "Carrera 2",
                "Carrera 3",
                "Modalidad",
                "Canal contacto",
                "Observaciones",
                "Autoriza contacto",
                "Fecha captura",
                "Dirección",
                "Latitud",
                "Longitud",
                "Precisión metros",
                "Fecha ubicación",
                "Ruta fotografía",
                "Estado sincronización"
            };
            Row encabezado = hoja.createRow(0);

            for (int i = 0; i < columnas.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            int numeroFila = 1;

            for (Prospecto p : prospectos) {

                Row fila = hoja.createRow(numeroFila++);

                Object[] datos = {
                    p.getId(),
                    p.getEvento().getId(),
                    p.getCaptador().getId(),
                    p.getRut(),
                    p.getNombre(),
                    p.getApellidos(),
                    p.getFechaNacimiento(),
                    p.getCelular(),
                    p.getEmail(),
                    p.getEstablecimiento(),
                    p.getComunaResidencia(),
                    p.getComunaEstablecimiento(),
                    p.getActividadActual(),
                    p.getJornada(),
                    p.getRegion(),
                    p.getSede(),
                    p.getNivelInteres(),
                    p.getCarrera1(),
                    p.getCarrera2(),
                    p.getCarrera3(),
                    p.getModalidad(),
                    p.getCanalContacto(),
                    p.getObservaciones(),
                    p.isAutorizaContacto() ? "Sí" : "No",
                    formatearFecha(p.getFechaCaptura()),
                    p.getDireccion(),
                    p.getLatitud(),
                    p.getLongitud(),
                    p.getPrecisionMetros(),
                    formatearFecha(p.getFechaUbicacion()),
                    p.getFotoRuta(),
                    "SINCRONIZADO"
                };

                for (int i = 0; i < datos.length; i++) {
                    escribirTexto(fila, i, datos[i]);
                }
            }
            hoja.createFreezePane(0, 1);
            hoja.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0, Math.max(1, numeroFila - 1), 0, columnas.length - 1));
            for (int i = 0; i < columnas.length; i++) {
                hoja.setColumnWidth(i, 6000);
            }
            Sheet resumen = workbook.createSheet("Resumen");

            Row titulo = resumen.createRow(0);
            titulo.createCell(0).setCellValue("RESUMEN THERIONLABS");

            Row filaEvento = resumen.createRow(2);
            filaEvento.createCell(0).setCellValue("ID del evento");
            filaEvento.createCell(1).setCellValue(eventoId.toString());

            Row filaTotal = resumen.createRow(3);
            filaTotal.createCell(0).setCellValue("Prospectos sincronizados");
            filaTotal.createCell(1).setCellValue(prospectos.size());

            Row filaRechazados = resumen.createRow(4);
            filaRechazados.createCell(0).setCellValue("Prospectos rechazados");
            filaRechazados.createCell(1).setCellValue(rechazoRepository.countByEventoId(eventoId));
            Row subtitulo = resumen.createRow(5);
            subtitulo.createCell(0).setCellValue("ID Captador");
            subtitulo.createCell(1).setCellValue("Prospectos recibidos");

            java.util.Map<UUID, Long> porCaptador =
                    prospectos.stream()
                            .collect(
                                    java.util.stream.Collectors.groupingBy(
                                            p -> p.getCaptador().getId(),
                                            java.util.stream.Collectors.counting()));

            int filaActual = 6;

            for (var entrada : porCaptador.entrySet()) {
                Row fila = resumen.createRow(filaActual++);
                fila.createCell(0).setCellValue(entrada.getKey().toString());
                fila.createCell(1).setCellValue(entrada.getValue());
            }

            resumen.setColumnWidth(0, 8500);
            resumen.setColumnWidth(1, 9000);
            Sheet hojaRechazados = workbook.createSheet("Rechazados");

            String[] columnasRechazo = {
                "ID Prospecto",
                "ID Evento",
                "ID Captador",
                "RUT",
                "Nombre",
                "Apellidos",
                "Correo",
                "Estado",
                "Motivo del rechazo",
                "Fecha del rechazo"
            };

            Row encabezadoRechazos = hojaRechazados.createRow(0);

            for (int i = 0; i < columnasRechazo.length; i++) {
                Cell celda = encabezadoRechazos.createCell(i);
                celda.setCellValue(columnasRechazo[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            var rechazados = rechazoRepository.findByEventoId(eventoId);

            int filaRechazo = 1;

            for (var rechazo : rechazados) {

                Row fila = hojaRechazados.createRow(filaRechazo++);

                Object[] datosRechazo = {
                    rechazo.getProspectoId(),
                    rechazo.getEventoId(),
                    rechazo.getCaptadorId(),
                    rechazo.getRut(),
                    rechazo.getNombre(),
                    rechazo.getApellidos(),
                    rechazo.getEmail(),
                    "RECHAZADO",
                    rechazo.getMotivo(),
                    rechazo.getFechaRechazo()
                };

                for (int i = 0; i < datosRechazo.length; i++) {
                    escribirTexto(fila, i, datosRechazo[i]);
                }
            }

            hojaRechazados.createFreezePane(0, 1);

            hojaRechazados.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0, Math.max(1, filaRechazo - 1), 0, columnasRechazo.length - 1));

            for (int i = 0; i < columnasRechazo.length; i++) {
                hojaRechazados.setColumnWidth(i, 6500);
            }

            workbook.write(salida);

            return salida.toByteArray();
        }
    }

    private String formatearFecha(Long milisegundos) {

        if (milisegundos == null || milisegundos <= 0) {
            return "";
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return Instant.ofEpochMilli(milisegundos)
                .atZone(ZoneId.of("America/Santiago"))
                .format(formato);
    }

    private void escribirTexto(Row fila, int columna, Object valor) {

        Cell celda = fila.createCell(columna);

        String texto = valor == null ? "" : valor.toString();
        if (!texto.isEmpty() && "=+-@\t\r\n".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }

        celda.setCellValue(texto);
    }
}
