package com.example.nexos.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nexos.dtos.FinancialReportDTO;
import com.example.nexos.dtos.FinancialReportFilterDTO;
import com.example.nexos.services.FinancialReportService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    public FinancialReportController(FinancialReportService financialReportService) {
        this.financialReportService = financialReportService;
    }

    @GetMapping("/financial")
    public ResponseEntity<FinancialReportDTO> generate(@Valid @ModelAttribute FinancialReportFilterDTO filterDTO) {
        FinancialReportDTO financialReport = financialReportService.generate(filterDTO);

        return ResponseEntity.ok(financialReport);
    }
}
