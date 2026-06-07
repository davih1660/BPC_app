package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.TipoPlano;
import jakarta.persistence.*;
import lombok.*;

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

    private Integer quantidadeAulasSemana;

    private Integer quantidadeRemadas;

    private Integer validadeMeses;

    @Column(nullable = false)
    private Boolean ilimitado;
}
