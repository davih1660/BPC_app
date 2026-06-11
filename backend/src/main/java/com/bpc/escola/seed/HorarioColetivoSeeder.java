package com.bpc.escola.seed;

import com.bpc.escola.domain.*;
import com.bpc.escola.domain.enums.*;
import com.bpc.escola.repository.*;
import com.bpc.escola.service.RelogioSaoPaulo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@Order(2)
@RequiredArgsConstructor
public class HorarioColetivoSeeder implements CommandLineRunner {

    private final HorarioColetivoRepository horarioRepository;
    private final ReservaColetivaRepository reservaColetivaRepository;
    private final AulaRepository aulaRepository;
    private final ReservaAulaRepository reservaAulaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (horarioRepository.count() > 0) {
            return;
        }

        Map<String, HorarioColetivo> horariosPorChave = new LinkedHashMap<>();

        if (aulaRepository.count() > 0) {
            migrarDeAulas(horariosPorChave);
            migrarReservasAula(horariosPorChave);
        } else {
            criarHorariosPadrao(horariosPorChave);
        }

        garantirUsuarioManutencao();
    }

    private void migrarDeAulas(Map<String, HorarioColetivo> horariosPorChave) {
        Map<String, List<Aula>> grupos = new LinkedHashMap<>();
        for (Aula aula : aulaRepository.findAll()) {
            String chave = chave(aula.getDiaSemana(), aula.getHorarioInicio(), aula.getHorarioFim());
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(aula);
        }

        for (var entry : grupos.entrySet()) {
            List<Aula> grupo = entry.getValue();
            Aula ref = grupo.getFirst();
            int capacidade = grupo.stream().mapToInt(Aula::getCapacidadeMaxima).sum();
            String titulo = String.format("Coletiva %s %s-%s",
                    ref.getDiaSemana().name().substring(0, 3),
                    ref.getHorarioInicio(), ref.getHorarioFim());

            HorarioColetivo h = horarioRepository.save(HorarioColetivo.builder()
                    .titulo(titulo)
                    .diaSemana(ref.getDiaSemana())
                    .horarioInicio(ref.getHorarioInicio())
                    .horarioFim(ref.getHorarioFim())
                    .capacidadeSlot(capacidade > 0 ? capacidade : 42)
                    .build());
            horariosPorChave.put(entry.getKey(), h);
        }
    }

    private void migrarReservasAula(Map<String, HorarioColetivo> horariosPorChave) {
        Set<String> duplicatas = new HashSet<>();
        for (ReservaAula ra : reservaAulaRepository.findAll()) {
            if (ra.getStatus() != StatusReserva.CONFIRMADA) continue;
            Aula aula = ra.getAula();
            String chave = chave(aula.getDiaSemana(), aula.getHorarioInicio(), aula.getHorarioFim());
            HorarioColetivo horario = horariosPorChave.get(chave);
            if (horario == null) continue;

            String dup = horario.getId() + "-" + ra.getAluno().getId() + "-" + ra.getDataReserva();
            if (!duplicatas.add(dup)) continue;

            OrigemReserva origem = ra.getOrigem() != null ? ra.getOrigem() : OrigemReserva.MANUAL;
            reservaColetivaRepository.save(ReservaColetiva.builder()
                    .horario(horario)
                    .aluno(ra.getAluno())
                    .status(StatusReserva.CONFIRMADA)
                    .origem(origem)
                    .dataReserva(ra.getDataReserva())
                    .presente(Boolean.TRUE.equals(ra.getPresente()))
                    .criadoEm(ra.getCriadoEm() != null ? ra.getCriadoEm() : RelogioSaoPaulo.dataHora())
                    .build());
        }
    }

    private void criarHorariosPadrao(Map<String, HorarioColetivo> horariosPorChave) {
        for (var entry : HorariosFixos.horarios().entrySet()) {
            DiaSemana dia = entry.getKey();
            for (LocalTime[] slot : entry.getValue()) {
                String titulo = String.format("Coletiva %s %s-%s",
                        dia.name().substring(0, 3), slot[0], slot[1]);
                HorarioColetivo h = horarioRepository.save(HorarioColetivo.builder()
                        .titulo(titulo)
                        .diaSemana(dia)
                        .horarioInicio(slot[0])
                        .horarioFim(slot[1])
                        .capacidadeSlot(42)
                        .build());
                horariosPorChave.put(chave(dia, slot[0], slot[1]), h);
            }
        }
    }

    private void garantirUsuarioManutencao() {
        if (usuarioRepository.findByEmail("manutencao@bpc.com").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .nome("Equipe Manutenção")
                    .email("manutencao@bpc.com")
                    .telefone("11999990099")
                    .tipoUsuario(TipoUsuario.MANUTENCAO)
                    .build());
        }
    }

    private String chave(DiaSemana dia, LocalTime inicio, LocalTime fim) {
        return dia + "-" + inicio + "-" + fim;
    }
}
