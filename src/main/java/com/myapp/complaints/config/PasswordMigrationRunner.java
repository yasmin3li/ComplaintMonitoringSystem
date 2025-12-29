package com.myapp.complaints.config;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final AccountRepo accountRepo;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) {
//        List<Account> users = accountRepo.findAll();
//
//        for (Account user : users) {
//
//            String password = user.getPassword();
//
//            // إذا لم تكن BCrypt
//            if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {
//                String encoded = validateAndEncodePassword(password);
//                user.setPassword(encoded);
//                accountRepo.save(user);
//
//                System.out.println("Password updated for user: " + user.getEmail());
//            }
//        }

        String raw = "YASMEN@bu3li5";
        String encoded = "$2a$10$gEJImlffyEScVA9g0qOLEemEBmHCeWAhVHqCqEb1KAaRFw/cP6l.6";

        boolean matches = passwordEncoder.matches(raw, encoded);

        System.out.println("PASSWORD MATCHES = " + matches);
    }
    private  String validateAndEncodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
//        if (!rawPassword.matches(".*[a-z].*")) {
//            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
//        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!rawPassword.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
//        if (!rawPassword.matches(".*[]\\[!@#$%^&*()_+\\-={};':\"\\\\|,.<>/?].*")) {
//            throw new IllegalArgumentException("Password must contain at least one special character");
//        }
        // تشفير كلمة المرور
        return passwordEncoder.encode(rawPassword);
    }
}
