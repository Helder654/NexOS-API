package com.example.nexos.services;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.nexos.dtos.ClientDTO;
import com.example.nexos.dtos.CreateClientDTO;
import com.example.nexos.dtos.PageResponseDTO;
import com.example.nexos.dtos.UpdateClientDTO;
import com.example.nexos.exceptions.ResourceNotFoundException;
import com.example.nexos.mappers.ClientMapper;
import com.example.nexos.models.ClientModel;
import com.example.nexos.repositories.ClientRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    public ClientDTO create(CreateClientDTO createClientDTO) {
        ClientModel clientModel = clientMapper.map(createClientDTO);
        ClientModel savedClient = clientRepository.save(clientModel);

        return clientMapper.map(savedClient);
    }

    public ClientDTO findById(Long id) {
        return clientMapper.map(findClientModelById(id));
    }

    public PageResponseDTO<ClientDTO> findAll(Pageable pageable) {
        Page<ClientDTO> clientPage = clientRepository.findAll(pageable)
                .map(clientMapper::map);

        return PageResponseDTO.from(clientPage);
    }

    public ClientDTO update(Long id, UpdateClientDTO updateClientDTO) {
        ClientModel clientModel = findClientModelById(id);
        clientMapper.updateModel(updateClientDTO, clientModel);
        ClientModel updatedClient = clientRepository.save(clientModel);

        return clientMapper.map(updatedClient);
    }

    public void delete(Long id) {
        ClientModel clientModel = findClientModelById(id);
        clientRepository.delete(clientModel);
    }

    private ClientModel findClientModelById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente com id " + id + " não foi encontrado"));
    }

}
