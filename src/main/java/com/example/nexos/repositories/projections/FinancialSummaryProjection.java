package com.example.nexos.repositories.projections;

import java.math.BigDecimal;

public interface FinancialSummaryProjection {

    long getQuantidadeOrdensFinalizadas();

    BigDecimal getFaturamentoTotal();

    BigDecimal getCustoTotal();
}
