package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.StatusSolicitacaoUsoLivre;
import com.bpc.escola.dto.CreateSolicitacaoUsoLivreRequest;
import com.bpc.escola.dto.SolicitacaoUsoLivreDTO;
import com.bpc.escola.service.SolicitacaoUsoLivreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitacoes-uso-livre")
@RequiredArgsConstructor
public class SolicitacaoUsoLivreController {

    private final SolicitacaoUsoLivreService service;

    @GetMapping
    public List<SolicitacaoUsoLivreDTO> listar(
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) StatusSolicitacaoUsoLivre status,
            @RequestParam(required = false, defaultValue = "false") boolean pendentes) {
        if (alunoId != null) {
            return service.listarPorAluno(alunoId);
        }
        if (pendentes) {
            return service.listarPendentes();
        }
        return service.listarTodas(status);
    }

    @PostMapping
    public SolicitacaoUsoLivreDTO criar(@Valid @RequestBody CreateSolicitacaoUsoLivreRequest request) {
        return service.criar(request);
    }

    @PatchMapping("/{id}/aprovar")
    public SolicitacaoUsoLivreDTO aprovar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long embarcacaoId = Long.valueOf(body.get("embarcacaoId").toString());
        Long processadoPorId = Long.valueOf(body.get("processadoPorId").toString());
        return service.aprovar(id, embarcacaoId, processadoPorId);
    }

    @PatchMapping("/{id}/recusar")
    public SolicitacaoUsoLivreDTO recusar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long processadoPorId = Long.valueOf(body.get("processadoPorId").toString());
        String motivo = body.get("motivo") != null ? body.get("motivo").toString() : null;
        return service.recusar(id, processadoPorId, motivo);
    }

    @DeleteMapping("/{id}")
    public void cancelar(@PathVariable Long id, @RequestParam Long alunoId) {
        service.cancelar(id, alunoId);
    }
}
