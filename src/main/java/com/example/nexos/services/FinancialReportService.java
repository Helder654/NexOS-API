package com.example.nexos.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.nexos.dtos.FinancialReportDTO;
import com.example.nexos.dtos.FinancialReportFilterDTO;
import com.example.nexos.exceptions.InvalidFinancialReportFilterException;
import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.repositories.ServiceOrderRepository;
import com.example.nexos.repositories.projections.FinancialSummaryProjection;

@Service
public class FinancialReportService {

    private final ServiceOrderRepository serviceOrderRepository;

    public FinancialReportService(ServiceOrderRepository serviceOrderRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
    }

    public FinancialReportDTO generate(FinancialReportFilterDTO financialReportFilterDTO) {
        validateFilter(financialReportFilterDTO);

        LocalDateTime startDate = financialReportFilterDTO.getDataInicial().atStartOfDay();
        LocalDateTime endDateExclusive = financialReportFilterDTO.getDataFinal().plusDays(1).atStartOfDay();
        FinancialSummaryProjection summary = serviceOrderRepository.summarizeFinancialResults(
                ServiceOrderStatus.FINALIZADA, startDate, endDateExclusive);

        BigDecimal grossRevenue = defaultToZero(summary.getFaturamentoTotal());
        BigDecimal repairCost = defaultToZero(summary.getCustoTotal());

        return new FinancialReportDTO(
                financialReportFilterDTO.getDataInicial(),
                financialReportFilterDTO.getDataFinal(),
                summary.getQuantidadeOrdensFinalizadas(),
                grossRevenue,
                repairCost,
                grossRevenue.subtract(repairCost));
    }

    private void validateFilter(FinancialReportFilterDTO financialReportFilterDTO) {
        if (financialReportFilterDTO.getDataInicial().isAfter(financialReportFilterDTO.getDataFinal())) {
            throw new InvalidFinancialReportFilterException(
                    "A data inicial não pode ser posterior à data final");
        }
    }

    private BigDecimal defaultToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
