package com.bpc.escola.controller;

import com.bpc.escola.dto.CobrancaDTO;
import com.bpc.escola.service.CobrancaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cobrancas")
@RequiredArgsConstructor
public class CobrancaController {

    private final CobrancaService service;

    @GetMapping
    public List<CobrancaDTO> listar() {
        return service.listarTodas();
    }

    @PostMapping
    public CobrancaDTO criar(@RequestBody Map<String, Object> body) {
        return service.criar(
                Long.valueOf(body.get("alunoId").toString()),
                body.get("planoId") != null ? Long.valueOf(body.get("planoId").toString()) : null,
                new BigDecimal(body.get("valor").toString()),
                LocalDate.parse(body.get("vencimento").toString()));
    }

    @PatchMapping("/{id}/pago")
    public CobrancaDTO marcarPago(@PathVariable Long id) {
        return service.marcarPago(id);
    }

    @PatchMapping("/{id}/inadimplente")
    public CobrancaDTO marcarInadimplente(@PathVariable Long id) {
        return service.marcarInadimplente(id);
    }
}
