package com.example.nexos.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexos.models.ClientModel;

public interface ClientRepository extends JpaRepository<ClientModel, Long> {
    
}





}
