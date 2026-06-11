package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.EstadoSessao;
import com.bpc.escola.dto.GrupoCanoaDTO;
import com.bpc.escola.dto.SessaoOperacaoDTO;
import com.bpc.escola.service.SessaoOperacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessao")
@RequiredArgsConstructor
public class SessaoOperacaoController {

    private final SessaoOperacaoService service;

    @GetMapping
    public SessaoOperacaoDTO obter(
            @RequestParam Long horarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return service.obter(horarioId, data);
    }

    @PostMapping("/chamada")
    public SessaoOperacaoDTO iniciarChamada(
            @RequestParam Long horarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return service.iniciarChamada(horarioId, data);
    }

    @PatchMapping("/estado")
    public SessaoOperacaoDTO atualizarEstado(
            @RequestParam Long horarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestBody Map<String, String> body
    ) {
        EstadoSessao estado = EstadoSessao.valueOf(body.get("estado"));
        return service.atualizarEstado(horarioId, data, estado);
    }

    @PostMapping("/grupos")
    public GrupoCanoaDTO criarGrupo(@RequestBody Map<String, Object> body) {
        Long horarioId = Long.valueOf(body.get("horarioId").toString());
        LocalDate data = LocalDate.parse(body.get("data").toString());
        Long professorId = Long.valueOf(body.get("professorId").toString());
        @SuppressWarnings("unchecked")
        List<Long> reservaIds = ((List<Number>) body.get("reservaIds")).stream()
                .map(Number::longValue).toList();
        return service.criarGrupo(horarioId, data, professorId, reservaIds);
    }

    @PostMapping("/grupos/{grupoId}/confirmar")
    public GrupoCanoaDTO confirmarGrupo(
            @PathVariable Long grupoId,
            @RequestBody Map<String, Long> body
    ) {
        return service.confirmarGrupo(grupoId, body.get("embarcacaoId"));
    }

    @PostMapping("/escalacao")
    public void escalarProfessor(@RequestBody Map<String, Object> body) {
        Long horarioId = Long.valueOf(body.get("horarioId").toString());
        LocalDate data = LocalDate.parse(body.get("data").toString());
        Long professorId = Long.valueOf(body.get("professorId").toString());
        service.escalarProfessor(horarioId, data, professorId);
    }
}
