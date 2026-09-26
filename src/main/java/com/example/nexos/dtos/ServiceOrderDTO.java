package com.example.nexos.dtos;

import java.math.BigDecimal;
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
public class ServiceOrderDTO {

    private Long id;
    private Long clienteId;
    private TechnicianSummaryDTO tecnico;
    private String console;
    private String defeitoRelatado;
    private String analiseTecnico;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFinalizacao;
    private String diagnostico;
    private BigDecimal valor;
    private BigDecimal custoReparo;
    private ServiceOrderStatus status;

}
