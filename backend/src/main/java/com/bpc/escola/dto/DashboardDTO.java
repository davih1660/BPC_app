package com.bpc.escola.dto;

import java.util.List;

public record DashboardDTO(
        OperacaoHorarioSlotDTO destaque,
        ProximasAulasOperacionaisDTO proximasAulas,
        List<EmbarcacaoDTO> embarcacoesDisponiveis,
        List<OcorrenciaDTO> ocorrenciasAbertas,
        List<HorarioLotadoDTO> horariosLotados,
        long alunosNoDia
) {
    public record HorarioLotadoDTO(Long horarioId, String titulo, LocalDateInfo data, int inscritos, int capacidade) {
    }

    public record LocalDateInfo(String data) {
    }
}
