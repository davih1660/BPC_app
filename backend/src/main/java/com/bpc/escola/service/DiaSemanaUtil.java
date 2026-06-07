package com.bpc.escola.service;

import com.bpc.escola.domain.enums.DiaSemana;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class DiaSemanaUtil {

    private DiaSemanaUtil() {
    }

    public static DiaSemana fromLocalDate(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    public static LocalDate proximaData(DiaSemana dia, LocalDate aPartirDe) {
        DayOfWeek target = switch (dia) {
            case SEGUNDA -> DayOfWeek.MONDAY;
            case TERCA -> DayOfWeek.TUESDAY;
            case QUARTA -> DayOfWeek.WEDNESDAY;
            case QUINTA -> DayOfWeek.THURSDAY;
            case SEXTA -> DayOfWeek.FRIDAY;
            case SABADO -> DayOfWeek.SATURDAY;
            case DOMINGO -> DayOfWeek.SUNDAY;
        };
        LocalDate d = aPartirDe;
        while (d.getDayOfWeek() != target) {
            d = d.plusDays(1);
        }
        return d;
    }
}
