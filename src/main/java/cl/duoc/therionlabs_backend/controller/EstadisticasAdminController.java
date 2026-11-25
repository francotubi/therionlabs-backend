package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.repository.ProspectoRepository;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/estadisticas")
public class EstadisticasAdminController {
    private final ProspectoRepository prospectos;

    public EstadisticasAdminController(ProspectoRepository prospectos) {
        this.prospectos = prospectos;
    }

    @GetMapping("/prospectos-por-evento")
    public Map<UUID, Long> prospectosPorEvento() {
        return prospectos.findAll().stream()
                .filter(p -> p.getEvento() != null && !p.isEliminado())
                .collect(Collectors.groupingBy(p -> p.getEvento().getId(), Collectors.counting()));
    }
}
