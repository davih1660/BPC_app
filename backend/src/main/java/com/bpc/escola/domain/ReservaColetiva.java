package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.OrigemReserva;
import com.bpc.escola.domain.enums.StatusReserva;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas_coletivas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaColetiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", nullable = false)
    private HorarioColetivo horario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrigemReserva origem = OrigemReserva.MANUAL;

    @Column(nullable = false)
    private LocalDate dataReserva;

    @Builder.Default
    private Boolean presente = false;

    private String wellhubReservaId;

    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
