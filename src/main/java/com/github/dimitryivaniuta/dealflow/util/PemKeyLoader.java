package com.github.dimitryivaniuta.dealflow.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.core.io.Resource;

/**
 * Loads RSA public/private keys from PEM files.
 *
 * <p>Why PEM:
 * <ul>
 *   <li>Easy to mount via Kubernetes/Docker secrets</li>
 *   <li>Simple key rotation (swap files + restart)</li>
 *   <li>Interoperable with non-Java tooling (OpenSSL, Vault, cloud secret stores)</li>
 * </ul>
 *
 * <p>Supported formats:
 * <ul>
 *   <li>Private key: PKCS#8 ("BEGIN PRIVATE KEY")</li>
 *   <li>Public key: X.509 SubjectPublicKeyInfo ("BEGIN PUBLIC KEY")</li>
 * </ul>
 */
public final class PemKeyLoader {

    private PemKeyLoader() {
    }

    public static RSAPrivateKey loadRsaPrivateKey(Resource pemResource) {
        try {
            String pem = readAll(pemResource);
            String base64 = stripPem(pem, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
            byte[] der = Base64.getMimeDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Failed to load RSA private key from " + safeName(pemResource), e);
        }
    }

    public static RSAPublicKey loadRsaPublicKey(Resource pemResource) {
        try {
            String pem = readAll(pemResource);
            String base64 = stripPem(pem, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
            byte[] der = Base64.getMimeDecoder().decode(base64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Failed to load RSA public key from " + safeName(pemResource), e);
        }
    }

    private static String readAll(Resource r) throws IOException {
        try (InputStream in = r.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String stripPem(String pem, String begin, String end) {
        String s = pem.replace("\r", "");
        int b = s.indexOf(begin);
        int e = s.indexOf(end);
        if (b < 0 || e < 0 || e <= b) {
            throw new IllegalArgumentException("Invalid PEM content, expected markers: " + begin + " ... " + end);
        }
        String body = s.substring(b + begin.length(), e);
        return body.replaceAll("\\s", "");
    }

    private static String safeName(Resource r) {
        try {
            return r.getURI().toString();
        } catch (IOException ex) {
            return r.toString();
        }
    }
}
