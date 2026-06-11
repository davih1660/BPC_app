package com.bpc.escola.controller;

import com.bpc.escola.dto.OperacaoDiaHorarioDTO;
import com.bpc.escola.dto.OperacaoHorarioSlotDTO;
import com.bpc.escola.service.HorarioOperacionalService;
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

    private final HorarioOperacionalService horarioOperacionalService;

    @GetMapping("/horario-atual")
    public OperacaoHorarioSlotDTO horarioAtual() {
        return horarioOperacionalService.obterOperacaoAtual();
    }

    @GetMapping("/dia")
    public OperacaoDiaHorarioDTO dia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate ref = data != null ? data : RelogioSaoPaulo.hoje();
        return horarioOperacionalService.obterDia(ref);
    }
}
