package com.laze.backend.security.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SamplePasswordGenerator {
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String plainPassword = "1234"; // 테스트용 비밀번호
        String encodedPassword = passwordEncoder.encode(plainPassword);
        System.out.println("Plain Password: " + plainPassword);
        System.out.println("Encoded Password (BCrypt): " + encodedPassword);
    }
}

