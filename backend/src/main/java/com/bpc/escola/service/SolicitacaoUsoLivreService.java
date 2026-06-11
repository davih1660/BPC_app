package com.bpc.escola.service;

import com.bpc.escola.domain.*;
import com.bpc.escola.domain.enums.StatusSolicitacaoUsoLivre;
import com.bpc.escola.domain.enums.TipoEmbarcacao;
import com.bpc.escola.domain.enums.TipoNotificacao;
import com.bpc.escola.dto.CreateReservaEmbarcacaoRequest;
import com.bpc.escola.dto.CreateSolicitacaoUsoLivreRequest;
import com.bpc.escola.dto.ReservaEmbarcacaoDTO;
import com.bpc.escola.dto.SolicitacaoUsoLivreDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.EmbarcacaoRepository;
import com.bpc.escola.repository.SolicitacaoUsoLivreRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitacaoUsoLivreService {

    private static final Set<TipoEmbarcacao> TIPOS_USO_LIVRE = EnumSet.of(
            TipoEmbarcacao.OC1, TipoEmbarcacao.OC2, TipoEmbarcacao.OC3,
            TipoEmbarcacao.OC4, TipoEmbarcacao.OC6);

    private final SolicitacaoUsoLivreRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final EmbarcacaoRepository embarcacaoRepository;
    private final HorarioColetivoService horarioService;
    private final ReservaColetivaService reservaColetivaService;
    private final CobrancaService cobrancaService;
    private final BloqueioAgendaService bloqueioAgendaService;
    private final ReservaEmbarcacaoService reservaEmbarcacaoService;
    private final NotificacaoService notificacaoService;

    public List<SolicitacaoUsoLivreDTO> listarPorAluno(Long alunoId) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));
        return repository.findByAlunoOrderByCriadoEmDesc(aluno).stream()
                .map(SolicitacaoUsoLivreDTO::from)
                .toList();
    }

    public List<SolicitacaoUsoLivreDTO> listarPendentes() {
        return repository.findByStatusOrderByCriadoEmAsc(StatusSolicitacaoUsoLivre.PENDENTE).stream()
                .map(SolicitacaoUsoLivreDTO::from)
                .toList();
    }

    public List<SolicitacaoUsoLivreDTO> listarTodas(StatusSolicitacaoUsoLivre status) {
        List<SolicitacaoUsoLivre> lista = status != null
                ? repository.findByStatusOrderByCriadoEmAsc(status)
                : repository.findByStatusInOrderByCriadoEmDesc(List.of(StatusSolicitacaoUsoLivre.values()));
        return lista.stream().map(SolicitacaoUsoLivreDTO::from).toList();
    }

    @Transactional
    public SolicitacaoUsoLivreDTO criar(CreateSolicitacaoUsoLivreRequest request) {
        if (!TIPOS_USO_LIVRE.contains(request.tipoCanoaDesejada())) {
            throw new BusinessException("Tipo de canoa inválido para uso livre.", "TIPO_CANOA_INVALIDO");
        }

        HorarioColetivo horario = horarioService.get(request.horarioId());
        Usuario aluno = usuarioRepository.findById(request.alunoId())
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        reservaColetivaService.validarAlunoPodeReservarEmbarcacao(aluno);
        cobrancaService.validarAlunoNaoInadimplente(aluno);
        bloqueioAgendaService.validarNaoBloqueado(request.data(), horario);
        validarDiaSemana(horario, request.data());
        validarDataFutura(request.data(), horario);

        if (repository.existsByAlunoAndHorarioAndDataAndStatus(
                aluno, horario, request.data(), StatusSolicitacaoUsoLivre.PENDENTE)) {
            throw new BusinessException("Você já possui uma solicitação pendente neste horário.", "SOLICITACAO_DUPLICADA");
        }

        SolicitacaoUsoLivre salva = repository.save(SolicitacaoUsoLivre.builder()
                .aluno(aluno)
                .horario(horario)
                .data(request.data())
                .tipoCanoaDesejada(request.tipoCanoaDesejada())
                .observacao(request.observacao())
                .status(StatusSolicitacaoUsoLivre.PENDENTE)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build());

        return SolicitacaoUsoLivreDTO.from(salva);
    }

    @Transactional
    public SolicitacaoUsoLivreDTO aprovar(Long id, Long embarcacaoId, Long processadoPorId) {
        SolicitacaoUsoLivre solicitacao = getPendente(id);
        Embarcacao embarcacao = embarcacaoRepository.findById(embarcacaoId)
                .orElseThrow(() -> new BusinessException("Embarcação não encontrada.", "EMBARCACAO_NAO_ENCONTRADA"));
        Usuario processadoPor = usuarioRepository.findById(processadoPorId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado.", "USUARIO_NAO_ENCONTRADO"));

        if (embarcacao.getTipo() != solicitacao.getTipoCanoaDesejada()) {
            throw new BusinessException(
                    "Embarcação deve ser do tipo " + solicitacao.getTipoCanoaDesejada() + ".",
                    "TIPO_CANOA_INCOMPATIVEL");
        }

        HorarioColetivo horario = solicitacao.getHorario();
        ReservaEmbarcacaoDTO reserva = reservaEmbarcacaoService.criarAposAprovacao(new CreateReservaEmbarcacaoRequest(
                solicitacao.getAluno().getId(),
                embarcacao.getId(),
                solicitacao.getData(),
                horario.getHorarioInicio(),
                horario.getHorarioFim()));

        ReservaEmbarcacao reservaEntity = reservaEmbarcacaoService.buscarEntidade(reserva.id());

        solicitacao.setStatus(StatusSolicitacaoUsoLivre.APROVADA);
        solicitacao.setEmbarcacaoAtribuida(embarcacao);
        solicitacao.setReservaEmbarcacao(reservaEntity);
        solicitacao.setProcessadoPor(processadoPor);
        solicitacao.setProcessadoEm(RelogioSaoPaulo.dataHora());

        SolicitacaoUsoLivre salva = repository.save(solicitacao);

        notificacaoService.criar(
                solicitacao.getAluno().getId(),
                TipoNotificacao.USO_LIVRE_APROVADO,
                "Uso livre aprovado",
                "Sua solicitação de " + embarcacao.getNome() + " em "
                        + solicitacao.getData() + " foi aprovada.",
                "SOLICITACAO_USO_LIVRE",
                salva.getId());

        return SolicitacaoUsoLivreDTO.from(salva);
    }

    @Transactional
    public SolicitacaoUsoLivreDTO recusar(Long id, Long processadoPorId, String motivo) {
        SolicitacaoUsoLivre solicitacao = getPendente(id);
        Usuario processadoPor = usuarioRepository.findById(processadoPorId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado.", "USUARIO_NAO_ENCONTRADO"));

        solicitacao.setStatus(StatusSolicitacaoUsoLivre.RECUSADA);
        solicitacao.setMotivoRecusa(motivo != null && !motivo.isBlank() ? motivo.trim() : "Solicitação recusada.");
        solicitacao.setProcessadoPor(processadoPor);
        solicitacao.setProcessadoEm(RelogioSaoPaulo.dataHora());

        SolicitacaoUsoLivre salva = repository.save(solicitacao);

        notificacaoService.criar(
                solicitacao.getAluno().getId(),
                TipoNotificacao.USO_LIVRE_RECUSADO,
                "Uso livre não aprovado",
                salva.getMotivoRecusa(),
                "SOLICITACAO_USO_LIVRE",
                salva.getId());

        return SolicitacaoUsoLivreDTO.from(salva);
    }

    @Transactional
    public void cancelar(Long id, Long alunoId) {
        SolicitacaoUsoLivre solicitacao = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada.", "SOLICITACAO_NAO_ENCONTRADA"));

        if (solicitacao.getStatus() != StatusSolicitacaoUsoLivre.PENDENTE) {
            throw new BusinessException("Apenas solicitações pendentes podem ser canceladas.", "SOLICITACAO_NAO_PENDENTE");
        }
        if (!solicitacao.getAluno().getId().equals(alunoId)) {
            throw new BusinessException("Solicitação não pertence ao aluno.", "SOLICITACAO_NAO_AUTORIZADA");
        }

        solicitacao.setStatus(StatusSolicitacaoUsoLivre.CANCELADA);
        solicitacao.setProcessadoEm(RelogioSaoPaulo.dataHora());
        repository.save(solicitacao);
    }

    private SolicitacaoUsoLivre getPendente(Long id) {
        SolicitacaoUsoLivre solicitacao = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitação não encontrada.", "SOLICITACAO_NAO_ENCONTRADA"));
        if (solicitacao.getStatus() != StatusSolicitacaoUsoLivre.PENDENTE) {
            throw new BusinessException("Solicitação já foi processada.", "SOLICITACAO_JA_PROCESSADA");
        }
        return solicitacao;
    }

    private void validarDiaSemana(HorarioColetivo horario, LocalDate data) {
        if (horario.getDiaSemana() != DiaSemanaUtil.fromLocalDate(data)) {
            throw new BusinessException("Horário não corresponde ao dia da semana.", "HORARIO_DIA_INVALIDO");
        }
    }

    private void validarDataFutura(LocalDate data, HorarioColetivo horario) {
        if (data.isBefore(RelogioSaoPaulo.hoje())) {
            throw new BusinessException("Não é possível solicitar datas passadas.", "DATA_PASSADA");
        }
        if (data.equals(RelogioSaoPaulo.hoje())
                && RelogioSaoPaulo.hora().isAfter(horario.getHorarioInicio())) {
            throw new BusinessException("Horário já iniciou.", "HORARIO_JA_INICIADO");
        }
    }
}
