package com.example.nexos.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.example.nexos.dtos.CreateServiceOrderDTO;
import com.example.nexos.dtos.PageResponseDTO;
import com.example.nexos.dtos.AssignServiceOrderTechnicianDTO;
import com.example.nexos.dtos.ServiceOrderDTO;
import com.example.nexos.dtos.ServiceOrderFilterDTO;
import com.example.nexos.dtos.ServiceOrderStatusHistoryDTO;
import com.example.nexos.dtos.UpdateServiceOrderDTO;
import com.example.nexos.dtos.UpdateServiceOrderStatusDTO;
import com.example.nexos.exceptions.InvalidServiceOrderStatusException;
import com.example.nexos.exceptions.InvalidServiceOrderTechnicianException;
import com.example.nexos.exceptions.InvalidServiceOrderDeletionException;
import com.example.nexos.exceptions.InvalidServiceOrderFilterException;
import com.example.nexos.exceptions.ResourceNotFoundException;
import com.example.nexos.mappers.ServiceOrderMapper;
import com.example.nexos.mappers.ServiceOrderStatusHistoryMapper;
import com.example.nexos.models.ClientModel;
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.models.UserModel;
import com.example.nexos.models.UserRole;
import com.example.nexos.repositories.ClientRepository;
import com.example.nexos.repositories.ServiceOrderRepository;
import com.example.nexos.repositories.ServiceOrderStatusHistoryRepository;
import com.example.nexos.repositories.UserRepository;
import com.example.nexos.specifications.ServiceOrderSpecification;

@Service
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ClientRepository clientRepository;
    private final ServiceOrderMapper serviceOrderMapper;
    private final ServiceOrderStatusHistoryRepository serviceOrderStatusHistoryRepository;
    private final ServiceOrderStatusHistoryMapper serviceOrderStatusHistoryMapper;
    private final UserRepository userRepository;

    public ServiceOrderService(ServiceOrderRepository serviceOrderRepository, ClientRepository clientRepository,
            ServiceOrderMapper serviceOrderMapper, ServiceOrderStatusHistoryRepository serviceOrderStatusHistoryRepository,
            ServiceOrderStatusHistoryMapper serviceOrderStatusHistoryMapper, UserRepository userRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.clientRepository = clientRepository;
        this.serviceOrderMapper = serviceOrderMapper;
        this.serviceOrderStatusHistoryRepository = serviceOrderStatusHistoryRepository;
        this.serviceOrderStatusHistoryMapper = serviceOrderStatusHistoryMapper;
        this.userRepository = userRepository;
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

    public PageResponseDTO<ServiceOrderDTO> findAll(ServiceOrderFilterDTO serviceOrderFilterDTO, Pageable pageable) {
        validateFilter(serviceOrderFilterDTO);

        Page<ServiceOrderDTO> serviceOrderPage = serviceOrderRepository
                .findAll(ServiceOrderSpecification.withFilters(serviceOrderFilterDTO), pageable)
                .map(serviceOrderMapper::map);

        return PageResponseDTO.from(serviceOrderPage);
    }

    public ServiceOrderDTO update(Long id, UpdateServiceOrderDTO updateServiceOrderDTO) {
        ServiceOrderModel serviceOrderModel = findServiceOrderModelById(id);
        serviceOrderMapper.updateModel(updateServiceOrderDTO, serviceOrderModel);

        ServiceOrderModel updatedServiceOrder = serviceOrderRepository.save(serviceOrderModel);

        return serviceOrderMapper.map(updatedServiceOrder);
    }

    public ServiceOrderDTO assignTechnician(Long id,
            AssignServiceOrderTechnicianDTO assignServiceOrderTechnicianDTO) {
        ServiceOrderModel serviceOrderModel = findServiceOrderModelById(id);

        if (!serviceOrderModel.getStatus().canAssignTechnician()) {
            throw new InvalidServiceOrderTechnicianException(
                    "Não é possível atribuir um técnico a uma ordem com status " + serviceOrderModel.getStatus());
        }

        Long technicianId = assignServiceOrderTechnicianDTO.getTecnicoId();
        UserModel technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico com id " + technicianId + " não foi encontrado"));

        if (technician.getRole() != UserRole.TECNICO) {
            throw new InvalidServiceOrderTechnicianException(
                    "O usuário com id " + technicianId + " não possui o papel TECNICO");
        }

        serviceOrderModel.setTecnico(technician);
        ServiceOrderModel updatedServiceOrder = serviceOrderRepository.save(serviceOrderModel);

        return serviceOrderMapper.map(updatedServiceOrder);
    }

    @Transactional
    public ServiceOrderDTO updateStatus(Long id, UpdateServiceOrderStatusDTO updateServiceOrderStatusDTO) {
        ServiceOrderModel serviceOrderModel = findServiceOrderModelById(id);
        ServiceOrderStatus previousStatus = serviceOrderModel.getStatus();
        ServiceOrderStatus newStatus = updateServiceOrderStatusDTO.getStatus();

        if (!previousStatus.canTransitionTo(newStatus)) {
            throw new InvalidServiceOrderStatusException(
                    "Não é possível alterar o status de " + previousStatus + " para " + newStatus);
        }

        serviceOrderModel.setStatus(newStatus);
        ServiceOrderModel updatedServiceOrder = serviceOrderRepository.save(serviceOrderModel);
        serviceOrderStatusHistoryRepository.save(
                serviceOrderStatusHistoryMapper.map(updatedServiceOrder, previousStatus, newStatus));

        return serviceOrderMapper.map(updatedServiceOrder);
    }

    @Transactional
    public void delete(Long id) {
        ServiceOrderModel serviceOrderModel = findServiceOrderModelById(id);

        if (!serviceOrderModel.getStatus().canBeDeleted()) {
            throw new InvalidServiceOrderDeletionException(
                    "A ordem de serviço com status " + serviceOrderModel.getStatus() + " não pode ser excluída");
        }

        serviceOrderStatusHistoryRepository.deleteByServiceOrderId(id);
        serviceOrderRepository.delete(serviceOrderModel);
    }

    public List<ServiceOrderStatusHistoryDTO> findStatusHistory(Long id) {
        findServiceOrderModelById(id);

        return serviceOrderStatusHistoryRepository.findByServiceOrderIdOrderByDataAlteracaoDesc(id)
                .stream()
                .map(serviceOrderStatusHistoryMapper::map)
                .toList();
    }

    private ServiceOrderModel findServiceOrderModelById(Long id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ordem de serviço com id " + id + " não foi encontrada"));
    }

    private void validateFilter(ServiceOrderFilterDTO serviceOrderFilterDTO) {
        if (serviceOrderFilterDTO.getDataAberturaInicial() != null
                && serviceOrderFilterDTO.getDataAberturaFinal() != null
                && serviceOrderFilterDTO.getDataAberturaInicial().isAfter(serviceOrderFilterDTO.getDataAberturaFinal())) {
            throw new InvalidServiceOrderFilterException(
                    "A data inicial não pode ser posterior à data final");
        }
    }

}
