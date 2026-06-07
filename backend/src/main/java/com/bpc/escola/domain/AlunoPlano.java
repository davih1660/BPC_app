package com.bpc.escola.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "aluno_planos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlunoPlano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Column(nullable = false)
    private LocalDate dataInicio;

    private LocalDate dataFim;

    @Builder.Default
    private Integer aulasConsumidasSemana = 0;

    @Builder.Default
    private Integer remadasConsumidas = 0;

    @Builder.Default
    private Boolean ativo = true;
}
