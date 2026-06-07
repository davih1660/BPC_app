package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.StatusEmbarcacao;
import com.bpc.escola.domain.enums.TipoEmbarcacao;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "embarcacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Embarcacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmbarcacao tipo;

    @Column(nullable = false)
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEmbarcacao status;

    @Column(length = 1000)
    private String observacoes;
}
