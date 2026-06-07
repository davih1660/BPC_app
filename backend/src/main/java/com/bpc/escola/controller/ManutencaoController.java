package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.StatusManutencao;
import com.bpc.escola.dto.ManutencaoDTO;
import com.bpc.escola.service.ManutencaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manutencoes")
@RequiredArgsConstructor
public class ManutencaoController {

    private final ManutencaoService manutencaoService;

    @GetMapping
    public List<ManutencaoDTO> listar(@RequestParam(required = false) Long embarcacaoId) {
        return manutencaoService.listar(embarcacaoId);
    }

    @PostMapping
    public ManutencaoDTO criar(@RequestBody ManutencaoDTO dto) {
        return manutencaoService.criar(dto);
    }

    @PatchMapping("/{id}/status")
    public ManutencaoDTO atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return manutencaoService.atualizarStatus(id, StatusManutencao.valueOf(body.get("status")));
    }
}
