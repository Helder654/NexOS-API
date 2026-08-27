package com.example.nexos.mappers;

import org.springframework.stereotype.Component;

import com.example.nexos.dtos.ClientDTO;
import com.example.nexos.models.ClientModel;

@Component
public class ClientMapper {

    public ClientModel map(ClientDTO clientDTO){
    ClientModel clientModel = new ClientModel();

    clientModel.setId(clientDTO.getId());
    clientModel.setNome(clientDTO.getNome());
    clientModel.setTelefone(clientDTO.getTelefone());
    clientModel.setEmail(clientDTO.getEmail());

    return clientModel;
    }

    public ClientDTO map(ClientModel clientModel){
        ClientDTO clientDTO = new ClientDTO();
        
        clientDTO.setId(clientModel.getId());
        clientDTO.setNome(clientModel.getNome());
        clientDTO.setTelefone(clientModel.getTelefone());
        clientDTO.setEmail(clientModel.getEmail());

        return clientDTO;

    }
}
