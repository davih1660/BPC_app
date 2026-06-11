package com.bpc.escola.service;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.ReservaEmbarcacao;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.AgendaDTO;
import com.bpc.escola.repository.HorarioColetivoRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.ReservaEmbarcacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendaService {

    private final HorarioColetivoRepository horarioRepository;
    private final ReservaColetivaRepository reservaColetivaRepository;
    private final ReservaEmbarcacaoRepository reservaEmbarcacaoRepository;

    public AgendaDTO obter(LocalDate de, LocalDate ate) {
        List<AgendaDTO.EventoAgenda> eventos = new ArrayList<>();
        LocalDate cursor = de;
        while (!cursor.isAfter(ate)) {
            var dia = DiaSemanaUtil.fromLocalDate(cursor);
            for (HorarioColetivo horario : horarioRepository.findByDiaSemana(dia)) {
                List<ReservaColetiva> inscritos = reservaColetivaRepository.findByHorarioAndDataReservaAndStatus(
                        horario, cursor, StatusReserva.CONFIRMADA);
                eventos.add(new AgendaDTO.EventoAgenda(
                        "HORARIO_COLETIVO",
                        horario.getId(),
                        horario.getTitulo(),
                        horario.getDiaSemana(),
                        cursor,
                        horario.getHorarioInicio(),
                        horario.getHorarioFim(),
                        null,
                        null,
                        null,
                        inscritos.size(),
                        horario.getCapacidadeSlot()
                ));
            }
            for (ReservaEmbarcacao r : reservaEmbarcacaoRepository.findByData(cursor)) {
                if (r.getStatus() != StatusReserva.CONFIRMADA) continue;
                eventos.add(new AgendaDTO.EventoAgenda(
                        "RESERVA_EMBARCACAO",
                        r.getId(),
                        "Reserva " + r.getEmbarcacao().getNome(),
                        dia,
                        cursor,
                        r.getHorarioInicio(),
                        r.getHorarioFim(),
                        r.getEmbarcacao().getNome(),
                        null,
                        r.getAluno().getNome(),
                        0,
                        0
                ));
            }
            cursor = cursor.plusDays(1);
        }
        eventos.sort(Comparator
                .comparing(AgendaDTO.EventoAgenda::data)
                .thenComparing(AgendaDTO.EventoAgenda::horarioInicio)
                .thenComparing(AgendaDTO.EventoAgenda::horarioFim)
                .thenComparing(AgendaDTO.EventoAgenda::id));
        return new AgendaDTO(de, ate, eventos);
    }
}
