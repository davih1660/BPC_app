package com.bpc.escola.seed;

import com.bpc.escola.domain.HorarioColetivo;
import com.bpc.escola.domain.ReservaColetiva;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.OrigemReserva;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.domain.enums.TipoUsuario;
import com.bpc.escola.repository.HorarioColetivoRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.UsuarioRepository;
import com.bpc.escola.service.DiaSemanaUtil;
import com.bpc.escola.service.RelogioSaoPaulo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class JunhoReservasSeeder implements CommandLineRunner {

    private final HorarioColetivoRepository horarioRepository;
    private final ReservaColetivaRepository reservaColetivaRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        List<Usuario> alunos = usuarioRepository.findByTipoUsuarioOrderByNomeAsc(TipoUsuario.ALUNO);
        if (alunos.isEmpty()) {
            return;
        }

        int ano = RelogioSaoPaulo.hoje().getYear();
        LocalDate inicio = LocalDate.of(ano, Month.JUNE, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        int criadas = 0;
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fim)) {
            var diaSemana = DiaSemanaUtil.fromLocalDate(cursor);
            for (HorarioColetivo horario : horarioRepository.findByDiaSemana(diaSemana)) {
                criadas += popularSlot(horario, cursor, alunos);
            }
            cursor = cursor.plusDays(1);
        }

        if (criadas > 0) {
            log.info("JunhoReservasSeeder: {} reservas coletivas criadas para {}/{}.", criadas, inicio, fim);
        }
    }

    private int popularSlot(HorarioColetivo horario, LocalDate data, List<Usuario> alunos) {
        long existentes = reservaColetivaRepository.countByHorarioAndDataReservaAndStatus(
                horario, data, StatusReserva.CONFIRMADA);
        int capacidade = horario.getCapacidadeSlot();
        int alvo = ThreadLocalRandom.current().nextInt(3, Math.min(capacidade, 40) + 1);

        if (existentes >= alvo) {
            return 0;
        }

        int faltam = (int) (alvo - existentes);
        List<Usuario> embaralhados = new ArrayList<>(alunos);
        Collections.shuffle(embaralhados);

        OrigemReserva[] origens = OrigemReserva.values();
        var agora = RelogioSaoPaulo.hora();
        boolean slotPassou = data.isBefore(RelogioSaoPaulo.hoje())
                || (data.equals(RelogioSaoPaulo.hoje()) && !agora.isBefore(horario.getHorarioFim()));

        int criadas = 0;
        for (Usuario aluno : embaralhados) {
            if (criadas >= faltam) {
                break;
            }
            if (reservaColetivaRepository.existsByHorarioAndAlunoAndDataReservaAndStatus(
                    horario, aluno, data, StatusReserva.CONFIRMADA)) {
                continue;
            }
            boolean presente = slotPassou && ThreadLocalRandom.current().nextBoolean();
            reservaColetivaRepository.save(ReservaColetiva.builder()
                    .horario(horario)
                    .aluno(aluno)
                    .status(StatusReserva.CONFIRMADA)
                    .origem(origens[ThreadLocalRandom.current().nextInt(origens.length)])
                    .dataReserva(data)
                    .presente(presente)
                    .criadoEm(RelogioSaoPaulo.dataHora())
                    .build());
            criadas++;
        }
        return criadas;
    }
}
