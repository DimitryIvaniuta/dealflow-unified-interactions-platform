package com.github.dimitryivaniuta.dealflow.service.auth;

import com.github.dimitryivaniuta.dealflow.config.auth.PasswordPolicyProps;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Validates user passwords against a configurable policy.
 *
 * <p>Production-grade:
 * <ul>
 *   <li>Enforces length and character classes.</li>
 *   <li>Returns a concise error message (no password echo).</li>
 * </ul>
 */
@Component
public class PasswordPolicy {

    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    private final PasswordPolicyProps props;

    public PasswordPolicy(PasswordPolicyProps props) {
        this.props = props;
    }

    /** Throws {@link PasswordPolicyViolationException} if password violates policy. */
    public void validateOrThrow(String password) {
        List<String> problems = new ArrayList<>();
        if (password == null || password.isBlank()) {
            throw new PasswordPolicyViolationException("Password must not be blank");
        }

        if (password.length() < props.minLength()) {
            problems.add("min length " + props.minLength());
        }
        if (props.requireUpper() && !UPPER.matcher(password).matches()) {
            problems.add("one uppercase letter");
        }
        if (props.requireLower() && !LOWER.matcher(password).matches()) {
            problems.add("one lowercase letter");
        }
        if (props.requireDigit() && !DIGIT.matcher(password).matches()) {
            problems.add("one digit");
        }
        if (props.requireSpecial() && !SPECIAL.matcher(password).matches()) {
            problems.add("one special character");
        }

        if (!problems.isEmpty()) {
            throw new PasswordPolicyViolationException("Password policy violation: " + String.join(", ", problems));
        }
    }
}
