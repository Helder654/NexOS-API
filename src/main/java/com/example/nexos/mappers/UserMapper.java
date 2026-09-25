package com.example.nexos.mappers;

import org.springframework.stereotype.Component;

import com.example.nexos.dtos.CreateUserDTO;
import com.example.nexos.dtos.UpdateUserDTO;
import com.example.nexos.dtos.UserDTO;
import com.example.nexos.models.UserModel;

@Component
public class UserMapper {

    public UserModel map(CreateUserDTO createUserDTO) {
        UserModel userModel = new UserModel();

        userModel.setNome(createUserDTO.getNome());
        userModel.setEmail(createUserDTO.getEmail());
        userModel.setRole(createUserDTO.getRole());

        return userModel;
    }

    public UserDTO map(UserModel userModel) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(userModel.getId());
        userDTO.setNome(userModel.getNome());
        userDTO.setEmail(userModel.getEmail());
        userDTO.setRole(userModel.getRole());

        return userDTO;
    }

    public void updateModel(UpdateUserDTO updateUserDTO, UserModel userModel) {
        userModel.setNome(updateUserDTO.getNome());
        userModel.setEmail(updateUserDTO.getEmail());
        userModel.setRole(updateUserDTO.getRole());
    }
}
