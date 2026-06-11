package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.StatusListaEspera;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lista_espera")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListaEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", nullable = false)
    private HorarioColetivo horario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @Column(nullable = false)
    private LocalDate dataReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusListaEspera status = StatusListaEspera.AGUARDANDO;

    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
