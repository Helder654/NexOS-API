package com.example.nexos.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FinancialReportDTO {

    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private long quantidadeOrdensFinalizadas;
    private BigDecimal faturamentoTotal;
    private BigDecimal custoTotal;
    private BigDecimal lucroTotal;
}
