package com.emysore.ecom_mysore_backend.config;

import com.emysore.ecom_mysore_backend.model.Role;
import com.emysore.ecom_mysore_backend.model.User;
import com.emysore.ecom_mysore_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Dev-only data loader: creates a default admin user if none exists.
 * This class is safe for dev profile and does not run in production if profiles differ.
 */
@Component
@Profile("dev")
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public DevDataLoader(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminUsername = "admin";
        userRepository.findByUsername(adminUsername).ifPresentOrElse(u -> {
            // admin exists, nothing to do
        }, () -> {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(encoder.encode("adminpass"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("[DevDataLoader] Created dev admin user: 'admin' with password 'adminpass'");
        });
    }
}
