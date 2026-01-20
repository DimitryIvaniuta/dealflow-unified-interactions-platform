package com.github.dimitryivaniuta.dealflow.service.auth;

import com.github.dimitryivaniuta.dealflow.config.auth.JwtKeyProps;
import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccount;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Issues short-lived access tokens (JWT).
 *
 * <p>Why JWT access tokens:
 * <ul>
 *   <li>Stateless verification by resource servers</li>
 *   <li>Can include UI hints (roles claim) for role-aware navigation</li>
 * </ul>
 *
 * <p>Important compatibility note for this project:
 * <ul>
 *   <li>Workspace RBAC relies on {@code sub} to match the {@code df_workspace_members.subject} value.</li>
 *   <li>Therefore we set {@code sub = user.username} (e.g. "user-1").</li>
 * </ul>
 */
@Service
public class JwtIssuerService {

    private final JwtEncoder encoder;
    private final JwtKeyProps props;

    public JwtIssuerService(JwtEncoder encoder, JwtKeyProps props) {
        this.encoder = encoder;
        this.props = props;
    }

    public IssuedToken issueAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.accessTokenTtlMinutes(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(props.issuer())
            .issuedAt(now)
            .expiresAt(exp)
            .subject(user.getUsername())
            .claim("preferred_username", user.getUsername())
            .claim("roles", user.getRoles().stream().map(Enum::name).toList())
            .build();

        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new IssuedToken(token, exp, user.getUsername());
    }

    /** Simple value object returned to controllers. */
    public record IssuedToken(String token, Instant expiresAt, String subject) {
    }
}
