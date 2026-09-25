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

import com.example.nexos.models.ClientModel;
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.models.UserModel;
import com.example.nexos.models.UserRole;
import com.example.nexos.repositories.ClientRepository;
import com.example.nexos.repositories.ServiceOrderRepository;
import com.example.nexos.repositories.ServiceOrderStatusHistoryRepository;
import com.example.nexos.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
class AuthenticationControllerIntegrationTests {

    private static final String PASSWORD = "senha-segura-123";

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
    void shouldAuthenticateUserAndReturnJwt() throws Exception {
        saveUser("Ana Atendente", "ana@example.com", UserRole.ATENDENTE);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "ana@example.com",
                          "senha": "senha-segura-123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        saveUser("Ana Atendente", "ana@example.com", UserRole.ATENDENTE);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "ana@example.com",
                          "senha": "senha-incorreta"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Credenciais inválidas"));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAttendantToCreateClientButNotUpdateServiceOrderStatus() throws Exception {
        saveUser("Ana Atendente", "ana@example.com", UserRole.ATENDENTE);
        String token = loginAndGetToken("ana@example.com", PASSWORD);

        mockMvc.perform(post("/clients")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Cliente Teste",
                          "telefone": "(11) 99999-9999",
                          "email": "cliente@example.com"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/service-orders/{id}/status", 999L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "EM_ANALISE"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowTechnicianToUpdateServiceOrderStatus() throws Exception {
        UserModel technician = saveUser("Técnico Teste", "tecnico@example.com", UserRole.TECNICO);
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Cliente Teste", "(11) 99999-9999",
                "cliente@example.com"));
        ServiceOrderModel serviceOrderModel = new ServiceOrderModel();
        serviceOrderModel.setCliente(clientModel);
        serviceOrderModel.setConsole("PlayStation 5");
        serviceOrderModel.setDefeitoRelatado("Console não liga");
        serviceOrderModel.setDataAbertura(LocalDateTime.now());
        serviceOrderModel.setValor(new BigDecimal("350.00"));
        serviceOrderModel.setCustoReparo(new BigDecimal("180.00"));
        serviceOrderModel.setStatus(ServiceOrderStatus.ABERTA);
        serviceOrderModel.setTecnico(technician);
        serviceOrderModel = serviceOrderRepository.save(serviceOrderModel);
        String token = loginAndGetToken("tecnico@example.com", PASSWORD);

        mockMvc.perform(patch("/service-orders/{id}/status", serviceOrderModel.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "EM_ANALISE"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    private UserModel saveUser(String name, String email, UserRole userRole) {
        UserModel userModel = new UserModel();
        userModel.setNome(name);
        userModel.setEmail(email);
        userModel.setSenha(passwordEncoder.encode(PASSWORD));
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
