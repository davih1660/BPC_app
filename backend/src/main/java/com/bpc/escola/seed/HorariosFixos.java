package com.bpc.escola.seed;

import com.bpc.escola.domain.enums.DiaSemana;

import java.time.LocalTime;
import java.util.*;

public final class HorariosFixos {

    private HorariosFixos() {
    }

    public static Map<DiaSemana, List<LocalTime[]>> horarios() {
        Map<DiaSemana, List<LocalTime[]>> map = new LinkedHashMap<>();
        map.put(DiaSemana.SEGUNDA, slots(
                t(6, 0), t(7, 0), t(8, 0), t(12, 0), t(17, 0)
        ));
        map.put(DiaSemana.TERCA, slots(
                t(6, 0), t(7, 0), t(8, 0), t(9, 0),
                t(12, 30),
                t(17, 30)
        ));
        map.put(DiaSemana.QUARTA, slots(
                t(6, 0), t(7, 0), t(8, 0), t(12, 0), t(17, 0)
        ));
        map.put(DiaSemana.QUINTA, slots(
                t(6, 0), t(7, 0), t(8, 0), t(9, 0),
                t(12, 30),
                t(17, 30)
        ));
        map.put(DiaSemana.SEXTA, slots(
                t(6, 0), t(7, 0), t(8, 0), t(12, 0), t(17, 0)
        ));
        map.put(DiaSemana.SABADO, List.of(
                slot(LocalTime.of(6, 0), LocalTime.of(7, 0)),
                slot(LocalTime.of(7, 30), LocalTime.of(8, 30)),
                slot(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                slot(LocalTime.of(11, 0), LocalTime.of(12, 0)),
                slot(LocalTime.of(16, 20), LocalTime.of(17, 20)),
                slot(LocalTime.of(17, 30), LocalTime.of(18, 30))
        ));
        map.put(DiaSemana.DOMINGO, List.of(
                slot(LocalTime.of(7, 0), LocalTime.of(8, 0)),
                slot(LocalTime.of(8, 30), LocalTime.of(9, 30)),
                slot(LocalTime.of(10, 0), LocalTime.of(11, 0)),
                slot(LocalTime.of(11, 45), LocalTime.of(12, 45)),
                slot(LocalTime.of(15, 0), LocalTime.of(16, 0)),
                slot(LocalTime.of(16, 20), LocalTime.of(17, 20)),
                slot(LocalTime.of(17, 30), LocalTime.of(18, 30))
        ));
        return map;
    }

    private static List<LocalTime[]> slots(LocalTime[]... pairs) {
        return Arrays.asList(pairs);
    }

    private static LocalTime[] t(int h, int m) {
        return slot(LocalTime.of(h, m), LocalTime.of(h + 1, m));
    }

    private static LocalTime[] slot(LocalTime inicio, LocalTime fim) {
        return new LocalTime[]{inicio, fim};
    }
}
