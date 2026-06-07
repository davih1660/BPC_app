package com.bpc.escola.service;

import com.bpc.escola.domain.AlunoPlano;
import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.dto.AlunoPlanoDTO;
import com.bpc.escola.dto.PlanoDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.AlunoPlanoRepository;
import com.bpc.escola.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final AlunoPlanoRepository alunoPlanoRepository;
    private final UsuarioService usuarioService;

    public List<PlanoDTO> listarPlanos() {
        return planoRepository.findAll().stream().map(PlanoDTO::from).toList();
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
