package com.bpc.escola.domain;

import com.bpc.escola.domain.enums.DiaSemana;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "horarios_coletivos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dia_semana", "horario_inicio", "horario_fim"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioColetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private LocalTime horarioInicio;

    @Column(nullable = false)
    private LocalTime horarioFim;

    @Column(nullable = false)
    private Integer capacidadeSlot;
}
