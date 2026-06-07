package com.bpc.escola.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class RelogioSaoPaulo {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private RelogioSaoPaulo() {
    }

    public static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    public static LocalTime hora() {
        return LocalTime.now(FUSO);
    }

    public static LocalDateTime dataHora() {
        return LocalDateTime.now(FUSO);
    }

    public static ZonedDateTime agora() {
        return ZonedDateTime.now(FUSO);
    }

    public static boolean isHoje(LocalDate data) {
        return hoje().equals(data);
    }
}
