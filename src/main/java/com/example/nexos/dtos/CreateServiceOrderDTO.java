package com.example.nexos.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateServiceOrderDTO {

    @NotNull(message = "O cliente é obrigatório")
    private Long clienteId;

    @NotBlank(message = "O console é obrigatório")
    @Size(max = 100, message = "O console deve ter no máximo 100 caracteres")
    private String console;

    @NotBlank(message = "O defeito relatado é obrigatório")
    @Size(max = 1000, message = "O defeito relatado deve ter no máximo 1000 caracteres")
    private String defeitoRelatado;

    @Size(max = 2000, message = "A análise técnica deve ter no máximo 2000 caracteres")
    private String analiseTecnico;

    @Size(max = 2000, message = "O diagnóstico deve ter no máximo 2000 caracteres")
    private String diagnostico;

    @PositiveOrZero(message = "O valor não pode ser negativo")
    @Digits(integer = 10, fraction = 2, message = "O valor deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal valor;

    @PositiveOrZero(message = "O custo de reparo não pode ser negativo")
    @Digits(integer = 10, fraction = 2, message = "O custo de reparo deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal custoReparo;

}
