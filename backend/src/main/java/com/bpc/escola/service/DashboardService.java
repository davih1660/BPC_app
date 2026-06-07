package com.bpc.escola.service;

import com.bpc.escola.domain.Aula;
import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.ReservaAula;
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

    private final ReservaAulaRepository reservaAulaRepository;
    private final EmbarcacaoRepository embarcacaoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final AulaRepository aulaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DisponibilidadeEmbarcacaoService disponibilidadeService;
    private final AulaOperacionalService aulaOperacionalService;

    public DashboardDTO obter() {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        LocalTime inicio = LocalTime.of(6, 0);
        LocalTime fim = LocalTime.of(20, 0);

        OperacaoAulaDTO destaque = aulaOperacionalService.obterOperacaoAtual();

        ProximasAulasOperacionaisDTO proximas = aulaOperacionalService.obterProximosBlocos(hoje);

        List<EmbarcacaoDTO> disponiveis = new ArrayList<>();
        for (Embarcacao e : embarcacaoRepository.findAll()) {
            StatusEmbarcacao status = disponibilidadeService.calcularStatusEfetivo(e, hoje, inicio, fim);
            if (status == StatusEmbarcacao.DISPONIVEL) {
                disponiveis.add(EmbarcacaoDTO.from(e, status));
            }
        }

        List<OcorrenciaDTO> ocorrencias = ocorrenciaRepository.findByStatus(StatusOcorrencia.ABERTA)
                .stream().map(OcorrenciaDTO::from).toList();

        List<DashboardDTO.AulaLotadaDTO> lotadas = new ArrayList<>();
        for (Aula aula : aulaRepository.findByDiaSemana(DiaSemanaUtil.fromLocalDate(hoje))) {
            List<ReservaAula> inscritos = reservaAulaRepository.findByAulaAndDataReservaAndStatus(
                    aula, hoje, StatusReserva.CONFIRMADA);
            if (inscritos.size() >= aula.getCapacidadeMaxima()) {
                lotadas.add(new DashboardDTO.AulaLotadaDTO(
                        aula.getId(),
                        aula.getTitulo(),
                        new DashboardDTO.LocalDateInfo(hoje.toString()),
                        inscritos.size(),
                        aula.getCapacidadeMaxima()
                ));
            }
        }

        long alunosNoDia = reservaAulaRepository.findByDataReserva(hoje).stream()
                .filter(r -> r.getStatus() == StatusReserva.CONFIRMADA)
                .map(r -> r.getAluno().getId())
                .distinct()
                .count();

        return new DashboardDTO(destaque, proximas, disponiveis, ocorrencias, lotadas, alunosNoDia);
    }
}
