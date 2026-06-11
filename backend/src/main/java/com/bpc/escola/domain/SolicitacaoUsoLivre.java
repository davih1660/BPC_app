package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.StatusSolicitacaoUsoLivre;
import com.bpc.escola.domain.enums.TipoEmbarcacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_uso_livre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoUsoLivre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", nullable = false)
    private HorarioColetivo horario;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmbarcacao tipoCanoaDesejada;

    @Column(length = 500)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacaoUsoLivre status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embarcacao_id")
    private Embarcacao embarcacaoAtribuida;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_embarcacao_id")
    private ReservaEmbarcacao reservaEmbarcacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processado_por_id")
    private Usuario processadoPor;

    @Column(length = 500)
    private String motivoRecusa;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime processadoEm;
}
