package com.example.nexos.controllers;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.nexos.dtos.ClientDTO;
import com.example.nexos.dtos.CreateClientDTO;
import com.example.nexos.dtos.PageResponseDTO;
import com.example.nexos.dtos.UpdateClientDTO;
import com.example.nexos.services.ClientService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clients")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<ClientDTO> create(@Valid @RequestBody CreateClientDTO createClientDTO) {
        ClientDTO createdClient = clientService.create(createClientDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdClient.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdClient);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    public ResponseEntity<ClientDTO> findById(@PathVariable Long id) {
        ClientDTO clientDTO = clientService.findById(id);

        return ResponseEntity.ok(clientDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'TECNICO')")
    public ResponseEntity<PageResponseDTO<ClientDTO>> findAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        PageResponseDTO<ClientDTO> clients = clientService.findAll(pageable);

        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public ResponseEntity<ClientDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateClientDTO updateClientDTO) {
        ClientDTO updatedClient = clientService.update(id, updateClientDTO);

        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
