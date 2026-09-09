package com.example.nexos.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.nexos.models.ClientModel;
import com.example.nexos.repositories.ClientRepository;
import com.example.nexos.repositories.ServiceOrderRepository;

@SpringBootTest
class ServiceOrderControllerIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        serviceOrderRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldCreateServiceOrder() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));

        mockMvc.perform(post("/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clienteId": %d,
                          "console": "PlayStation 5",
                          "defeitoRelatado": "O console não liga",
                          "analiseTecnico": "Fonte em análise",
                          "diagnostico": "Possível falha na fonte",
                          "valor": 350.00,
                          "custoReparo": 180.00
                        }
                        """.formatted(clientModel.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.clienteId").value(clientModel.getId()))
                .andExpect(jsonPath("$.console").value("PlayStation 5"))
                .andExpect(jsonPath("$.defeitoRelatado").value("O console não liga"))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.dataAbertura").exists())
                .andExpect(jsonPath("$.valor").exists())
                .andExpect(jsonPath("$.custoReparo").exists());
    }

    @Test
    void shouldRejectInvalidServiceOrder() throws Exception {
        mockMvc.perform(post("/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clienteId": null,
                          "console": "",
                          "defeitoRelatado": "",
                          "valor": -1,
                          "custoReparo": -1
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
        mockMvc.perform(post("/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clienteId": 999,
                          "console": "PlayStation 5",
                          "defeitoRelatado": "O console não liga"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Cliente com id 999 não foi encontrado"));
    }

}
