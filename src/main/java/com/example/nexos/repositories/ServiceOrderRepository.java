package com.example.nexos.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexos.models.ServiceOrderModel;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrderModel, Long> {

    @Override
    @EntityGraph(attributePaths = "cliente")
    Page<ServiceOrderModel> findAll(Pageable pageable);

}
