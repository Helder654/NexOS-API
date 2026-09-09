package com.example.nexos.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.nexos.dtos.CreateServiceOrderDTO;
import com.example.nexos.dtos.PageResponseDTO;
import com.example.nexos.dtos.ServiceOrderDTO;
import com.example.nexos.dtos.ServiceOrderFilterDTO;
import com.example.nexos.dtos.ServiceOrderStatusHistoryDTO;
import com.example.nexos.dtos.UpdateServiceOrderDTO;
import com.example.nexos.dtos.UpdateServiceOrderStatusDTO;
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
    public ResponseEntity<PageResponseDTO<ServiceOrderDTO>> findAll(
            @ModelAttribute ServiceOrderFilterDTO serviceOrderFilterDTO,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDTO<ServiceOrderDTO> serviceOrders = serviceOrderService.findAll(serviceOrderFilterDTO, pageable);

        return ResponseEntity.ok(serviceOrders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOrderDTO> update(@PathVariable Long id,
            @Valid @RequestBody UpdateServiceOrderDTO updateServiceOrderDTO) {
        ServiceOrderDTO updatedServiceOrder = serviceOrderService.update(id, updateServiceOrderDTO);

        return ResponseEntity.ok(updatedServiceOrder);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceOrderDTO> updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateServiceOrderStatusDTO updateServiceOrderStatusDTO) {
        ServiceOrderDTO updatedServiceOrder = serviceOrderService.updateStatus(id, updateServiceOrderStatusDTO);

        return ResponseEntity.ok(updatedServiceOrder);
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<ServiceOrderStatusHistoryDTO>> findStatusHistory(@PathVariable Long id) {
        List<ServiceOrderStatusHistoryDTO> statusHistory = serviceOrderService.findStatusHistory(id);

        return ResponseEntity.ok(statusHistory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceOrderService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
