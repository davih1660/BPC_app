package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.TipoBloqueioAgenda;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bloqueios_agenda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueioAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id")
    private HorarioColetivo horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBloqueioAgenda tipo;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
