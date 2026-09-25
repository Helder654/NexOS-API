package com.example.nexos.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.nexos.models.UserModel;
import com.example.nexos.models.UserRole;
import com.example.nexos.repositories.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String name;
    private final String email;
    private final String password;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin.name:}") String name, @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (!StringUtils.hasText(name) || !StringUtils.hasText(email) || !StringUtils.hasText(password)
                || userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        UserModel admin = new UserModel();
        admin.setNome(name.trim());
        admin.setEmail(normalizedEmail);
        admin.setSenha(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);

        userRepository.save(admin);
    }

}
