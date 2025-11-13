package com.emysore.ecom_mysore_backend.controller;

import com.emysore.ecom_mysore_backend.model.User;
import com.emysore.ecom_mysore_backend.model.Role;
import com.emysore.ecom_mysore_backend.service.UserService;
import com.emysore.ecom_mysore_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }
        if (userService.findByUsername(username).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "user_exists"));
        }
        User u = userService.register(username, password);
        return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername()));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }
        if (userService.findByUsername(username).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "user_exists"));
        }
        User u = userService.registerAdmin(username, password);
        return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername(), "role", "ADMIN"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }
        return userService.authenticate(username, password)
                .map(user -> {
                    String token = jwtUtil.generateToken(user);
                    Map<String, Object> resp = Map.of(
                        "token", token,
                        "username", user.getUsername(),
                        "role", user.getRole().name()
                    );
                    // If ObjectMapper is available, disable pretty-printing for this response
                    if (objectMapper != null) {
                        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
                    }
                    return ResponseEntity.ok(resp);
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "invalid_credentials")));
    }
}
