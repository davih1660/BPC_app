package com.bpc.escola.dto;

import com.bpc.escola.domain.AlunoPlano;
import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.CategoriaPlano;
import com.bpc.escola.domain.enums.SituacaoAluno;

import java.time.LocalDate;

public record AlunoSituacaoDTO(
        Long alunoId,
        String alunoNome,
        String alunoEmail,
        SituacaoAluno situacao,
        Long planoId,
        String planoNome,
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer remadasConsumidas,
        Integer quantidadeRemadas
) {
    public static AlunoSituacaoDTO from(Usuario aluno, AlunoPlano alunoPlano) {
        if (alunoPlano == null || !Boolean.TRUE.equals(alunoPlano.getAtivo())) {
            return semPlano(aluno);
        }
        var plano = alunoPlano.getPlano();
        return new AlunoSituacaoDTO(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                situacaoDe(plano),
                plano.getId(),
                plano.getNome(),
                alunoPlano.getDataInicio(),
                alunoPlano.getDataFim(),
                alunoPlano.getRemadasConsumidas(),
                plano.getQuantidadeRemadas()
        );
    }

    public static AlunoSituacaoDTO semPlano(Usuario aluno) {
        return new AlunoSituacaoDTO(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                SituacaoAluno.SEM_PLANO,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static SituacaoAluno situacaoDe(Plano plano) {
        CategoriaPlano categoria = PlanoDTO.categoriaDe(plano);
        return switch (categoria) {
            case RECORRENTE -> SituacaoAluno.PLANO;
            case WELLHUB -> SituacaoAluno.WELLHUB;
            case AVULSO -> isPacote(plano) ? SituacaoAluno.PACOTE : SituacaoAluno.AVULSO;
            case EQUIPAMENTO -> SituacaoAluno.AVULSO;
        };
    }

    private static boolean isPacote(Plano plano) {
        if (plano.getQuantidadeRemadas() != null && plano.getQuantidadeRemadas() > 1) {
            return true;
        }
        String nome = plano.getNome();
        return nome != null && nome.toLowerCase().startsWith("pacote");
    }
}
