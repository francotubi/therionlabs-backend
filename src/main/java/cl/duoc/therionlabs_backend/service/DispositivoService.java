package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.Captador;
import cl.duoc.therionlabs_backend.entity.Dispositivo;
import cl.duoc.therionlabs_backend.repository.CaptadorRepository;
import cl.duoc.therionlabs_backend.repository.DispositivoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class DispositivoService {

    private final DispositivoRepository dispositivoRepository;
    private final CaptadorRepository captadorRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public DispositivoService(
            DispositivoRepository dispositivoRepository,
            CaptadorRepository captadorRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.captadorRepository = captadorRepository;
    }

    @Transactional
    public CredencialActivacion registrarDispositivo(UUID captadorId) {

        Captador captador = captadorRepository.findById(captadorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Captador no encontrado"));

        if (!captador.isActivo()) {
            throw new IllegalArgumentException(
                    "El captador está deshabilitado");
        }

        if (dispositivoRepository.findByCaptadorId(captadorId).isPresent()) {
            throw new IllegalStateException(
                    "El captador ya tiene un dispositivo registrado");
        }

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String credencial = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setId(UUID.randomUUID());
        dispositivo.setCaptador(captador);
        dispositivo.setCredencialHash(calcularHash(credencial));
        dispositivo.setActivo(true);
        dispositivo.setFechaRegistro(LocalDateTime.now());

        dispositivoRepository.save(dispositivo);

        return new CredencialActivacion(
                dispositivo.getId(),
                credencial
        );
    }

    @Transactional(readOnly = true)
    public Dispositivo validarCredencial(String credencial) {

        if (credencial == null || credencial.isBlank()) {
            throw new SecurityException("Credencial no proporcionada");
        }

        Dispositivo dispositivo = dispositivoRepository
                .findByCredencialHash(calcularHash(credencial))
                .orElseThrow(() ->
                        new SecurityException("Credencial no válida"));

        if (!dispositivo.isActivo()
                || !dispositivo.getCaptador().isActivo()) {
            throw new SecurityException(
                    "Dispositivo o captador deshabilitado");
        }

        return dispositivo;
    }

    @Transactional
    public CredencialActivacion renovarCredencial(UUID captadorId) {
        Captador captador = captadorRepository.findById(captadorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Captador no encontrado"));
        if (!captador.isActivo()) {
            throw new IllegalArgumentException(
                    "El captador está deshabilitado");
        }
        Dispositivo dispositivo = dispositivoRepository
                .findByCaptadorId(captadorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El captador no tiene un dispositivo registrado"));

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String credencialNueva = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
        dispositivo.setCredencialHash(
                calcularHash(credencialNueva)
        );
        dispositivoRepository.save(dispositivo);
        return new CredencialActivacion(
                dispositivo.getId(),
                credencialNueva
        );
    }


    private String calcularHash(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 no disponible", e);
        }
    }

    public record CredencialActivacion(
            UUID dispositivoId,
            String credencial
    ) {}
}
