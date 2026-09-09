package com.example.nexos.services;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.nexos.models.UserModel;
import com.example.nexos.repositories.UserRepository;

@Service
public class NexosUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public NexosUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserModel userModel = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(userModel.getEmail())
                .password(userModel.getSenha())
                .roles(userModel.getRole().name())
                .build();
    }

}
