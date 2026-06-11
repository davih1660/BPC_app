package com.bpc.escola.service;

import com.bpc.escola.domain.TipoOcorrencia;
import com.bpc.escola.dto.TipoOcorrenciaDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.TipoOcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipoOcorrenciaService {

    private final TipoOcorrenciaRepository repository;

    public List<TipoOcorrenciaDTO> listar(Boolean somenteAtivos) {
        List<TipoOcorrencia> lista = Boolean.TRUE.equals(somenteAtivos)
                ? repository.findByAtivoTrueOrderByOrdemAscNomeAsc()
                : repository.findAllByOrderByOrdemAscNomeAsc();
        return lista.stream().map(TipoOcorrenciaDTO::from).toList();
    }

    public TipoOcorrenciaDTO buscar(Long id) {
        return TipoOcorrenciaDTO.from(get(id));
    }

    public TipoOcorrencia get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de ocorrência não encontrado.", "TIPO_OCORRENCIA_NAO_ENCONTRADO"));
    }

    public TipoOcorrencia getAtivo(Long id) {
        TipoOcorrencia tipo = get(id);
        if (!Boolean.TRUE.equals(tipo.getAtivo())) {
            throw new BusinessException("Tipo de ocorrência inativo.", "TIPO_OCORRENCIA_INATIVO");
        }
        return tipo;
    }

    @Transactional
    public TipoOcorrenciaDTO criar(TipoOcorrenciaDTO dto) {
        validar(dto);
        if (repository.findByNome(dto.nome().trim()).isPresent()) {
            throw new BusinessException("Já existe um tipo com este nome.", "TIPO_OCORRENCIA_DUPLICADO");
        }
        TipoOcorrencia salvo = repository.save(TipoOcorrencia.builder()
                .nome(dto.nome().trim())
                .gravidade(dto.gravidade())
                .ativo(dto.ativo() != null ? dto.ativo() : true)
                .ordem(dto.ordem() != null ? dto.ordem() : 0)
                .build());
        return TipoOcorrenciaDTO.from(salvo);
    }

    @Transactional
    public TipoOcorrenciaDTO atualizar(Long id, TipoOcorrenciaDTO dto) {
        validar(dto);
        TipoOcorrencia tipo = get(id);
        repository.findByNome(dto.nome().trim())
                .filter(t -> !t.getId().equals(id))
                .ifPresent(t -> {
                    throw new BusinessException("Já existe um tipo com este nome.", "TIPO_OCORRENCIA_DUPLICADO");
                });
        tipo.setNome(dto.nome().trim());
        tipo.setGravidade(dto.gravidade());
        tipo.setAtivo(dto.ativo() != null ? dto.ativo() : true);
        tipo.setOrdem(dto.ordem() != null ? dto.ordem() : 0);
        return TipoOcorrenciaDTO.from(repository.save(tipo));
    }

    private void validar(TipoOcorrenciaDTO dto) {
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new BusinessException("Informe o nome do tipo.", "NOME_OBRIGATORIO");
        }
        if (dto.gravidade() == null) {
            throw new BusinessException("Informe a gravidade.", "GRAVIDADE_OBRIGATORIA");
        }
    }
}
