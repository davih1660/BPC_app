package com.bpc.escola.service;

import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.ReservaEmbarcacao;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.CreateReservaEmbarcacaoRequest;
import com.bpc.escola.dto.ReservaEmbarcacaoDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.EmbarcacaoRepository;
import com.bpc.escola.repository.ReservaEmbarcacaoRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservaEmbarcacaoService {

    private final ReservaEmbarcacaoRepository reservaEmbarcacaoRepository;
    private final EmbarcacaoRepository embarcacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DisponibilidadeEmbarcacaoService disponibilidadeService;
    private final PlanoValidacaoService planoValidacaoService;
    private final ReservaColetivaService reservaColetivaService;

    @Transactional
    public ReservaEmbarcacaoDTO criar(CreateReservaEmbarcacaoRequest request) {
        throw new BusinessException(
                "Reserva direta de embarcação não está disponível. Use solicitação de uso livre ou aprovação pelo staff.",
                "RESERVA_DIRETA_BLOQUEADA");
    }

    @Transactional
    public ReservaEmbarcacaoDTO criarAposAprovacao(CreateReservaEmbarcacaoRequest request) {
        return salvarReserva(request);
    }

    @Transactional
    public ReservaEmbarcacaoDTO criarPorStaff(CreateReservaEmbarcacaoRequest request) {
        return salvarReserva(request);
    }

    public ReservaEmbarcacao buscarEntidade(Long id) {
        return reservaEmbarcacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva não encontrada.", "RESERVA_NAO_ENCONTRADA"));
    }

    private ReservaEmbarcacaoDTO salvarReserva(CreateReservaEmbarcacaoRequest request) {
        Embarcacao embarcacao = embarcacaoRepository.findById(request.embarcacaoId())
                .orElseThrow(() -> new BusinessException("Embarcação não encontrada.", "EMBARCACAO_NAO_ENCONTRADA"));
        Usuario aluno = usuarioRepository.findById(request.alunoId())
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        if (!request.horarioFim().isAfter(request.horarioInicio())) {
            throw new BusinessException("Horário fim deve ser após início.", "HORARIO_INVALIDO");
        }

        disponibilidadeService.validarPodeReservar(embarcacao, request.data(), request.horarioInicio(), request.horarioFim());
        validarOverlap(embarcacao, request);
        reservaColetivaService.validarAlunoPodeReservarEmbarcacao(aluno);
        planoValidacaoService.validarReservaEmbarcacao(aluno);

        ReservaEmbarcacao reserva = ReservaEmbarcacao.builder()
                .aluno(aluno)
                .embarcacao(embarcacao)
                .data(request.data())
                .horarioInicio(request.horarioInicio())
                .horarioFim(request.horarioFim())
                .status(StatusReserva.CONFIRMADA)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build();

        ReservaEmbarcacao salva = reservaEmbarcacaoRepository.save(reserva);
        planoValidacaoService.consumirRemada(aluno);
        return ReservaEmbarcacaoDTO.from(salva);
    }

    @Transactional
    public void cancelar(Long id) {
        ReservaEmbarcacao reserva = reservaEmbarcacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva não encontrada.", "RESERVA_NAO_ENCONTRADA"));
        LocalDateTime inicio = LocalDateTime.of(reserva.getData(), reserva.getHorarioInicio());
        if (RelogioSaoPaulo.dataHora().isAfter(inicio.minusHours(1))) {
            throw new BusinessException("Cancelamento permitido apenas até 1h antes.", "CANCELAMENTO_TARDE");
        }
        reserva.setStatus(StatusReserva.CANCELADA);
        reservaEmbarcacaoRepository.save(reserva);
    }

    public List<ReservaEmbarcacaoDTO> listar(Long alunoId, StatusReserva status, LocalDate data) {
        List<ReservaEmbarcacao> lista;
        if (data != null) {
            lista = reservaEmbarcacaoRepository.findByData(data).stream()
                    .filter(r -> status == null || r.getStatus() == status)
                    .filter(r -> alunoId == null || r.getAluno().getId().equals(alunoId))
                    .toList();
        } else if (alunoId != null && status != null) {
            Usuario aluno = usuarioRepository.findById(alunoId).orElseThrow();
            lista = reservaEmbarcacaoRepository.findByAlunoAndStatus(aluno, status);
        } else if (alunoId != null) {
            Usuario aluno = usuarioRepository.findById(alunoId).orElseThrow();
            lista = reservaEmbarcacaoRepository.findByAluno(aluno);
        } else if (status != null) {
            lista = reservaEmbarcacaoRepository.findByStatus(status);
        } else {
            lista = reservaEmbarcacaoRepository.findAll();
        }
        return lista.stream().map(ReservaEmbarcacaoDTO::from).toList();
    }

    private void validarOverlap(Embarcacao embarcacao, CreateReservaEmbarcacaoRequest request) {
        if (!reservaEmbarcacaoRepository.findOverlapping(
                embarcacao, request.data(), request.horarioInicio(), request.horarioFim(),
                StatusReserva.CONFIRMADA).isEmpty()) {
            throw new BusinessException("Já existe reserva neste horário.", "RESERVA_CONFLITO_HORARIO");
        }
    }
}
