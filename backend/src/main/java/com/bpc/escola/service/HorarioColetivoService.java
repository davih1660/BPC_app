package com.bpc.escola.service;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.enums.DiaSemana;
import com.bpc.escola.dto.HorarioColetivoDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.HorarioColetivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HorarioColetivoService {

    private final HorarioColetivoRepository horarioRepository;

    public List<HorarioColetivoDTO> listar(DiaSemana dia) {
        List<HorarioColetivo> lista = dia != null
                ? horarioRepository.findByDiaSemana(dia)
                : horarioRepository.findAll();
        return lista.stream().map(HorarioColetivoDTO::from).toList();
    }

    public HorarioColetivoDTO buscar(Long id) {
        return HorarioColetivoDTO.from(get(id));
    }

    @Transactional
    public HorarioColetivoDTO criar(HorarioColetivoDTO dto) {
        HorarioColetivo h = HorarioColetivo.builder()
                .titulo(dto.titulo())
                .diaSemana(dto.diaSemana())
                .horarioInicio(dto.horarioInicio())
                .horarioFim(dto.horarioFim())
                .capacidadeSlot(dto.capacidadeSlot())
                .build();
        return HorarioColetivoDTO.from(horarioRepository.save(h));
    }

    @Transactional
    public HorarioColetivoDTO atualizar(Long id, HorarioColetivoDTO dto) {
        HorarioColetivo h = get(id);
        h.setTitulo(dto.titulo());
        h.setDiaSemana(dto.diaSemana());
        h.setHorarioInicio(dto.horarioInicio());
        h.setHorarioFim(dto.horarioFim());
        h.setCapacidadeSlot(dto.capacidadeSlot());
        return HorarioColetivoDTO.from(horarioRepository.save(h));
    }

    public HorarioColetivo get(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Horário não encontrado.", "HORARIO_NAO_ENCONTRADO"));
    }
}
