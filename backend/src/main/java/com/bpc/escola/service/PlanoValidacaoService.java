package com.bpc.escola.service;

import com.bpc.escola.domain.AlunoPlano;
import com.bpc.escola.domain.Usuario;
import com.bpc.escola.domain.enums.StatusReserva;
import com.bpc.escola.domain.enums.TipoPlano;
import com.bpc.escola.exception.BusinessException;
import com.bpc.escola.repository.AlunoPlanoRepository;
import com.bpc.escola.repository.ReservaAulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
@Service
@RequiredArgsConstructor
public class PlanoValidacaoService {

    private final AlunoPlanoRepository alunoPlanoRepository;
    private final ReservaAulaRepository reservaAulaRepository;

    public AlunoPlano obterPlanoAtivo(Usuario aluno) {
        return alunoPlanoRepository.findFirstByAlunoAndAtivoTrue(aluno)
                .orElseThrow(() -> new BusinessException("Aluno sem plano ativo.", "PLANO_INATIVO"));
    }

    public void validarReservaAula(Usuario aluno, LocalDate dataReserva) {
        AlunoPlano alunoPlano = obterPlanoAtivo(aluno);
        TipoPlano tipo = alunoPlano.getPlano().getTipoPlano();

        if (tipo == TipoPlano.ILIMITADO || Boolean.TRUE.equals(alunoPlano.getPlano().getIlimitado())) {
            return;
        }

        WeekFields weekFields = WeekFields.ISO;
        LocalDate inicioSemana = dataReserva.with(weekFields.dayOfWeek(), 1);
        LocalDate fimSemana = inicioSemana.plusDays(6);

        long reservasNaSemana = reservaAulaRepository.countByAlunoAndStatusAndDataReservaBetween(
                aluno, StatusReserva.CONFIRMADA, inicioSemana, fimSemana);

        int limite = switch (tipo) {
            case UMA_AULA_SEMANA -> 1;
            case DUAS_AULAS_SEMANA -> 2;
            default -> Integer.MAX_VALUE;
        };

        if (reservasNaSemana >= limite) {
            throw new BusinessException(
                    "Limite de aulas por semana atingido para o plano (" + limite + "/semana).",
                    "PLANO_LIMITE_SEMANA");
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
