package com.bpc.escola.service;

import com.bpc.escola.domain.AlunoPlano;
import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.domain.enums.TipoPlano;
import com.bpc.escola.dto.SaldoPlanoDTO;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.ReservaAulaRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import com.bpc.escola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaldoPlanoService {

    private static final int WELLHUB_LIMITE_MES_PADRAO = 8;

    private final UsuarioRepository usuarioRepository;
    private final PlanoValidacaoService planoValidacaoService;
    private final ReservaColetivaRepository reservaColetivaRepository;
    private final ReservaAulaRepository reservaAulaRepository;

    public SaldoPlanoDTO obterSaldo(Long alunoId, LocalDate ref) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado.", "ALUNO_NAO_ENCONTRADO"));

        AlunoPlano alunoPlano;
        try {
            alunoPlano = planoValidacaoService.obterPlanoAtivo(aluno);
        } catch (BusinessException e) {
            return new SaldoPlanoDTO(alunoId, null, "sem_plano", 0, 0, "Sem plano ativo");
        }

        Plano plano = alunoPlano.getPlano();
        TipoPlano tipo = plano.getTipoPlano();

        if (tipo == TipoPlano.ILIMITADO || Boolean.TRUE.equals(plano.getIlimitado())) {
            return new SaldoPlanoDTO(alunoId, plano.getNome(), "ilimitado", 0, null, "Aulas ilimitadas");
        }

        if (tipo == TipoPlano.WELLHUB || plano.getQuantidadeAulasMes() != null) {
            int limite = plano.getQuantidadeAulasMes() != null ? plano.getQuantidadeAulasMes() : WELLHUB_LIMITE_MES_PADRAO;
            LocalDate inicioMes = ref.withDayOfMonth(1);
            LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            int usado = (int) contarReservas(aluno, inicioMes, fimMes);
            return new SaldoPlanoDTO(
                    alunoId, plano.getNome(), "mes", usado, limite,
                    usado + " de " + limite + " aulas este mês");
        }

        if (tipo == TipoPlano.AVULSO_REMADAS) {
            int usado = alunoPlano.getRemadasConsumidas() != null ? alunoPlano.getRemadasConsumidas() : 0;
            Integer limite = plano.getQuantidadeRemadas();
            return new SaldoPlanoDTO(
                    alunoId, plano.getNome(), "pacote", usado, limite,
                    limite != null ? usado + " de " + limite + " remadas" : usado + " remadas usadas");
        }

        WeekFields wf = WeekFields.ISO;
        LocalDate inicioSemana = ref.with(wf.dayOfWeek(), 1);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        int usado = (int) contarReservas(aluno, inicioSemana, fimSemana);
        int limite = switch (tipo) {
            case UMA_AULA_SEMANA -> 1;
            case DUAS_AULAS_SEMANA -> 2;
            case TRES_AULAS_SEMANA -> 3;
            default -> Integer.MAX_VALUE;
        };
        return new SaldoPlanoDTO(
                alunoId, plano.getNome(), "semana", usado, limite,
                usado + " de " + limite + " aulas esta semana");
    }

    private long contarReservas(Usuario aluno, LocalDate inicio, LocalDate fim) {
        return reservaColetivaRepository.countByAlunoAndStatusAndDataReservaBetween(
                aluno, StatusReserva.CONFIRMADA, inicio, fim)
                + reservaAulaRepository.countByAlunoAndStatusAndDataReservaBetween(
                aluno, StatusReserva.CONFIRMADA, inicio, fim);
    }
}
