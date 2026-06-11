package com.bpc.escola.service;

import com.bpc.escola.domain.Cobranca;
import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusCobranca;
import com.bpc.escola.domain.enums.TipoNotificacao;
import com.bpc.escola.dto.CobrancaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.CobrancaRepository;
import com.bpc.escola.repository.PlanoRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;
    private final NotificacaoService notificacaoService;

    public List<CobrancaDTO> listarTodas() {
        return cobrancaRepository.findAllByOrderByVencimentoDesc().stream().map(CobrancaDTO::from).toList();
    }

    public boolean alunoEstaInadimplente(Long alunoId) {
        Usuario aluno = usuarioRepository.findById(alunoId).orElse(null);
        if (aluno == null) return false;
        if (cobrancaRepository.existsByAlunoAndStatus(aluno, StatusCobranca.INADIMPLENTE)) {
            return true;
        }
        return cobrancaRepository.existsByAlunoAndStatusAndVencimentoBefore(
                aluno, StatusCobranca.PENDENTE, RelogioSaoPaulo.hoje());
    }

    public void validarAlunoNaoInadimplente(Usuario aluno) {
        if (alunoEstaInadimplente(aluno.getId())) {
            throw new BusinessException(
                    "Regularize sua mensalidade para fazer novas reservas.",
                    "ALUNO_INADIMPLENTE");
        }
    }

    @Transactional
    public CobrancaDTO criar(Long alunoId, Long planoId, BigDecimal valor, LocalDate vencimento) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));
        Plano plano = planoId != null ? planoRepository.findById(planoId).orElse(null) : null;
        Cobranca c = cobrancaRepository.save(Cobranca.builder()
                .aluno(aluno)
                .plano(plano)
                .valor(valor)
                .vencimento(vencimento)
                .status(StatusCobranca.PENDENTE)
                .criadoEm(RelogioSaoPaulo.dataHora())
                .build());
        return CobrancaDTO.from(c);
    }

    @Transactional
    public CobrancaDTO marcarPago(Long id) {
        Cobranca c = get(id);
        c.setStatus(StatusCobranca.PAGO);
        c.setPagoEm(RelogioSaoPaulo.dataHora());
        return CobrancaDTO.from(cobrancaRepository.save(c));
    }

    @Transactional
    public CobrancaDTO marcarInadimplente(Long id) {
        Cobranca c = get(id);
        c.setStatus(StatusCobranca.INADIMPLENTE);
        Cobranca salva = cobrancaRepository.save(c);
        notificacaoService.criar(
                c.getAluno().getId(),
                TipoNotificacao.COBRANCA_VENCIDA,
                "Mensalidade em atraso",
                "Sua cobrança de R$ " + c.getValor() + " está inadimplente. Regularize para voltar a reservar.",
                "COBRANCA",
                c.getId());
        return CobrancaDTO.from(salva);
    }

    private Cobranca get(Long id) {
        return cobrancaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cobrança não encontrada.", "COBRANCA_NAO_ENCONTRADA"));
    }
}
