package com.bpc.escola.controller;

import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.dto.CreateReservaAulaRequest;
import com.bpc.escola.dto.ReservaAulaDTO;
import com.bpc.escola.service.ReservaAulaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas-aula")
@RequiredArgsConstructor
public class ReservaAulaController {

    private final ReservaAulaService reservaAulaService;

    @GetMapping
    public List<ReservaAulaDTO> listar(
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) StatusReserva status) {
        return reservaAulaService.listar(alunoId, status);
    }

    @PostMapping
    public ReservaAulaDTO criar(@Valid @RequestBody CreateReservaAulaRequest request) {
        return reservaAulaService.criar(request);
    }

    @DeleteMapping("/{id}")
    public void cancelar(@PathVariable Long id) {
        reservaAulaService.cancelar(id);
    }

    @PatchMapping("/{id}/presenca")
    public ReservaAulaDTO presenca(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return reservaAulaService.atualizarPresenca(id, body.getOrDefault("presente", false));
    }
}
