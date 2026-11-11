package com.example.nexo_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Secure by default: every endpoint except the health checks requires a valid Supabase JWT.
 * Local development can opt out explicitly with NEXO_AUTH_DISABLED=true (all calls then act as one local user).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource") CorsConfigurationSource cors,
                                            @Value("${nexo.auth.disabled:false}") boolean authDisabled) throws Exception {
        http.cors(c -> c.configurationSource(cors))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (authDisabled) {
            http.authorizeHttpRequests(a -> a.anyRequest().permitAll());
        } else {
            http.authorizeHttpRequests(a -> a
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/health", "/status", "/api/v1/health", "/api/v1/status").permitAll()
                    .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
        }
        return http.build();
    }

    /** Verifies Supabase access tokens: asymmetric keys through the JWKS endpoint, or the legacy HS256 secret. */
    @Bean
    @ConditionalOnProperty(name = "nexo.auth.disabled", havingValue = "false", matchIfMissing = true)
    JwtDecoder jwtDecoder(@Value("${nexo.auth.jwks-uri:}") String jwksUri,
                          @Value("${nexo.auth.issuer:}") String issuer,
                          @Value("${nexo.auth.hs256-secret:}") String hs256Secret) {
        NimbusJwtDecoder decoder;
        if (!hs256Secret.isBlank()) {
            decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(hs256Secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                    .macAlgorithm(MacAlgorithm.HS256).build();
        } else if (!jwksUri.isBlank()) {
            decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                    .jwsAlgorithms(algorithms -> { algorithms.add(SignatureAlgorithm.ES256); algorithms.add(SignatureAlgorithm.RS256); })
                    .build();
        } else {
            throw new IllegalStateException("Falta SUPABASE_JWKS_URI (o SUPABASE_JWT_SECRET). Para desarrollo local sin cuentas usa NEXO_AUTH_DISABLED=true.");
        }
        OAuth2TokenValidator<Jwt> validator = issuer.isBlank() ? JwtValidators.createDefault() : JwtValidators.createDefaultWithIssuer(issuer);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validator));
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${nexo.cors.allowed-origins}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowed = Arrays.stream(origins.split(",")).map(String::trim).filter(o -> !o.isEmpty()).toList();
        config.setAllowedOrigins(allowed);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Client-Platform"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
