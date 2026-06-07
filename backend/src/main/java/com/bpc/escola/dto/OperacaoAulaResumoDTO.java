package com.bpc.escola.dto;

public record OperacaoAulaResumoDTO(
        AulaDTO aula,
        int totalInscritos,
        int capacidade,
        boolean lotada
) {
    public static OperacaoAulaResumoDTO from(OperacaoAulaDTO slot) {
        return new OperacaoAulaResumoDTO(
                slot.aula(),
                slot.totalInscritos(),
                slot.capacidade(),
                slot.lotada()
        );
    }
}
