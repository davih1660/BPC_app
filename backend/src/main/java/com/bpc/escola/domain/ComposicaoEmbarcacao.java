package com.bpc.escola.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "composicao_embarcacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComposicaoEmbarcacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embarcacao_principal_id", nullable = false)
    private Embarcacao embarcacaoPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embarcacao_filha_id", nullable = false)
    private Embarcacao embarcacaoFilha;
}
