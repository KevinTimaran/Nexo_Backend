package com.example.nexo_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/auth", "/auth"})
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody(required = false) Map<String, Object> body) {
        String email = body != null && body.containsKey("email") ? String.valueOf(body.get("email")) : "kevin@nexo.ai";
        Map<String, Object> user = new HashMap<>();
        user.put("id", "u-a9f2c14e");
        user.put("name", "Kevin Timaran");
        user.put("email", email);

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", user);
        resp.put("id", user.get("id"));
        resp.put("name", user.get("name"));
        resp.put("email", user.get("email"));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "u-a9f2c14e");
        user.put("name", "Kevin Timaran");
        user.put("email", "kevin@nexo.ai");
        return ResponseEntity.ok(user);
    }
}
