package com.github.dimitryivaniuta.dealflow.config.auth;

import com.github.dimitryivaniuta.dealflow.util.PemKeyLoader;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Configures {@link JwtEncoder}/{@link JwtDecoder} from RSA PEM files.
 *
 * <p>The decoder/encoder beans are marked {@link ConditionalOnMissingBean} so integration tests
 * can easily override {@link JwtDecoder} (see {@code BasePostgresIT}).
 */
@Configuration
@EnableConfigurationProperties(JwtKeyProps.class)
public class JwtPemKeysConfig {

    @Bean
    @ConditionalOnMissingBean
    public JwtEncoder jwtEncoder(JwtKeyProps props, ResourceLoader resourceLoader) {
        Resource privatePem = resourceLoader.getResource(props.privateKey());
        Resource publicPem = resourceLoader.getResource(props.publicKey());

        RSAPrivateKey privateKey = PemKeyLoader.loadRsaPrivateKey(privatePem);
        RSAPublicKey publicKey = PemKeyLoader.loadRsaPublicKey(publicPem);

        RSAKey rsa = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsa));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JwtKeyProps props, ResourceLoader resourceLoader) {
        Resource publicPem = resourceLoader.getResource(props.publicKey());
        RSAPublicKey publicKey = PemKeyLoader.loadRsaPublicKey(publicPem);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        // Issuer validation is typically enforced via JwtIssuerValidator, but we keep it simple:
        // the issuer is embedded in tokens we issue; external IdP validation can be enabled via issuer-uri.
        return decoder;
    }
}
