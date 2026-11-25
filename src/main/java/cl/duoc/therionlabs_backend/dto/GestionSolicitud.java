package cl.duoc.therionlabs_backend.dto;

import java.util.UUID;

public record GestionSolicitud(
        UUID id, String responsable, String estado, String canal, String observacion) {}
