package com.example.nexos.controllers;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.nexos.dtos.CreateUserDTO;
import com.example.nexos.dtos.PageResponseDTO;
import com.example.nexos.dtos.UpdateUserDTO;
import com.example.nexos.dtos.UpdateUserPasswordDTO;
import com.example.nexos.dtos.UserDTO;
import com.example.nexos.services.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserDTO createdUser = userService.create(createUserDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        UserDTO userDTO = userService.findById(id);

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<UserDTO>> findAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PageResponseDTO<UserDTO> users = userService.findAll(pageable);

        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        UserDTO updatedUser = userService.update(id, updateUserDTO);

        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
            @Valid @RequestBody UpdateUserPasswordDTO updateUserPasswordDTO) {
        userService.updatePassword(id, updateUserPasswordDTO);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        userService.delete(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}
