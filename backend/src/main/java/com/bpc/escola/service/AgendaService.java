package com.bpc.escola.service;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.ReservaAula;
import com.bpc.escola.domain.ReservaEmbarcacao;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.AgendaDTO;
import com.bpc.escola.repository.AulaRepository;
import com.bpc.escola.repository.ReservaAulaRepository;
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

    private final AulaRepository aulaRepository;
    private final ReservaAulaRepository reservaAulaRepository;
    private final ReservaEmbarcacaoRepository reservaEmbarcacaoRepository;
    private final DisponibilidadeEmbarcacaoService disponibilidadeService;

    public AgendaDTO obter(LocalDate de, LocalDate ate) {
        List<AgendaDTO.EventoAgenda> eventos = new ArrayList<>();
        LocalDate cursor = de;
        while (!cursor.isAfter(ate)) {
            var dia = DiaSemanaUtil.fromLocalDate(cursor);
            for (Aula aula : aulaRepository.findByDiaSemana(dia)) {
                List<ReservaAula> inscritos = reservaAulaRepository.findByAulaAndDataReservaAndStatus(
                        aula, cursor, StatusReserva.CONFIRMADA);
                eventos.add(new AgendaDTO.EventoAgenda(
                        "AULA",
                        aula.getId(),
                        aula.getTitulo(),
                        aula.getDiaSemana(),
                        cursor,
                        aula.getHorarioInicio(),
                        aula.getHorarioFim(),
                        aula.getEmbarcacaoPrincipal().getNome(),
                        disponibilidadeService.calcularStatusEfetivo(
                                aula.getEmbarcacaoPrincipal(), cursor,
                                aula.getHorarioInicio(), aula.getHorarioFim()),
                        null,
                        inscritos.size(),
                        aula.getCapacidadeMaxima()
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
                        disponibilidadeService.calcularStatusEfetivo(
                                r.getEmbarcacao(), cursor, r.getHorarioInicio(), r.getHorarioFim()),
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
