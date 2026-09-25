package com.example.nexos.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import com.example.nexos.models.ServiceOrderModel;

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

}
