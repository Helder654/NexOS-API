package com.example.nexos.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
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
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnPaginatedAndSortedServiceOrders() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel firstServiceOrder = saveServiceOrder(clientModel);
        firstServiceOrder.setConsole("Nintendo Switch");
        serviceOrderRepository.save(firstServiceOrder);
        ServiceOrderModel secondServiceOrder = saveServiceOrder(clientModel);
        secondServiceOrder.setConsole("PlayStation 5");
        serviceOrderRepository.save(secondServiceOrder);
        ServiceOrderModel thirdServiceOrder = saveServiceOrder(clientModel);
        thirdServiceOrder.setConsole("Xbox Series X");
        serviceOrderRepository.save(thirdServiceOrder);

        mockMvc.perform(get("/service-orders?page=1&size=1&sort=console,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].console").value("PlayStation 5"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldFilterServiceOrders() throws Exception {
        ClientModel firstClient = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ClientModel secondClient = clientRepository.save(new ClientModel(null, "Bruno Lima", "(11) 98888-8888",
                "bruno.lima@example.com"));

        ServiceOrderModel matchingServiceOrder = saveServiceOrder(firstClient);
        matchingServiceOrder.setStatus(ServiceOrderStatus.EM_ANALISE);
        matchingServiceOrder.setDataAbertura(LocalDateTime.of(2026, 1, 15, 10, 0));
        serviceOrderRepository.save(matchingServiceOrder);

        ServiceOrderModel differentDateServiceOrder = saveServiceOrder(firstClient);
        differentDateServiceOrder.setStatus(ServiceOrderStatus.EM_ANALISE);
        differentDateServiceOrder.setDataAbertura(LocalDateTime.of(2026, 2, 15, 10, 0));
        serviceOrderRepository.save(differentDateServiceOrder);

        ServiceOrderModel differentClientServiceOrder = saveServiceOrder(secondClient);
        differentClientServiceOrder.setStatus(ServiceOrderStatus.EM_ANALISE);
        differentClientServiceOrder.setDataAbertura(LocalDateTime.of(2026, 1, 20, 10, 0));
        serviceOrderRepository.save(differentClientServiceOrder);

        mockMvc.perform(get("/service-orders?clienteId=%d&status=EM_ANALISE&dataAberturaInicial=%s&dataAberturaFinal=%s"
                .formatted(firstClient.getId(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(matchingServiceOrder.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldRejectServiceOrderFilterWithInvertedDates() throws Exception {
        mockMvc.perform(get("/service-orders?dataAberturaInicial=2026-02-01&dataAberturaFinal=2026-01-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Filtro de ordem inválido"))
                .andExpect(jsonPath("$.detail").value("A data inicial não pode ser posterior à data final"));
    }

    @Test
    void shouldUpdateServiceOrder() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);

        mockMvc.perform(put("/service-orders/{id}", serviceOrderModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "console": "Xbox Series X",
                          "defeitoRelatado": "Desliga durante o jogo",
                          "analiseTecnico": "Sistema de refrigeração revisado",
                          "diagnostico": "Pasta térmica ressecada",
                          "valor": 420.00,
                          "custoReparo": 210.00
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceOrderModel.getId()))
                .andExpect(jsonPath("$.clienteId").value(clientModel.getId()))
                .andExpect(jsonPath("$.console").value("Xbox Series X"))
                .andExpect(jsonPath("$.defeitoRelatado").value("Desliga durante o jogo"))
                .andExpect(jsonPath("$.analiseTecnico").value("Sistema de refrigeração revisado"))
                .andExpect(jsonPath("$.diagnostico").value("Pasta térmica ressecada"))
                .andExpect(jsonPath("$.valor").value(420.00))
                .andExpect(jsonPath("$.custoReparo").value(210.00))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void shouldRejectInvalidServiceOrderUpdate() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);

        mockMvc.perform(put("/service-orders/{id}", serviceOrderModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "console": "",
                          "defeitoRelatado": "",
                          "valor": -1,
                          "custoReparo": -1
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentServiceOrder() throws Exception {
        mockMvc.perform(put("/service-orders/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "console": "Xbox Series X",
                          "defeitoRelatado": "Desliga durante o jogo"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Ordem de serviço com id 999 não foi encontrada"));
    }

    @Test
    void shouldUpdateServiceOrderStatus() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);

        mockMvc.perform(patch("/service-orders/{id}/status", serviceOrderModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "EM_ANALISE"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceOrderModel.getId()))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    @Test
    void shouldRejectInvalidServiceOrderStatusTransition() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);

        mockMvc.perform(patch("/service-orders/{id}/status", serviceOrderModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "FINALIZADA"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Transição de status inválida"))
                .andExpect(jsonPath("$.detail").value("Não é possível alterar o status de ABERTA para FINALIZADA"));
    }

    @Test
    void shouldRejectServiceOrderStatusWithoutValue() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);

        mockMvc.perform(patch("/service-orders/{id}/status", serviceOrderModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": null
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfNonexistentServiceOrder() throws Exception {
        mockMvc.perform(patch("/service-orders/{id}/status", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "EM_ANALISE"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Ordem de serviço com id 999 não foi encontrada"));
    }

    @Test
    void shouldDeleteOpenServiceOrder() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);

        mockMvc.perform(delete("/service-orders/{id}", serviceOrderModel.getId()))
                .andExpect(status().isNoContent());

        assertThat(serviceOrderRepository.existsById(serviceOrderModel.getId())).isFalse();
    }

    @Test
    void shouldDeleteCancelledServiceOrder() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);
        serviceOrderModel.setStatus(ServiceOrderStatus.CANCELADA);
        serviceOrderRepository.save(serviceOrderModel);

        mockMvc.perform(delete("/service-orders/{id}", serviceOrderModel.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDeletionOfServiceOrderInProgress() throws Exception {
        ClientModel clientModel = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        ServiceOrderModel serviceOrderModel = saveServiceOrder(clientModel);
        serviceOrderModel.setStatus(ServiceOrderStatus.EM_ANALISE);
        serviceOrderRepository.save(serviceOrderModel);

        mockMvc.perform(delete("/service-orders/{id}", serviceOrderModel.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Exclusão de ordem não permitida"))
                .andExpect(jsonPath("$.detail").value("A ordem de serviço com status EM_ANALISE não pode ser excluída"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentServiceOrder() throws Exception {
        mockMvc.perform(delete("/service-orders/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Ordem de serviço com id 999 não foi encontrada"));
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
