package cl.duoc.therionlabs_backend.controller;

import cl.duoc.therionlabs_backend.service.AccesoService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cuentas")
public class CuentasController {
    private final AccesoService acceso;

    public CuentasController(AccesoService acceso) {
        this.acceso = acceso;
    }

    @GetMapping
    public List<AccesoService.CuentaResumen> listar() {
        return acceso.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccesoService.CuentaResumen crear(@RequestBody AccesoService.CrearCuenta datos) {
        return acceso.crear(datos);
    }
}
