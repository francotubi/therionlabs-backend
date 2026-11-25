package cl.duoc.therionlabs_backend.service;

import cl.duoc.therionlabs_backend.entity.Captador;
import cl.duoc.therionlabs_backend.repository.CaptadorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class CaptadorAdminService {

    private final CaptadorRepository captadorRepository;

    public CaptadorAdminService(
            CaptadorRepository captadorRepository
    ) {
        this.captadorRepository = captadorRepository;
    }

    @Transactional
    public Captador registrarCaptador(
            String identificadorLocal,
            String nombre
    ) {
        if (identificadorLocal == null ||
                identificadorLocal.isBlank() ||
                nombre == null ||
                nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El identificador y el nombre son obligatorios"
            );
        }

        String identificador = identificadorLocal
                .trim()
                .toUpperCase(Locale.ROOT);

        String nombreLimpio = nombre.trim();

        if (!identificador.matches("CAP-[0-9]{2,4}")) {
            throw new IllegalArgumentException(
                    "El identificador debe tener formato CAP-01"
            );
        }

        if (captadorRepository.existsByIdentificadorLocal(
                identificador
        )) {
            throw new IllegalArgumentException(
                    "El captador ya está registrado"
            );
        }

        Captador captador = new Captador();
        captador.setId(UUID.randomUUID());
        captador.setIdentificadorLocal(identificador);
        captador.setNombre(nombreLimpio);
        captador.setActivo(true);

        return captadorRepository.save(captador);
    }
}
