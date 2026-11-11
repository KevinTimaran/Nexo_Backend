package com.example.nexo_api.controller;

import com.example.nexo_api.config.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Sign-in, sign-up and sign-out happen directly against Supabase Auth from the client.
 * The backend only reports who the verified token belongs to.
 */
@RestController
@RequestMapping({"/api/v1/auth", "/auth"})
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", AuthUser.id(jwt));
        if (jwt != null) {
            user.put("email", jwt.getClaimAsString("email"));
            Map<String, Object> metadata = jwt.getClaimAsMap("user_metadata");
            if (metadata != null && metadata.get("name") != null) user.put("name", String.valueOf(metadata.get("name")));
        }
        return ResponseEntity.ok(user);
    }
}
