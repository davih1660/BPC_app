package com.bpc.escola.controller;

import com.bpc.escola.dto.ProximaReservaDTO;
import com.bpc.escola.dto.SaldoPlanoDTO;
import com.bpc.escola.service.AlunoPortalService;
import com.bpc.escola.service.SaldoPlanoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alunos")
@RequiredArgsConstructor
public class AlunoPortalController {

    private final AlunoPortalService alunoPortalService;
    private final SaldoPlanoService saldoPlanoService;

    @GetMapping("/{id}/proxima-reserva")
    public ProximaReservaDTO proximaReserva(@PathVariable Long id) {
        return alunoPortalService.obterProximaReserva(id);
    }

    @GetMapping("/{id}/saldo")
    public SaldoPlanoDTO saldo(@PathVariable Long id) {
        return saldoPlanoService.obterSaldo(id, com.bpc.escola.service.RelogioSaoPaulo.hoje());
    }
}
