package com.bpc.escola.controller;

import com.bpc.escola.dto.AlunoPlanoDTO;
import com.bpc.escola.dto.AlunoSituacaoDTO;
import com.bpc.escola.dto.AtualizarPlanoDTO;
import com.bpc.escola.dto.PlanoDTO;
import com.bpc.escola.service.PlanoService;
import com.bpc.escola.service.RelogioSaoPaulo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;

    @GetMapping("/planos")
    public List<PlanoDTO> listarPlanos() {
        return planoService.listarPlanos();
    }

    @PutMapping("/planos/{id}")
    public PlanoDTO atualizarPlano(@PathVariable Long id, @RequestBody AtualizarPlanoDTO dto) {
        return planoService.atualizar(id, dto);
    }

    @GetMapping("/planos/situacoes")
    public List<AlunoSituacaoDTO> listarSituacoesAlunos() {
        return planoService.listarSituacoesAlunos();
    }

    @GetMapping("/alunos/{alunoId}/planos")
    public List<AlunoPlanoDTO> listarPorAluno(@PathVariable Long alunoId) {
        return planoService.listarPorAluno(alunoId);
    }

    @PostMapping("/alunos/{alunoId}/planos")
    public AlunoPlanoDTO vincular(
            @PathVariable Long alunoId,
            @RequestBody Map<String, Object> body) {
        Long planoId = Long.valueOf(body.get("planoId").toString());
        LocalDate dataInicio = body.containsKey("dataInicio")
                ? LocalDate.parse(body.get("dataInicio").toString())
                : RelogioSaoPaulo.hoje();
        return planoService.vincularPlano(alunoId, planoId, dataInicio);
    }
}
