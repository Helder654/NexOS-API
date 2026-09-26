package com.example.nexos.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.nexos.models.ClientModel;
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.repositories.ClientRepository;
import com.example.nexos.repositories.ServiceOrderRepository;
import com.example.nexos.repositories.ServiceOrderStatusHistoryRepository;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WithMockUser(roles = "ADMIN")
class FinancialReportControllerIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ServiceOrderStatusHistoryRepository serviceOrderStatusHistoryRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        serviceOrderStatusHistoryRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldReturnFinancialSummaryForCompletedServiceOrdersInThePeriod() throws Exception {
        ClientModel client = clientRepository.save(new ClientModel(null, "Ana Souza", "(11) 99999-9999",
                "ana.souza@example.com"));
        saveCompletedServiceOrder(client, LocalDateTime.of(2026, 1, 10, 10, 0), "350.00", "180.00");
        saveCompletedServiceOrder(client, LocalDateTime.of(2026, 1, 31, 23, 59), "420.00", "210.00");
        saveCompletedServiceOrder(client, LocalDateTime.of(2026, 2, 1, 0, 0), "999.00", "100.00");
        saveOpenServiceOrder(client);

        mockMvc.perform(get("/reports/financial?dataInicial=2026-01-01&dataFinal=2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicial").value("2026-01-01"))
                .andExpect(jsonPath("$.dataFinal").value("2026-01-31"))
                .andExpect(jsonPath("$.quantidadeOrdensFinalizadas").value(2))
                .andExpect(jsonPath("$.faturamentoTotal").value(770.00))
                .andExpect(jsonPath("$.custoTotal").value(390.00))
                .andExpect(jsonPath("$.lucroTotal").value(380.00));
    }

    @Test
    void shouldReturnZeroTotalsWhenThePeriodHasNoCompletedServiceOrders() throws Exception {
        mockMvc.perform(get("/reports/financial?dataInicial=2026-03-01&dataFinal=2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeOrdensFinalizadas").value(0))
                .andExpect(jsonPath("$.faturamentoTotal").value(0))
                .andExpect(jsonPath("$.custoTotal").value(0))
                .andExpect(jsonPath("$.lucroTotal").value(0));
    }

    @Test
    void shouldRejectFinancialReportWithInvertedDates() throws Exception {
        mockMvc.perform(get("/reports/financial?dataInicial=2026-02-01&dataFinal=2026-01-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Filtro financeiro inválido"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void shouldNotAllowAttendantToAccessFinancialReport() throws Exception {
        mockMvc.perform(get("/reports/financial?dataInicial=2026-01-01&dataFinal=2026-01-31"))
                .andExpect(status().isForbidden());
    }

    private void saveCompletedServiceOrder(ClientModel client, LocalDateTime completionDate, String value, String cost) {
        ServiceOrderModel serviceOrder = new ServiceOrderModel();
        serviceOrder.setCliente(client);
        serviceOrder.setConsole("PlayStation 5");
        serviceOrder.setDefeitoRelatado("Console não liga");
        serviceOrder.setDataAbertura(completionDate.minusDays(2));
        serviceOrder.setDataFinalizacao(completionDate);
        serviceOrder.setValor(new BigDecimal(value));
        serviceOrder.setCustoReparo(new BigDecimal(cost));
        serviceOrder.setStatus(ServiceOrderStatus.FINALIZADA);

        serviceOrderRepository.save(serviceOrder);
    }

    private void saveOpenServiceOrder(ClientModel client) {
        ServiceOrderModel serviceOrder = new ServiceOrderModel();
        serviceOrder.setCliente(client);
        serviceOrder.setConsole("Nintendo Switch");
        serviceOrder.setDefeitoRelatado("Joy-Con desconecta");
        serviceOrder.setDataAbertura(LocalDateTime.of(2026, 1, 20, 10, 0));
        serviceOrder.setValor(new BigDecimal("900.00"));
        serviceOrder.setCustoReparo(new BigDecimal("250.00"));
        serviceOrder.setStatus(ServiceOrderStatus.ABERTA);

        serviceOrderRepository.save(serviceOrder);
    }
}
