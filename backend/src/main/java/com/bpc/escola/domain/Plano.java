package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.CategoriaPlano;
import com.bpc.escola.domain.enums.PeriodicidadePlano;
import com.bpc.escola.domain.enums.TipoPlano;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "planos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlano tipoPlano;

    @Enumerated(EnumType.STRING)
    private CategoriaPlano categoriaPlano;

    @Enumerated(EnumType.STRING)
    private PeriodicidadePlano periodicidade;

    private Integer quantidadeAulasSemana;

    private Integer quantidadeAulasMes;

    private Integer quantidadeRemadas;

    private Integer validadeMeses;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private Boolean ilimitado;
}
