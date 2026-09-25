package com.example.nexos.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.nexos.models.UserModel;
import com.example.nexos.models.UserRole;
import com.example.nexos.models.ClientModel;
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.repositories.ClientRepository;
import com.example.nexos.repositories.ServiceOrderRepository;
import com.example.nexos.repositories.ServiceOrderStatusHistoryRepository;
import com.example.nexos.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserControllerIntegrationTests {

    private static final String ADMIN_PASSWORD = "senha-admin-segura";
    private static final String USER_PASSWORD = "senha-usuario-segura";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ServiceOrderStatusHistoryRepository serviceOrderStatusHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        clearDatabase();
    }

    @AfterEach
    void tearDown() {
        clearDatabase();
    }

    @Test
    void shouldCreateTechnicianAsAdministratorWithoutExposingPassword() throws Exception {
        String adminToken = createAdministratorToken();

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Marina Técnica",
                          "email": "MARINA@EXAMPLE.COM",
                          "senha": "senha-tecnica-segura",
                          "role": "TECNICO"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Marina Técnica"))
                .andExpect(jsonPath("$.email").value("marina@example.com"))
                .andExpect(jsonPath("$.role").value("TECNICO"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        UserModel technician = userRepository.findByEmail("marina@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("senha-tecnica-segura", technician.getSenha()))
                .isTrue();
    }

    @Test
    void shouldNotAllowAttendantToManageUsers() throws Exception {
        saveUser("Ana Atendente", "ana@example.com", USER_PASSWORD, UserRole.ATENDENTE);
        String attendantToken = loginAndGetToken("ana@example.com", USER_PASSWORD);

        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + attendantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldListUsersWithPagination() throws Exception {
        String adminToken = createAdministratorToken();
        saveUser("Bruno Técnico", "bruno@example.com", USER_PASSWORD, UserRole.TECNICO);
        saveUser("Carla Atendente", "carla@example.com", USER_PASSWORD, UserRole.ATENDENTE);

        mockMvc.perform(get("/users?page=0&size=2&sort=nome,asc")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("Administrador"));
    }

    @Test
    void shouldFindUserById() throws Exception {
        String adminToken = createAdministratorToken();
        UserModel technician = saveUser("Bruno Técnico", "bruno@example.com", USER_PASSWORD, UserRole.TECNICO);

        mockMvc.perform(get("/users/{id}", technician.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(technician.getId()))
                .andExpect(jsonPath("$.email").value("bruno@example.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void shouldUpdateUserDataAndRole() throws Exception {
        String adminToken = createAdministratorToken();
        UserModel attendant = saveUser("Ana Atendente", "ana@example.com", USER_PASSWORD, UserRole.ATENDENTE);

        mockMvc.perform(put("/users/{id}", attendant.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Ana Técnica",
                          "email": "ANA.TECNICA@EXAMPLE.COM",
                          "role": "TECNICO"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Técnica"))
                .andExpect(jsonPath("$.email").value("ana.tecnica@example.com"))
                .andExpect(jsonPath("$.role").value("TECNICO"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        String adminToken = createAdministratorToken();
        saveUser("Marina Técnica", "marina@example.com", USER_PASSWORD, UserRole.TECNICO);

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Outra Marina",
                          "email": "MARINA@EXAMPLE.COM",
                          "senha": "senha-outra-segura",
                          "role": "TECNICO"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldResetPasswordAndAuthenticateWithTheNewPassword() throws Exception {
        String adminToken = createAdministratorToken();
        UserModel technician = saveUser("Marina Técnica", "marina@example.com", USER_PASSWORD, UserRole.TECNICO);

        mockMvc.perform(patch("/users/{id}/password", technician.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "senha": "nova-senha-tecnica"
                        }
                        """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "marina@example.com",
                          "senha": "senha-usuario-segura"
                        }
                        """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "marina@example.com",
                          "senha": "nova-senha-tecnica"
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotAllowDeletingOwnAccount() throws Exception {
        UserModel administrator = saveUser("Administrador", "admin@example.com", ADMIN_PASSWORD, UserRole.ADMIN);
        String adminToken = loginAndGetToken("admin@example.com", ADMIN_PASSWORD);

        mockMvc.perform(delete("/users/{id}", administrator.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotAllowChangingTheRoleOfTheLastAdministrator() throws Exception {
        UserModel administrator = saveUser("Administrador", "admin@example.com", ADMIN_PASSWORD, UserRole.ADMIN);
        String adminToken = loginAndGetToken("admin@example.com", ADMIN_PASSWORD);

        mockMvc.perform(put("/users/{id}", administrator.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Administrador",
                          "email": "admin@example.com",
                          "role": "ATENDENTE"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteAnotherUserWhenAnAdministratorRemains() throws Exception {
        String adminToken = createAdministratorToken();
        UserModel technician = saveUser("Marina Técnica", "marina@example.com", USER_PASSWORD, UserRole.TECNICO);

        mockMvc.perform(delete("/users/{id}", technician.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(userRepository.findById(technician.getId())).isEmpty();
    }

    @Test
    void shouldNotAllowDeletingTechnicianWithAssignedServiceOrders() throws Exception {
        String adminToken = createAdministratorToken();
        UserModel technician = saveUser("Marina Técnica", "marina@example.com", USER_PASSWORD, UserRole.TECNICO);
        ClientModel client = clientRepository.save(new ClientModel(null, "Cliente Teste", "(11) 99999-9999",
                "cliente@example.com"));
        ServiceOrderModel serviceOrder = new ServiceOrderModel();
        serviceOrder.setCliente(client);
        serviceOrder.setTecnico(technician);
        serviceOrder.setConsole("PlayStation 5");
        serviceOrder.setDefeitoRelatado("Console não liga");
        serviceOrder.setDataAbertura(LocalDateTime.now());
        serviceOrder.setValor(new BigDecimal("350.00"));
        serviceOrder.setCustoReparo(new BigDecimal("180.00"));
        serviceOrder.setStatus(ServiceOrderStatus.ABERTA);
        serviceOrderRepository.save(serviceOrder);

        mockMvc.perform(delete("/users/{id}", technician.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    private String createAdministratorToken() throws Exception {
        saveUser("Administrador", "admin@example.com", ADMIN_PASSWORD, UserRole.ADMIN);

        return loginAndGetToken("admin@example.com", ADMIN_PASSWORD);
    }

    private UserModel saveUser(String name, String email, String password, UserRole userRole) {
        UserModel userModel = new UserModel();
        userModel.setNome(name);
        userModel.setEmail(email);
        userModel.setSenha(passwordEncoder.encode(password));
        userModel.setRole(userRole);

        return userRepository.save(userModel);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "senha": "%s"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode response = objectMapper.readTree(responseBody);

        return response.get("token").asText();
    }

    private void clearDatabase() {
        serviceOrderStatusHistoryRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        clientRepository.deleteAll();
        userRepository.deleteAll();
    }
}
