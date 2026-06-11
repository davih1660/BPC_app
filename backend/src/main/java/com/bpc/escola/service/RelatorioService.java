package com.bpc.escola.service;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.enums.StatusCobranca;
import com.bpc.escola.domain.enums.StatusListaEspera;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.RelatorioResumoDTO;
import com.bpc.escola.repository.CobrancaRepository;
import com.bpc.escola.repository.HorarioColetivoRepository;
import com.bpc.escola.repository.ListaEsperaRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioService {

    private final ReservaColetivaRepository reservaRepository;
    private final HorarioColetivoRepository horarioRepository;
    private final CobrancaRepository cobrancaRepository;
    private final ListaEsperaRepository listaEsperaRepository;

    public RelatorioResumoDTO resumo(LocalDate de, LocalDate ate) {
        List<ReservaColetiva> reservas = reservaRepository.findAll().stream()
                .filter(r -> !r.getDataReserva().isBefore(de) && !r.getDataReserva().isAfter(ate))
                .toList();

        List<ReservaColetiva> confirmadas = reservas.stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA)
                .toList();

        long totalInscritos = 0;
        long totalCapacidade = 0;
        List<RelatorioResumoDTO.OcupacaoHorarioDTO> porHorario = new ArrayList<>();

        for (HorarioColetivo h : horarioRepository.findAll()) {
            long inscritos = confirmadas.stream()
                    .filter(r -> r.getHorario().getId().equals(h.getId()))
                    .count();
            long diasNoPeriodo = de.datesUntil(ate.plusDays(1))
                    .filter(d -> d.getDayOfWeek().getValue() == diaSemanaValor(h.getDiaSemana()))
                    .count();
            long cap = diasNoPeriodo * h.getCapacidadeSlot();
            totalInscritos += inscritos;
            totalCapacidade += cap;
            double pct = cap > 0 ? (inscritos * 100.0 / cap) : 0;
            porHorario.add(new RelatorioResumoDTO.OcupacaoHorarioDTO(
                    h.getId(), h.getTitulo(), inscritos, cap, Math.round(pct * 10) / 10.0));
        }

        double ocupacaoMedia = totalCapacidade > 0 ? (totalInscritos * 100.0 / totalCapacidade) : 0;

        long noShow = reservas.stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA)
                .filter(r -> Boolean.FALSE.equals(r.getPresente()))
                .filter(this::aulaJaPassou)
                .count();

        Set<Long> alunosAtivos = new HashSet<>();
        confirmadas.forEach(r -> alunosAtivos.add(r.getAluno().getId()));

        BigDecimal receita = cobrancaRepository.findByStatusAndPagoEmBetween(
                StatusCobranca.PAGO,
                de.atStartOfDay(),
                ate.atTime(LocalTime.MAX)).stream()
                .map(c -> c.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long entradasLista = listaEsperaRepository.findAll().stream()
                .filter(le -> !le.getDataReserva().isBefore(de) && !le.getDataReserva().isAfter(ate))
                .count();

        long promocoes = listaEsperaRepository.findAll().stream()
                .filter(le -> le.getStatus() == StatusListaEspera.PROMOVIDO)
                .filter(le -> !le.getDataReserva().isBefore(de) && !le.getDataReserva().isAfter(ate))
                .count();

        return new RelatorioResumoDTO(
                new RelatorioResumoDTO.LocalDateRange(de.toString(), ate.toString()),
                Math.round(ocupacaoMedia * 10) / 10.0,
                noShow,
                receita,
                alunosAtivos.size(),
                entradasLista,
                promocoes,
                porHorario
        );
    }

    private boolean aulaJaPassou(ReservaColetiva r) {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        if (r.getDataReserva().isBefore(hoje)) return true;
        if (r.getDataReserva().isAfter(hoje)) return false;
        return RelogioSaoPaulo.hora().isAfter(r.getHorario().getHorarioFim());
    }

    private int diaSemanaValor(com.bpc.escola.domain.enums.DiaSemana dia) {
        return switch (dia) {
            case SEGUNDA -> 1;
            case TERCA -> 2;
            case QUARTA -> 3;
            case QUINTA -> 4;
            case SEXTA -> 5;
            case SABADO -> 6;
            case DOMINGO -> 7;
        };
    }
}
