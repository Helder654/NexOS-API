package com.example.nexos.services;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.nexos.dtos.CreateServiceOrderDTO;
import com.example.nexos.dtos.PageResponseDTO;
import com.example.nexos.dtos.ServiceOrderDTO;
import com.example.nexos.dtos.UpdateServiceOrderDTO;
import com.example.nexos.dtos.UpdateServiceOrderStatusDTO;
import com.example.nexos.exceptions.InvalidServiceOrderStatusException;
import com.example.nexos.exceptions.InvalidServiceOrderDeletionException;
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

    public PageResponseDTO<ServiceOrderDTO> findAll(Pageable pageable) {
        Page<ServiceOrderDTO> serviceOrderPage = serviceOrderRepository.findAll(pageable)
                .map(serviceOrderMapper::map);

        return PageResponseDTO.from(serviceOrderPage);
    }

    public ServiceOrderDTO update(Long id, UpdateServiceOrderDTO updateServiceOrderDTO) {
        ServiceOrderModel serviceOrderModel = findServiceOrderModelById(id);
        serviceOrderMapper.updateModel(updateServiceOrderDTO, serviceOrderModel);

        ServiceOrderModel updatedServiceOrder = serviceOrderRepository.save(serviceOrderModel);

        return serviceOrderMapper.map(updatedServiceOrder);
    }

    public ServiceOrderDTO updateStatus(Long id, UpdateServiceOrderStatusDTO updateServiceOrderStatusDTO) {
        ServiceOrderModel serviceOrderModel = findServiceOrderModelById(id);
        ServiceOrderStatus newStatus = updateServiceOrderStatusDTO.getStatus();

        if (!serviceOrderModel.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidServiceOrderStatusException(
                    "Não é possível alterar o status de " + serviceOrderModel.getStatus() + " para " + newStatus);
        }

        serviceOrderModel.setStatus(newStatus);
        ServiceOrderModel updatedServiceOrder = serviceOrderRepository.save(serviceOrderModel);

        return serviceOrderMapper.map(updatedServiceOrder);
    }

    public void delete(Long id) {
        ServiceOrderModel serviceOrderModel = findServiceOrderModelById(id);

        if (!serviceOrderModel.getStatus().canBeDeleted()) {
            throw new InvalidServiceOrderDeletionException(
                    "A ordem de serviço com status " + serviceOrderModel.getStatus() + " não pode ser excluída");
        }

        serviceOrderRepository.delete(serviceOrderModel);
    }

    private ServiceOrderModel findServiceOrderModelById(Long id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ordem de serviço com id " + id + " não foi encontrada"));
    }

}
