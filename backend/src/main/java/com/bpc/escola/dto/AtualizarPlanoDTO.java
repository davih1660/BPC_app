package com.bpc.escola.dto;

import java.math.BigDecimal;

public record AtualizarPlanoDTO(
        BigDecimal valor,
        Integer validadeMeses
) {
}
