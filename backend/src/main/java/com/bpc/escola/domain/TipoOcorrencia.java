package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.GravidadeOcorrencia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_ocorrencia", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GravidadeOcorrencia gravidade;

    @Builder.Default
    private Boolean ativo = true;

    @Builder.Default
    private Integer ordem = 0;
}
