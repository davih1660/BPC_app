package com.bpc.escola.dto;

import com.bpc.escola.domain.enums.DiaSemana;
import com.bpc.escola.domain.enums.StatusEmbarcacao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AgendaDTO(
        LocalDate dataInicio,
        LocalDate dataFim,
        List<EventoAgenda> eventos
) {
    public record EventoAgenda(
            String tipo,
            Long id,
            String titulo,
            DiaSemana diaSemana,
            LocalDate data,
            LocalTime horarioInicio,
            LocalTime horarioFim,
            String embarcacaoNome,
            StatusEmbarcacao statusEmbarcacao,
            String alunoNome,
            int inscritos,
            int capacidade
    ) {
    }
}
