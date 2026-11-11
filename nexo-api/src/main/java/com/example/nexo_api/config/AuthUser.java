package com.example.nexo_api.config;

import org.springframework.security.oauth2.jwt.Jwt;

/** Resolves who is calling. The id always comes from the verified JWT, never from a request body. */
public final class AuthUser {
    /** Used only when authentication is explicitly disabled (NEXO_AUTH_DISABLED=true, local development). */
    public static final String LOCAL_USER = "local-user";

    private AuthUser() {}

    public static String id(Jwt jwt) {
        return jwt == null ? LOCAL_USER : jwt.getSubject();
    }
}
