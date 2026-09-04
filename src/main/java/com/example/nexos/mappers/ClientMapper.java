package com.example.nexos.mappers;

import org.springframework.stereotype.Component;

import com.example.nexos.dtos.ClientDTO;
import com.example.nexos.dtos.CreateClientDTO;
import com.example.nexos.models.ClientModel;

@Component
public class ClientMapper {

    public ClientModel map(CreateClientDTO createClientDTO) {
        ClientModel clientModel = new ClientModel();

        clientModel.setNome(createClientDTO.getNome());
        clientModel.setTelefone(createClientDTO.getTelefone());
        clientModel.setEmail(createClientDTO.getEmail());

        return clientModel;
    }

    public ClientDTO map(ClientModel clientModel) {
        ClientDTO clientDTO = new ClientDTO();

        clientDTO.setId(clientModel.getId());
        clientDTO.setNome(clientModel.getNome());
        clientDTO.setTelefone(clientModel.getTelefone());
        clientDTO.setEmail(clientModel.getEmail());

        return clientDTO;
    }

}
