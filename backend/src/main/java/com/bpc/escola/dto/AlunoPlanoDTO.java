package com.bpc.escola.dto;

import com.bpc.escola.domain.AlunoPlano;

import java.time.LocalDate;

public record AlunoPlanoDTO(
        Long id,
        Long alunoId,
        String alunoNome,
        PlanoDTO plano,
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer aulasConsumidasSemana,
        Integer remadasConsumidas,
        Boolean ativo
) {
    public static AlunoPlanoDTO from(AlunoPlano ap) {
        return new AlunoPlanoDTO(
                ap.getId(),
                ap.getAluno().getId(),
                ap.getAluno().getNome(),
                PlanoDTO.from(ap.getPlano()),
                ap.getDataInicio(),
                ap.getDataFim(),
                ap.getAulasConsumidasSemana(),
                ap.getRemadasConsumidas(),
                ap.getAtivo()
        );
    }
}
