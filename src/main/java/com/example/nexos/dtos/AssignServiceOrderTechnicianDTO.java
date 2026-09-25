package com.example.nexos.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignServiceOrderTechnicianDTO {

    @NotNull(message = "O id do técnico é obrigatório")
    private Long tecnicoId;
}
