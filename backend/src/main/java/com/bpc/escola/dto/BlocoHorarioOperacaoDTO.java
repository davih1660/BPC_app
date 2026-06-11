package com.bpc.escola.dto;

import java.time.LocalTime;
import java.util.List;

public record BlocoHorarioOperacaoDTO(
        LocalTime horarioInicio,
        LocalTime horarioFim,
        String statusBloco,
        List<OperacaoHorarioResumoDTO> horarios
) {
}
