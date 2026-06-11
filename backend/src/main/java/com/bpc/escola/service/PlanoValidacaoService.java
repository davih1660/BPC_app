package com.bpc.escola.service;

import com.bpc.escola.domain.AlunoPlano;
import com.bpc.escola.domain.Plano;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.domain.enums.TipoPlano;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.AlunoPlanoRepository;
import com.bpc.escola.repository.ReservaAulaRepository;
import com.bpc.escola.repository.ReservaColetivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

@Service
@RequiredArgsConstructor
public class PlanoValidacaoService {

    private static final int WELLHUB_LIMITE_MES_PADRAO = 8;

    private final AlunoPlanoRepository alunoPlanoRepository;
    private final ReservaAulaRepository reservaAulaRepository;
    private final ReservaColetivaRepository reservaColetivaRepository;

    public AlunoPlano obterPlanoAtivo(Usuario aluno) {
        return alunoPlanoRepository.findFirstByAlunoAndAtivoTrue(aluno)
                .orElseThrow(() -> new BusinessException("Aluno sem plano ativo.", "PLANO_INATIVO"));
    }

    public void validarReservaAula(Usuario aluno, LocalDate dataReserva) {
        validarReservaColetiva(aluno, dataReserva);
    }

    public void validarReservaColetiva(Usuario aluno, LocalDate dataReserva) {
        AlunoPlano alunoPlano = obterPlanoAtivo(aluno);
        Plano plano = alunoPlano.getPlano();
        TipoPlano tipo = plano.getTipoPlano();

        if (tipo == TipoPlano.ILIMITADO || Boolean.TRUE.equals(plano.getIlimitado())) {
            return;
        }

        if (tipo == TipoPlano.WELLHUB || plano.getQuantidadeAulasMes() != null) {
            int limite = plano.getQuantidadeAulasMes() != null
                    ? plano.getQuantidadeAulasMes()
                    : WELLHUB_LIMITE_MES_PADRAO;
            validarLimiteMensal(aluno, dataReserva, limite);
            return;
        }

        WeekFields weekFields = WeekFields.ISO;
        LocalDate inicioSemana = dataReserva.with(weekFields.dayOfWeek(), 1);
        LocalDate fimSemana = inicioSemana.plusDays(6);

        long reservasNaSemana = reservaColetivaRepository.countByAlunoAndStatusAndDataReservaBetween(
                aluno, StatusReserva.CONFIRMADA, inicioSemana, fimSemana)
                + reservaAulaRepository.countByAlunoAndStatusAndDataReservaBetween(
                aluno, StatusReserva.CONFIRMADA, inicioSemana, fimSemana);

        int limite = switch (tipo) {
            case UMA_AULA_SEMANA -> 1;
            case DUAS_AULAS_SEMANA -> 2;
            case TRES_AULAS_SEMANA -> 3;
            default -> Integer.MAX_VALUE;
        };

        if (reservasNaSemana >= limite) {
            throw new BusinessException(
                    "Limite de aulas por semana atingido para o plano (" + limite + "/semana).",
                    "PLANO_LIMITE_SEMANA");
        }
    }

    private void validarLimiteMensal(Usuario aluno, LocalDate dataReserva, int limite) {
        LocalDate inicioMes = dataReserva.withDayOfMonth(1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        long reservasNoMes = reservaColetivaRepository.countByAlunoAndStatusAndDataReservaBetween(
                aluno, StatusReserva.CONFIRMADA, inicioMes, fimMes)
                + reservaAulaRepository.countByAlunoAndStatusAndDataReservaBetween(
                aluno, StatusReserva.CONFIRMADA, inicioMes, fimMes);

        if (reservasNoMes >= limite) {
            throw new BusinessException(
                    "Limite de aulas por mês atingido para o plano (" + limite + "/mês).",
                    "PLANO_LIMITE_MES");
        }
    }

    public void validarReservaEmbarcacao(Usuario aluno) {
        AlunoPlano alunoPlano = obterPlanoAtivo(aluno);
        if (alunoPlano.getPlano().getTipoPlano() != TipoPlano.AVULSO_REMADAS) {
            return;
        }
        Integer total = alunoPlano.getPlano().getQuantidadeRemadas();
        if (total != null && alunoPlano.getRemadasConsumidas() >= total) {
            throw new BusinessException("Pacote avulso sem remadas disponíveis.", "PLANO_REMADAS_ESGOTADAS");
        }
    }

    public void consumirRemada(Usuario aluno) {
        AlunoPlano alunoPlano = obterPlanoAtivo(aluno);
        if (alunoPlano.getPlano().getTipoPlano() == TipoPlano.AVULSO_REMADAS) {
            alunoPlano.setRemadasConsumidas(alunoPlano.getRemadasConsumidas() + 1);
            alunoPlanoRepository.save(alunoPlano);
        }
    }
}
