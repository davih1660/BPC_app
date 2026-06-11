package com.bpc.escola.controller;

import com.bpc.escola.dto.ReservaColetivaDTO;
import com.bpc.escola.dto.WellhubSyncErroDTO;
import com.bpc.escola.service.WellhubIntegracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integracao/wellhub")
@RequiredArgsConstructor
public class WellhubController {

    private final WellhubIntegracaoService service;

    @PostMapping("/reservas")
    public ReservaColetivaDTO importar(@RequestBody Map<String, Object> payload) {
        return service.importarReserva(payload);
    }

    @GetMapping("/erros")
    public List<WellhubSyncErroDTO> erros() {
        return service.listarErros();
    }

    @PatchMapping("/erros/{id}/resolver")
    public void resolver(@PathVariable Long id) {
        service.resolverErro(id);
    }
}
