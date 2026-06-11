package com.bpc.escola.dto;

import com.bpc.escola.domain.enums.TipoEmbarcacao;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSolicitacaoUsoLivreRequest(
        @NotNull Long alunoId,
        @NotNull Long horarioId,
        @NotNull LocalDate data,
        @NotNull TipoEmbarcacao tipoCanoaDesejada,
        String observacao
) {
}
