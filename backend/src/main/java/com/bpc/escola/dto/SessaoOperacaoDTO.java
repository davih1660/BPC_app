package com.bpc.escola.dto;

import com.bpc.escola.domain.enums.EstadoSessao;

import java.time.LocalDate;
import java.util.List;

public record SessaoOperacaoDTO(
        Long sessaoId,
        HorarioColetivoDTO horario,
        LocalDate data,
        EstadoSessao estado,
        List<ReservaColetivaDTO> reservas,
        List<ReservaColetivaDTO> presentes,
        List<Long> professorIdsEscalados,
        List<GrupoCanoaDTO> grupos,
        int totalInscritos,
        int capacidade,
        boolean lotada,
        SugestaoCanoaDTO sugestaoGeral
) {
}
