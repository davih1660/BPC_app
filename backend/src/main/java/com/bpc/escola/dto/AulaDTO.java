package com.bpc.escola.dto;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.enums.DiaSemana;

import java.time.LocalTime;

public record AulaDTO(
        Long id,
        String titulo,
        DiaSemana diaSemana,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        Integer capacidadeMaxima,
        Long professorId,
        String professorNome,
        Long embarcacaoPrincipalId,
        String embarcacaoPrincipalNome
) {
    public static AulaDTO from(Aula a) {
        return new AulaDTO(
                a.getId(),
                a.getTitulo(),
                a.getDiaSemana(),
                a.getHorarioInicio(),
                a.getHorarioFim(),
                a.getCapacidadeMaxima(),
                a.getProfessor().getId(),
                a.getProfessor().getNome(),
                a.getEmbarcacaoPrincipal().getId(),
                a.getEmbarcacaoPrincipal().getNome()
        );
    }
}
