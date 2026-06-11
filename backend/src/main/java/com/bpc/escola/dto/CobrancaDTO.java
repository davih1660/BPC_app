package com.bpc.escola.dto;

import com.bpc.escola.domain.Cobranca;
import com.bpc.escola.domain.enums.StatusCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CobrancaDTO(
        Long id,
        Long alunoId,
        String alunoNome,
        Long planoId,
        String planoNome,
        BigDecimal valor,
        LocalDate vencimento,
        StatusCobranca status,
        LocalDateTime pagoEm
) {
    public static CobrancaDTO from(Cobranca c) {
        return new CobrancaDTO(
                c.getId(),
                c.getAluno().getId(),
                c.getAluno().getNome(),
                c.getPlano() != null ? c.getPlano().getId() : null,
                c.getPlano() != null ? c.getPlano().getNome() : null,
                c.getValor(),
                c.getVencimento(),
                c.getStatus(),
                c.getPagoEm()
        );
    }
}
