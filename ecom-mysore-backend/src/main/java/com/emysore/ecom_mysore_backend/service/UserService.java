package com.emysore.ecom_mysore_backend.service;

import com.emysore.ecom_mysore_backend.model.User;
import com.emysore.ecom_mysore_backend.model.Role;
import com.emysore.ecom_mysore_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // (Tokens are now JWTs; we no longer store session tokens in-memory)

    public User register(String username, String rawPassword) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(rawPassword));
        return userRepository.save(u);
    }

    public User registerAdmin(String username, String rawPassword) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(Role.ADMIN);
        return userRepository.save(u);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        Optional<User> maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) return Optional.empty();
        User u = maybe.get();
        if (encoder.matches(rawPassword, u.getPassword())) {
            return Optional.of(u);
        }
        return Optional.empty();
    }
}
