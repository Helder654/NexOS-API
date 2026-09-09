package com.example.nexos.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.models.ServiceOrderStatus;
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

    @Test
    void shouldReturnServiceOrderById() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);

        mockMvc.perform(get("/service-orders/{id}", serviceOrderModel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceOrderModel.getId()))
                .andExpect(jsonPath("$.clienteId").value(clientModel.getId()))
                .andExpect(jsonPath("$.console").value("PlayStation 5"))
                .andExpect(jsonPath("$.defeitoRelatado").value("O console não liga"))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void shouldReturnNotFoundWhenServiceOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/service-orders/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Ordem de serviço com id 999 não foi encontrada"));
    }

    @Test
    void shouldReturnEmptyServiceOrderList() throws Exception {
        mockMvc.perform(get("/service-orders"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnAllServiceOrders() throws Exception {
        ClientModel firstClient = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ClientModel secondClient = clientRepository.save(new ClientModel(null, "Bruno Lima", "(11) 98888-8888",
                "bruno.lima@example.com"));
        saveServiceOrder(firstClient);
        saveServiceOrder(secondClient);

        mockMvc.perform(get("/service-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private ServiceOrderModel saveServiceOrder(ClientModel clientModel) {
        ServiceOrderModel serviceOrderModel = new ServiceOrderModel();
        serviceOrderModel.setCliente(clientModel);
        serviceOrderModel.setConsole("PlayStation 5");
        serviceOrderModel.setDefeitoRelatado("O console não liga");
        serviceOrderModel.setDataAbertura(LocalDateTime.now());
        serviceOrderModel.setValor(new BigDecimal("350.00"));
        serviceOrderModel.setCustoReparo(new BigDecimal("180.00"));
        serviceOrderModel.setStatus(ServiceOrderStatus.ABERTA);

        return serviceOrderRepository.save(serviceOrderModel);
    }

}
