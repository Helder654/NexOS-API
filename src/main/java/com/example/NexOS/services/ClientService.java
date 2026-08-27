package com.example.nexos.services;

import org.springframework.stereotype.Service;

import com.example.nexos.repositories.ClientRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    

}
