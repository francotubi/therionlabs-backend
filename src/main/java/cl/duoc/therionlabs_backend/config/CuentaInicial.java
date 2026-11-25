package cl.duoc.therionlabs_backend.config;

import cl.duoc.therionlabs_backend.repository.CuentaRepository;
import cl.duoc.therionlabs_backend.service.AccesoService;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CuentaInicial implements SmartInitializingSingleton {
    private final CuentaRepository cuentas;
    private final AccesoService acceso;
    private final String usuario;
    private final String clave;

    public CuentaInicial(
            CuentaRepository cuentas,
            AccesoService acceso,
            @Value("${THERIONLABS_ADMIN_USER:administrativo}") String usuario,
            @Value("${THERIONLABS_ADMIN_PASSWORD:}") String clave) {
        this.cuentas = cuentas;
        this.acceso = acceso;
        this.usuario = usuario;
        this.clave = clave;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (cuentas.count() == 0) {
            if (clave.isBlank())
                throw new IllegalStateException(
                        "Configura THERIONLABS_ADMIN_PASSWORD para crear la primera cuenta"
                                + " administrativa");
            acceso.crear(
                    new AccesoService.CrearCuenta(
                            usuario,
                            clave,
                            "Administración de prospectos",
                            "ADMINISTRATIVO",
                            null));
        }
    }
}
