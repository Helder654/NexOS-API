package com.example.nexos.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.nexos.dtos.CreateServiceOrderDTO;
import com.example.nexos.dtos.ServiceOrderDTO;
import com.example.nexos.services.ServiceOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/service-orders")
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    public ServiceOrderController(ServiceOrderService serviceOrderService) {
        this.serviceOrderService = serviceOrderService;
    }

    @PostMapping
    public ResponseEntity<ServiceOrderDTO> create(@Valid @RequestBody CreateServiceOrderDTO createServiceOrderDTO) {
        ServiceOrderDTO createdServiceOrder = serviceOrderService.create(createServiceOrderDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdServiceOrder.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdServiceOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderDTO> findById(@PathVariable Long id) {
        ServiceOrderDTO serviceOrderDTO = serviceOrderService.findById(id);

        return ResponseEntity.ok(serviceOrderDTO);
    }

    @GetMapping
    public ResponseEntity<List<ServiceOrderDTO>> findAll() {
        List<ServiceOrderDTO> serviceOrders = serviceOrderService.findAll();

        return ResponseEntity.ok(serviceOrders);
    }

}
