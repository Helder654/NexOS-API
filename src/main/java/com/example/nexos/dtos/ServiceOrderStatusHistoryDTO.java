package com.example.nexos.dtos;

import java.time.LocalDateTime;

import com.example.nexos.models.ServiceOrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderStatusHistoryDTO {

    private Long id;
    private Long serviceOrderId;
    private ServiceOrderStatus statusAnterior;
    private ServiceOrderStatus statusNovo;
    private LocalDateTime dataAlteracao;

}
