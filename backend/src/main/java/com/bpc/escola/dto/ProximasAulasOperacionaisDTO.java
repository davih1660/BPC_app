package com.bpc.escola.dto;

public record ProximasAulasOperacionaisDTO(
        BlocoHorarioOperacaoDTO imediato,
        BlocoHorarioOperacaoDTO seguinte
) {
}
