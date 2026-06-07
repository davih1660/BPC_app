package com.bpc.escola.dto;

import java.util.List;

public record OperacaoDiaDTO(
        OperacaoAulaDTO destaque,
        List<OperacaoAulaDTO> slotsDoDia,
        long alunosNoDia
) {
}
