package com.bpc.escola.service;

import com.bpc.escola.domain.ComposicaoEmbarcacao;
import com.bpc.escola.domain.Embarcacao;
import com.bpc.escola.domain.enums.StatusEmbarcacao;
import com.bpc.escola.dto.ComposicaoDTO;
import com.bpc.escola.dto.EmbarcacaoDTO;
import com.bpc.escola.dto.PageResponse;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.ComposicaoEmbarcacaoRepository;
import com.bpc.escola.repository.EmbarcacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmbarcacaoService {

    private final EmbarcacaoRepository embarcacaoRepository;
    private final ComposicaoEmbarcacaoRepository composicaoRepository;
    private final DisponibilidadeEmbarcacaoService disponibilidadeService;

    public PageResponse<EmbarcacaoDTO> listar(String q, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Embarcacao> result = (q != null && !q.isBlank())
                ? embarcacaoRepository.findByNomeContainingIgnoreCase(q, pr)
                : embarcacaoRepository.findAll(pr);
        LocalDate hoje = RelogioSaoPaulo.hoje();
        return new PageResponse<>(
                result.getContent().stream()
                        .map(e -> EmbarcacaoDTO.from(e, disponibilidadeService.calcularStatusEfetivo(
                                e, hoje, LocalTime.of(6, 0), LocalTime.of(20, 0))))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public EmbarcacaoDTO buscar(Long id) {
        Embarcacao e = get(id);
        LocalDate hoje = RelogioSaoPaulo.hoje();
        return EmbarcacaoDTO.from(e, disponibilidadeService.calcularStatusEfetivo(
                e, hoje, LocalTime.of(6, 0), LocalTime.of(20, 0)));
    }

    public EmbarcacaoDTO disponibilidade(Long id, LocalDate data, LocalTime inicio, LocalTime fim) {
        Embarcacao e = get(id);
        return EmbarcacaoDTO.from(e, disponibilidadeService.calcularStatusEfetivo(e, data, inicio, fim));
    }

    @Transactional
    public EmbarcacaoDTO criar(EmbarcacaoDTO dto) {
        Embarcacao e = Embarcacao.builder()
                .nome(dto.nome())
                .tipo(dto.tipo())
                .capacidade(dto.capacidade())
                .status(dto.status() != null ? dto.status() : StatusEmbarcacao.DISPONIVEL)
                .observacoes(dto.observacoes())
                .build();
        return EmbarcacaoDTO.from(embarcacaoRepository.save(e), StatusEmbarcacao.DISPONIVEL);
    }

    @Transactional
    public EmbarcacaoDTO atualizar(Long id, EmbarcacaoDTO dto) {
        Embarcacao e = get(id);
        e.setNome(dto.nome());
        e.setTipo(dto.tipo());
        e.setCapacidade(dto.capacidade());
        if (dto.status() != null) {
            e.setStatus(dto.status());
        }
        e.setObservacoes(dto.observacoes());
        Embarcacao salva = embarcacaoRepository.save(e);
        return EmbarcacaoDTO.from(salva, disponibilidadeService.calcularStatusEfetivo(
                salva, RelogioSaoPaulo.hoje(), LocalTime.of(6, 0), LocalTime.of(20, 0)));
    }

    @Transactional
    public EmbarcacaoDTO interditar(Long id, String motivo) {
        Embarcacao e = get(id);
        e.setStatus(StatusEmbarcacao.INTERDITADA);
        e.setObservacoes(motivo);
        Embarcacao salva = embarcacaoRepository.save(e);
        return EmbarcacaoDTO.from(salva, StatusEmbarcacao.INTERDITADA);
    }

    public List<ComposicaoDTO> composicao(Long id) {
        Embarcacao e = get(id);
        return composicaoRepository.findByEmbarcacaoPrincipal(e).stream()
                .map(c -> new ComposicaoDTO(c.getId(), c.getEmbarcacaoFilha().getId(), c.getEmbarcacaoFilha().getNome()))
                .toList();
    }

    public Embarcacao get(Long id) {
        return embarcacaoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Embarcação não encontrada.", "EMBARCACAO_NAO_ENCONTRADA"));
    }
}
