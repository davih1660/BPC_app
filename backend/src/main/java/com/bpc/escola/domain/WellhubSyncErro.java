package com.bpc.escola.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wellhub_sync_erros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellhubSyncErro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Builder.Default
    private Boolean resolvido = false;

    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
