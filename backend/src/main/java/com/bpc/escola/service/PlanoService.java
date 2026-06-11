package com.bpc.escola.service;

import com.bpc.escola.domain.AlunoPlano;
import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.SituacaoAluno;
import com.bpc.escola.domain.enums.TipoUsuario;
import com.bpc.escola.dto.AlunoPlanoDTO;
import com.bpc.escola.dto.AlunoSituacaoDTO;
import com.bpc.escola.dto.AtualizarPlanoDTO;
import com.bpc.escola.dto.PlanoDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.AlunoPlanoRepository;
import com.bpc.escola.repository.PlanoRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final AlunoPlanoRepository alunoPlanoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public List<PlanoDTO> listarPlanos() {
        return planoRepository.findAll().stream().map(PlanoDTO::from).toList();
    }

    public PlanoDTO buscar(Long id) {
        return PlanoDTO.from(get(id));
    }

    @Transactional
    public PlanoDTO atualizar(Long id, AtualizarPlanoDTO dto) {
        Plano plano = get(id);
        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Informe um valor maior que zero.", "VALOR_INVALIDO");
        }
        plano.setValor(dto.valor());
        if (dto.validadeMeses() != null) {
            if (dto.validadeMeses() <= 0) {
                throw new BusinessException("Validade deve ser maior que zero.", "VALIDADE_INVALIDA");
            }
            plano.setValidadeMeses(dto.validadeMeses());
        }
        return PlanoDTO.from(planoRepository.save(plano));
    }

    private Plano get(Long id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Plano não encontrado.", "PLANO_NAO_ENCONTRADO"));
    }

    public List<AlunoSituacaoDTO> listarSituacoesAlunos() {
        var alunos = usuarioRepository.findByTipoUsuarioOrderByNomeAsc(TipoUsuario.ALUNO);
        Map<Long, AlunoPlano> planosAtivos = mapaPlanosAtivos();
        return alunos.stream()
                .map(aluno -> AlunoSituacaoDTO.from(aluno, planosAtivos.get(aluno.getId())))
                .toList();
    }

    public Map<Long, SituacaoAluno> mapaSituacoesAlunos() {
        var alunos = usuarioRepository.findByTipoUsuarioOrderByNomeAsc(TipoUsuario.ALUNO);
        Map<Long, AlunoPlano> planosAtivos = mapaPlanosAtivos();
        return alunos.stream()
                .collect(Collectors.toMap(
                        Usuario::getId,
                        aluno -> AlunoSituacaoDTO.from(aluno, planosAtivos.get(aluno.getId())).situacao()
                ));
    }

    private Map<Long, AlunoPlano> mapaPlanosAtivos() {
        return alunoPlanoRepository.findByAtivoTrue().stream()
                .collect(Collectors.toMap(ap -> ap.getAluno().getId(), ap -> ap, (a, b) -> a));
    }

    public List<AlunoPlanoDTO> listarPorAluno(Long alunoId) {
        Usuario aluno = usuarioService.get(alunoId);
        return alunoPlanoRepository.findByAluno(aluno).stream().map(AlunoPlanoDTO::from).toList();
    }

    @Transactional
    public AlunoPlanoDTO vincularPlano(Long alunoId, Long planoId, LocalDate dataInicio) {
        Usuario aluno = usuarioService.get(alunoId);
        Plano plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new BusinessException("Plano não encontrado.", "PLANO_NAO_ENCONTRADO"));

        alunoPlanoRepository.findByAluno(aluno).forEach(ap -> {
            ap.setAtivo(false);
            alunoPlanoRepository.save(ap);
        });

        LocalDate dataFim = plano.getValidadeMeses() != null
                ? dataInicio.plusMonths(plano.getValidadeMeses())
                : null;

        AlunoPlano ap = AlunoPlano.builder()
                .aluno(aluno)
                .plano(plano)
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .aulasConsumidasSemana(0)
                .remadasConsumidas(0)
                .ativo(true)
                .build();
        return AlunoPlanoDTO.from(alunoPlanoRepository.save(ap));
    }
}
