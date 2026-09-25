package com.example.nexos.services;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nexos.dtos.CreateUserDTO;
import com.example.nexos.dtos.PageResponseDTO;
import com.example.nexos.dtos.UpdateUserDTO;
import com.example.nexos.dtos.UpdateUserPasswordDTO;
import com.example.nexos.dtos.UserDTO;
import com.example.nexos.exceptions.EmailAlreadyInUseException;
import com.example.nexos.exceptions.InvalidUserOperationException;
import com.example.nexos.exceptions.ResourceNotFoundException;
import com.example.nexos.mappers.UserMapper;
import com.example.nexos.models.UserModel;
import com.example.nexos.models.UserRole;
import com.example.nexos.repositories.UserRepository;
import com.example.nexos.repositories.ServiceOrderRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ServiceOrderRepository serviceOrderRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
            ServiceOrderRepository serviceOrderRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.serviceOrderRepository = serviceOrderRepository;
    }

    public UserDTO create(CreateUserDTO createUserDTO) {
        String normalizedEmail = normalizeEmail(createUserDTO.getEmail());
        validateEmailAvailability(normalizedEmail, null);

        UserModel userModel = userMapper.map(createUserDTO);
        userModel.setNome(createUserDTO.getNome().trim());
        userModel.setEmail(normalizedEmail);
        userModel.setSenha(passwordEncoder.encode(createUserDTO.getSenha()));
        UserModel savedUser = userRepository.save(userModel);

        return userMapper.map(savedUser);
    }

    public UserDTO findById(Long id) {
        return userMapper.map(findUserModelById(id));
    }

    public PageResponseDTO<UserDTO> findAll(Pageable pageable) {
        Page<UserDTO> userPage = userRepository.findAll(pageable)
                .map(userMapper::map);

        return PageResponseDTO.from(userPage);
    }

    public UserDTO update(Long id, UpdateUserDTO updateUserDTO) {
        UserModel userModel = findUserModelById(id);
        validateLastAdminRoleChange(userModel, updateUserDTO.getRole());

        String normalizedEmail = normalizeEmail(updateUserDTO.getEmail());
        validateEmailAvailability(normalizedEmail, userModel.getEmail());

        userMapper.updateModel(updateUserDTO, userModel);
        userModel.setNome(updateUserDTO.getNome().trim());
        userModel.setEmail(normalizedEmail);
        UserModel updatedUser = userRepository.save(userModel);

        return userMapper.map(updatedUser);
    }

    public void updatePassword(Long id, UpdateUserPasswordDTO updateUserPasswordDTO) {
        UserModel userModel = findUserModelById(id);
        userModel.setSenha(passwordEncoder.encode(updateUserPasswordDTO.getSenha()));
        userRepository.save(userModel);
    }

    public void delete(Long id, String currentUserEmail) {
        UserModel userModel = findUserModelById(id);

        if (userModel.getEmail().equals(normalizeEmail(currentUserEmail))) {
            throw new InvalidUserOperationException("Um usuário não pode excluir a própria conta");
        }

        if (serviceOrderRepository.existsByTecnicoId(id)) {
            throw new InvalidUserOperationException(
                    "O técnico não pode ser excluído enquanto possuir ordens de serviço atribuídas");
        }

        validateLastAdminRoleChange(userModel, null);
        userRepository.delete(userModel);
    }

    private UserModel findUserModelById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com id " + id + " não foi encontrado"));
    }

    private void validateEmailAvailability(String email, String currentEmail) {
        if (!email.equals(currentEmail) && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("Já existe um usuário cadastrado com este e-mail");
        }
    }

    private void validateLastAdminRoleChange(UserModel userModel, UserRole newRole) {
        boolean removesAdminRole = userModel.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN;

        if (removesAdminRole && userRepository.countByRole(UserRole.ADMIN) == 1) {
            throw new InvalidUserOperationException(
                    "O último administrador do sistema não pode ser removido ou ter seu papel alterado");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
