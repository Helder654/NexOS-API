package com.example.nexos.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.nexos.models.ServiceOrderStatus;
import com.example.nexos.models.ServiceOrderModel;
import com.example.nexos.repositories.projections.FinancialSummaryProjection;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrderModel, Long>, JpaSpecificationExecutor<ServiceOrderModel> {

    @Override
    @EntityGraph(attributePaths = { "cliente", "tecnico" })
    Page<ServiceOrderModel> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "cliente", "tecnico" })
    Page<ServiceOrderModel> findAll(Specification<ServiceOrderModel> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "cliente", "tecnico" })
    Optional<ServiceOrderModel> findById(Long id);

    boolean existsByTecnicoId(Long tecnicoId);

    @Query("""
            SELECT COUNT(serviceOrder) AS quantidadeOrdensFinalizadas,
                   COALESCE(SUM(serviceOrder.valor), 0) AS faturamentoTotal,
                   COALESCE(SUM(serviceOrder.custoReparo), 0) AS custoTotal
            FROM ServiceOrderModel serviceOrder
            WHERE serviceOrder.status = :status
              AND serviceOrder.dataFinalizacao >= :dataInicial
              AND serviceOrder.dataFinalizacao < :dataFinalExclusiva
            """)
    FinancialSummaryProjection summarizeFinancialResults(
            @Param("status") ServiceOrderStatus status,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinalExclusiva") LocalDateTime dataFinalExclusiva);

}
