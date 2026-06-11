package com.bpc.escola.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grupos_canoa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoCanoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoHorario sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embarcacao_id")
    private Embarcacao embarcacao;

    @Builder.Default
    private Boolean confirmado = false;
}
