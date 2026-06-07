package com.bpc.escola.dto;

import java.util.List;

public record DashboardDTO(
        OperacaoAulaDTO destaque,
        ProximasAulasOperacionaisDTO proximasAulas,
        List<EmbarcacaoDTO> embarcacoesDisponiveis,
        List<OcorrenciaDTO> ocorrenciasAbertas,
        List<AulaLotadaDTO> aulasLotadas,
        long alunosNoDia
) {
    public record AulaLotadaDTO(Long aulaId, String titulo, LocalDateInfo data, int inscritos, int capacidade) {
    }

    public record LocalDateInfo(String data) {
    }
}
