package com.bpc.escola.dto;

public record SaldoPlanoDTO(
        Long alunoId,
        String planoNome,
        String periodo,
        Integer usado,
        Integer limite,
        String descricao
) {}
