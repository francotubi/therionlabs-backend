package cl.duoc.therionlabs_backend;

import cl.duoc.therionlabs_backend.service.DispositivoService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

@Configuration
@Profile("activar-dispositivo")
public class DispositivoTestRunner {

    @Bean
    CommandLineRunner activarDispositivo(DispositivoService dispositivoService) {

        return args -> {
            UUID captadorId = UUID.fromString("a1234567-1234-4234-8234-123456789abc");

            var resultado = dispositivoService.registrarDispositivo(captadorId);

            System.out.println("DISPOSITIVO ID: " + resultado.dispositivoId());

            System.out.println("TOKEN DE PRUEBA: " + resultado.credencial());
        };
    }
}
