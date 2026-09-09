package com.example.nexos.dtos;

import com.example.nexos.models.ServiceOrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceOrderStatusDTO {

    @NotNull(message = "O status é obrigatório")
    private ServiceOrderStatus status;

}
