package com.example.nexos.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.nexos.models.ServiceOrderStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceOrderFilterDTO {

    private Long clienteId;

    private ServiceOrderStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAberturaInicial;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAberturaFinal;

}
