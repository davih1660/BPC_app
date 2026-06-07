package com.bpc.escola.controller;

import com.bpc.escola.dto.ComposicaoDTO;
import com.bpc.escola.dto.EmbarcacaoDTO;
import com.bpc.escola.dto.PageResponse;
import com.bpc.escola.service.EmbarcacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/embarcacoes")
@RequiredArgsConstructor
public class EmbarcacaoController {

    private final EmbarcacaoService embarcacaoService;

    @GetMapping
    public PageResponse<EmbarcacaoDTO> listar(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return embarcacaoService.listar(q, page, size);
    }

    @GetMapping("/{id}")
    public EmbarcacaoDTO buscar(@PathVariable Long id) {
        return embarcacaoService.buscar(id);
    }

    @GetMapping("/{id}/disponibilidade")
    public EmbarcacaoDTO disponibilidade(
            @PathVariable Long id,
            @RequestParam LocalDate data,
            @RequestParam LocalTime inicio,
            @RequestParam LocalTime fim) {
        return embarcacaoService.disponibilidade(id, data, inicio, fim);
    }

    @GetMapping("/{id}/composicao")
    public List<ComposicaoDTO> composicao(@PathVariable Long id) {
        return embarcacaoService.composicao(id);
    }

    @PostMapping
    public EmbarcacaoDTO criar(@RequestBody EmbarcacaoDTO dto) {
        return embarcacaoService.criar(dto);
    }

    @PutMapping("/{id}")
    public EmbarcacaoDTO atualizar(@PathVariable Long id, @RequestBody EmbarcacaoDTO dto) {
        return embarcacaoService.atualizar(id, dto);
    }

    @PostMapping("/{id}/interditar")
    public EmbarcacaoDTO interditar(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.getOrDefault("motivo", "Interdição operacional") : "Interdição operacional";
        return embarcacaoService.interditar(id, motivo);
    }
}
