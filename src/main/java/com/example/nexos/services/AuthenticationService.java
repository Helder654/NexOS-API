package com.example.nexos.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.example.nexos.dtos.LoginRequestDTO;
import com.example.nexos.dtos.LoginResponseDTO;
import com.example.nexos.exceptions.InvalidCredentialsException;
import com.example.nexos.models.UserModel;
import com.example.nexos.repositories.UserRepository;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository,
            TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getSenha()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException("E-mail ou senha inválidos");
        }

        UserModel userModel = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("E-mail ou senha inválidos"));

        return tokenService.generateToken(userModel);
    }

}
