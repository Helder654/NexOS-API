package com.example.nexos.services;

import org.springframework.stereotype.Service;

import com.example.nexos.dtos.ClientDTO;
import com.example.nexos.dtos.CreateClientDTO;
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

}
