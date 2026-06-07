package com.bpc.escola.controller;

import com.bpc.escola.dto.OperacaoAulaDTO;
import com.bpc.escola.dto.OperacaoDiaDTO;
import com.bpc.escola.service.AulaOperacionalService;
import com.bpc.escola.service.RelogioSaoPaulo;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/operacao")
@RequiredArgsConstructor
public class OperacaoController {

    private final AulaOperacionalService aulaOperacionalService;

    @GetMapping("/aula-atual")
    public OperacaoAulaDTO aulaAtual() {
        return aulaOperacionalService.obterOperacaoAtual();
    }

    @GetMapping("/dia")
    public OperacaoDiaDTO dia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate ref = data != null ? data : RelogioSaoPaulo.hoje();
        return aulaOperacionalService.obterDia(ref);
    }
}
