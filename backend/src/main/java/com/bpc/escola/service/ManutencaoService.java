package com.bpc.escola.service;

import com.bpc.escola.domain.Manutencao;
import com.bpc.escola.domain.enums.StatusEmbarcacao;
import com.bpc.escola.domain.enums.StatusManutencao;
import com.bpc.escola.dto.ManutencaoDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.EmbarcacaoRepository;
import com.bpc.escola.repository.ManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final EmbarcacaoRepository embarcacaoRepository;
    private final EmbarcacaoService embarcacaoService;

    public List<ManutencaoDTO> listar(Long embarcacaoId) {
        if (embarcacaoId != null) {
            return manutencaoRepository.findByEmbarcacao(embarcacaoService.get(embarcacaoId))
                    .stream().map(ManutencaoDTO::from).toList();
        }
        return manutencaoRepository.findAll().stream().map(ManutencaoDTO::from).toList();
    }

    @Transactional
    public ManutencaoDTO criar(ManutencaoDTO dto) {
        var embarcacao = embarcacaoService.get(dto.embarcacaoId());
        embarcacao.setStatus(StatusEmbarcacao.MANUTENCAO);
        embarcacaoRepository.save(embarcacao);
        Manutencao m = Manutencao.builder()
                .embarcacao(embarcacao)
                .descricao(dto.descricao())
                .dataInicio(dto.dataInicio())
                .dataFim(dto.dataFim())
                .status(dto.status() != null ? dto.status() : StatusManutencao.EM_ANDAMENTO)
                .build();
        return ManutencaoDTO.from(manutencaoRepository.save(m));
    }

    @Transactional
    public ManutencaoDTO atualizarStatus(Long id, StatusManutencao status) {
        Manutencao m = manutencaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Manutenção não encontrada.", "MANUTENCAO_NAO_ENCONTRADA"));
        m.setStatus(status);
        if (status == StatusManutencao.CONCLUIDA) {
            m.getEmbarcacao().setStatus(StatusEmbarcacao.DISPONIVEL);
            embarcacaoRepository.save(m.getEmbarcacao());
        }
        return ManutencaoDTO.from(manutencaoRepository.save(m));
    }
}
