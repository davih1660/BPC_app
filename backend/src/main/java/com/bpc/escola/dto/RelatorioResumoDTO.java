package com.bpc.escola.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioResumoDTO(
        LocalDateRange periodo,
        double ocupacaoMediaPercent,
        long totalNoShow,
        BigDecimal receitaPaga,
        long alunosAtivos,
        long entradasListaEspera,
        long promocoesListaEspera,
        List<OcupacaoHorarioDTO> ocupacaoPorHorario
) {
    public record LocalDateRange(String de, String ate) {}

    public record OcupacaoHorarioDTO(
            Long horarioId,
            String titulo,
            long totalInscritos,
            long totalCapacidade,
            double percentual
    ) {}
}
