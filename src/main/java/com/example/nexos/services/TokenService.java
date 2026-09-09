package com.example.nexos.services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.nexos.dtos.LoginResponseDTO;
import com.example.nexos.models.UserModel;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public TokenService(JwtEncoder jwtEncoder, @Value("${security.jwt.expiration:PT2H}") Duration expiration) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }

    public LoginResponseDTO generateToken(UserModel userModel) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("nexos-api")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userModel.getEmail())
                .claim("roles", List.of("ROLE_" + userModel.getRole().name()))
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponseDTO(token, "Bearer", expiration.toSeconds());
    }

}
