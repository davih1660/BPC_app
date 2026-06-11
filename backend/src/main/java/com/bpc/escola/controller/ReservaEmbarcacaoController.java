package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.CreateReservaEmbarcacaoRequest;
import com.bpc.escola.dto.ReservaEmbarcacaoDTO;
import com.bpc.escola.service.ReservaEmbarcacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas-embarcacao")
@RequiredArgsConstructor
public class ReservaEmbarcacaoController {

    private final ReservaEmbarcacaoService reservaEmbarcacaoService;

    @GetMapping
    public List<ReservaEmbarcacaoDTO> listar(
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) StatusReserva status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return reservaEmbarcacaoService.listar(alunoId, status, data);
    }

    @PostMapping
    public ReservaEmbarcacaoDTO criar(@Valid @RequestBody CreateReservaEmbarcacaoRequest request) {
        return reservaEmbarcacaoService.criarPorStaff(request);
    }

    @DeleteMapping("/{id}")
    public void cancelar(@PathVariable Long id) {
        reservaEmbarcacaoService.cancelar(id);
    }
}
