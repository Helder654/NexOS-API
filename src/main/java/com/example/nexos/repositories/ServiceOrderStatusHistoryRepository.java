package com.example.nexos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexos.models.ServiceOrderStatusHistoryModel;

public interface ServiceOrderStatusHistoryRepository extends JpaRepository<ServiceOrderStatusHistoryModel, Long> {

    @EntityGraph(attributePaths = "serviceOrder")
    List<ServiceOrderStatusHistoryModel> findByServiceOrderIdOrderByDataAlteracaoDesc(Long serviceOrderId);

    void deleteByServiceOrderId(Long serviceOrderId);

}
