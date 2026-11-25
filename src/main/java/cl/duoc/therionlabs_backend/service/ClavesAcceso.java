package cl.duoc.therionlabs_backend.service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class ClavesAcceso {
    private ClavesAcceso() {}

    public static String aleatoria() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String derivar(String clave, String sal) {
        PBEKeySpec datos =
                new PBEKeySpec(
                        clave.toCharArray(), Base64.getUrlDecoder().decode(sal), 210000, 256);
        try {
            return Base64.getEncoder()
                    .encodeToString(
                            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                                    .generateSecret(datos)
                                    .getEncoded());
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("No fue posible verificar la contraseña", error);
        } finally {
            datos.clearPassword();
        }
    }

    public static boolean coincide(String clave, String sal, String resumen) {
        return MessageDigest.isEqual(
                derivar(clave, sal).getBytes(StandardCharsets.UTF_8),
                resumen.getBytes(StandardCharsets.UTF_8));
    }

    public static String resumen(String token) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException(error);
        }
    }
}
