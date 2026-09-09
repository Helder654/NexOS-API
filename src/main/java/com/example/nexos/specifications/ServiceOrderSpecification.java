package com.example.nexos.specifications;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.nexos.dtos.ServiceOrderFilterDTO;
import com.example.nexos.models.ServiceOrderModel;

import jakarta.persistence.criteria.Predicate;

public final class ServiceOrderSpecification {

    private ServiceOrderSpecification() {
    }

    public static Specification<ServiceOrderModel> withFilters(ServiceOrderFilterDTO serviceOrderFilterDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (serviceOrderFilterDTO.getClienteId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("cliente").get("id"), serviceOrderFilterDTO.getClienteId()));
            }

            if (serviceOrderFilterDTO.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), serviceOrderFilterDTO.getStatus()));
            }

            if (serviceOrderFilterDTO.getDataAberturaInicial() != null) {
                LocalDateTime dataInicial = serviceOrderFilterDTO.getDataAberturaInicial().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataAbertura"), dataInicial));
            }

            if (serviceOrderFilterDTO.getDataAberturaFinal() != null) {
                LocalDateTime proximoDia = serviceOrderFilterDTO.getDataAberturaFinal().plusDays(1).atStartOfDay();
                predicates.add(criteriaBuilder.lessThan(root.get("dataAbertura"), proximoDia));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

}
