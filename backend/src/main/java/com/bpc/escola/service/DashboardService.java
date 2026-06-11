package com.bpc.escola.service;

import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.enums.StatusEmbarcacao;
import com.bpc.escola.domain.enums.StatusOcorrencia;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.*;
import com.bpc.escola.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ReservaColetivaRepository reservaColetivaRepository;
    private final EmbarcacaoRepository embarcacaoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final HorarioColetivoRepository horarioRepository;
    private final DisponibilidadeEmbarcacaoService disponibilidadeService;
    private final HorarioOperacionalService horarioOperacionalService;

    public DashboardDTO obter() {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        LocalTime inicio = LocalTime.of(6, 0);
        LocalTime fim = LocalTime.of(20, 0);

        OperacaoHorarioSlotDTO destaque = horarioOperacionalService.obterOperacaoAtual();
        ProximasAulasOperacionaisDTO proximas = horarioOperacionalService.obterProximosBlocos(hoje);

        List<EmbarcacaoDTO> disponiveis = new ArrayList<>();
        for (Embarcacao e : embarcacaoRepository.findAll()) {
            StatusEmbarcacao status = disponibilidadeService.calcularStatusEfetivo(e, hoje, inicio, fim);
            if (status == StatusEmbarcacao.DISPONIVEL) {
                disponiveis.add(EmbarcacaoDTO.from(e, status));
            }
        }

        List<OcorrenciaDTO> ocorrencias = ocorrenciaRepository.findByStatus(StatusOcorrencia.ABERTA)
                .stream().map(OcorrenciaDTO::from).toList();

        List<DashboardDTO.HorarioLotadoDTO> lotados = new ArrayList<>();
        for (HorarioColetivo horario : horarioRepository.findByDiaSemana(DiaSemanaUtil.fromLocalDate(hoje))) {
            List<ReservaColetiva> inscritos = reservaColetivaRepository.findByHorarioAndDataReservaAndStatus(
                    horario, hoje, StatusReserva.CONFIRMADA);
            if (inscritos.size() >= horario.getCapacidadeSlot()) {
                lotados.add(new DashboardDTO.HorarioLotadoDTO(
                        horario.getId(),
                        horario.getTitulo(),
                        new DashboardDTO.LocalDateInfo(hoje.toString()),
                        inscritos.size(),
                        horario.getCapacidadeSlot()
                ));
            }
        }

        long alunosNoDia = reservaColetivaRepository.findByDataReserva(hoje).stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA)
                .map(r -> r.getAluno().getId())
                .distinct()
                .count();

        return new DashboardDTO(destaque, proximas, disponiveis, ocorrencias, lotados, alunosNoDia);
    }
}
