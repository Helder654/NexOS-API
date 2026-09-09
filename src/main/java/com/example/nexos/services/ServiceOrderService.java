package com.example.nexos.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.nexos.dtos.CreateServiceOrderDTO;
import com.example.nexos.dtos.ServiceOrderDTO;
import com.example.nexos.exceptions.ResourceNotFoundException;
import com.example.nexos.mappers.ServiceOrderMapper;
import com.example.nexos.models.ClientModel;
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.repositories.ClientRepository;
import com.example.nexos.repositories.ServiceOrderRepository;

@Service
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ClientRepository clientRepository;
    private final ServiceOrderMapper serviceOrderMapper;

    public ServiceOrderService(ServiceOrderRepository serviceOrderRepository, ClientRepository clientRepository,
            ServiceOrderMapper serviceOrderMapper) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.clientRepository = clientRepository;
        this.serviceOrderMapper = serviceOrderMapper;
    }

    public ServiceOrderDTO create(CreateServiceOrderDTO createServiceOrderDTO) {
        ClientModel clientModel = clientRepository.findById(createServiceOrderDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + createServiceOrderDTO.getClienteId() + " não foi encontrado"));

        ServiceOrderModel serviceOrderModel = serviceOrderMapper.map(createServiceOrderDTO, clientModel);
        serviceOrderModel.setStatus(ServiceOrderStatus.ABERTA);
        serviceOrderModel.setDataAbertura(LocalDateTime.now());

        ServiceOrderModel savedServiceOrder = serviceOrderRepository.save(serviceOrderModel);

        return serviceOrderMapper.map(savedServiceOrder);
    }

    public ServiceOrderDTO findById(Long id) {
        return serviceOrderMapper.map(findServiceOrderModelById(id));
    }

    private ServiceOrderModel findServiceOrderModelById(Long id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ordem de serviço com id " + id + " não foi encontrada"));
    }

}
