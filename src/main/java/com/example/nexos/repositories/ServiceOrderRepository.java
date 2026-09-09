package com.example.nexos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexos.models.ServiceOrderModel;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrderModel, Long> {

}
