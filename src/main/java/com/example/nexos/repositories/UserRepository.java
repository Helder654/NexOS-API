package com.example.nexos.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexos.models.UserModel;
import com.example.nexos.models.UserRole;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    Optional<UserModel> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

}
