package com.bpc.escola.seed;

import com.bpc.escola.domain.*;
import com.bpc.escola.domain.enums.*;
import com.bpc.escola.repository.*;
import com.bpc.escola.service.RelogioSaoPaulo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
@org.springframework.core.annotation.Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;
    private final AlunoPlanoRepository alunoPlanoRepository;
    private final EmbarcacaoRepository embarcacaoRepository;
    private final ComposicaoEmbarcacaoRepository composicaoRepository;
    private final HorarioColetivoRepository horarioRepository;
    private final ReservaColetivaRepository reservaColetivaRepository;
    private final ReservaEmbarcacaoRepository reservaEmbarcacaoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final ManutencaoRepository manutencaoRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || usuarioRepository.count() > 0) {
            return;
        }
        seed();
    }

    private void seed() {
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nome("Admin Recepção").email("admin@bpc.com").telefone("11999990001")
                .tipoUsuario(TipoUsuario.ADMIN).senha(UsuarioSenhaSeeder.SENHA_DEMO).build());

        Usuario prof1 = usuarioRepository.save(Usuario.builder()
                .nome("Prof. Ricardo").email("ricardo@bpc.com").telefone("11999990002")
                .tipoUsuario(TipoUsuario.PROFESSOR).senha(UsuarioSenhaSeeder.SENHA_DEMO).build());
        Usuario prof2 = usuarioRepository.save(Usuario.builder()
                .nome("Prof. Marina").email("marina@bpc.com").telefone("11999990003")
                .tipoUsuario(TipoUsuario.PROFESSOR).senha(UsuarioSenhaSeeder.SENHA_DEMO).build());
        usuarioRepository.save(Usuario.builder()
                .nome("Equipe Manutenção").email("manutencao@bpc.com").telefone("11999990099")
                .tipoUsuario(TipoUsuario.MANUTENCAO).senha(UsuarioSenhaSeeder.SENHA_DEMO).build());

        List<Usuario> alunos = new ArrayList<>();
        String[] nomes = {
                "Ana Silva", "Bruno Costa", "Carla Mendes", "Diego Lima", "Elena Souza",
                "Felipe Rocha", "Gabriela Dias", "Henrique Alves", "Isabela Nunes", "João Pedro",
                "Karina Martins", "Lucas Ferreira", "Mariana Teixeira", "Nicolas Barbosa", "Olivia Campos",
                "Paulo Henrique", "Rafaela Lima", "Samuel Dias", "Tatiana Rocha", "Ulisses Mendes",
                "Valentina Souza", "Wagner Alves", "Ximena Costa", "Yasmin Teixeira", "Zeca Ferreira",
                "Amanda Nunes", "Bernardo Silva", "Camila Barbosa", "Daniel Campos", "Elisa Martins"
        };
        for (int i = 0; i < nomes.length; i++) {
            alunos.add(usuarioRepository.save(Usuario.builder()
                    .nome(nomes[i]).email("aluno" + (i + 1) + "@bpc.com").telefone("1198888" + String.format("%04d", i))
                    .tipoUsuario(TipoUsuario.ALUNO).senha(UsuarioSenhaSeeder.SENHA_DEMO).build()));
        }

        vincularPlanosAlunos(alunos);

        List<Embarcacao> embarcacoes = criarEmbarcacoes();
        List<HorarioColetivo> horarios = criarHorariosColetivos();
        criarReservasColetivas(alunos, horarios, embarcacoes);
        criarOcorrenciasEManutencoes(embarcacoes, admin);
    }

    private void vincularPlanosAlunos(List<Usuario> alunos) {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        Plano plano1x = planoRepository.findByNome("1x Semana — Mensal").orElseThrow();
        Plano plano2x = planoRepository.findByNome("2x Semana — Mensal").orElseThrow();
        Plano plano3x = planoRepository.findByNome("3x Semana — Mensal").orElseThrow();
        Plano planoLivre = planoRepository.findByNome("Livre — Mensal").orElseThrow();
        Plano pacote10 = planoRepository.findByNome("Pacote 10 Remadas").orElseThrow();
        Plano remadaAvulsa = planoRepository.findByNome("Remada Avulsa — Adulto").orElseThrow();
        Plano wellhub = planoRepository.findByNome("Wellhub").orElseThrow();

        for (int i = 0; i < alunos.size(); i++) {
            if (i % 10 == 9) {
                continue;
            }
            Plano p = switch (i % 10) {
                case 0, 1 -> plano1x;
                case 2, 3 -> plano2x;
                case 4 -> plano3x;
                case 5 -> planoLivre;
                case 6, 7 -> pacote10;
                case 8 -> remadaAvulsa;
                default -> wellhub;
            };
            int validade = p.getValidadeMeses() != null ? p.getValidadeMeses() : 12;
            alunoPlanoRepository.save(AlunoPlano.builder()
                    .aluno(alunos.get(i)).plano(p).dataInicio(hoje.minusMonths(1))
                    .dataFim(hoje.plusMonths(validade - 1L)).ativo(true).build());
        }
    }

    private List<Embarcacao> criarEmbarcacoes() {
        List<Embarcacao> lista = new ArrayList<>();
        lista.add(criarEmb("OC1 Alpha", TipoEmbarcacao.OC1, 1, StatusEmbarcacao.DISPONIVEL));
        lista.add(criarEmb("OC2 Beta", TipoEmbarcacao.OC2, 2, StatusEmbarcacao.DISPONIVEL));
        lista.add(criarEmb("OC3 Gamma", TipoEmbarcacao.OC3, 3, StatusEmbarcacao.DISPONIVEL));
        lista.add(criarEmb("OC4 Delta", TipoEmbarcacao.OC4, 4, StatusEmbarcacao.DISPONIVEL));

        Embarcacao oc6a = criarEmb("OC6 #1", TipoEmbarcacao.OC6, 6, StatusEmbarcacao.DISPONIVEL);
        Embarcacao oc6b = criarEmb("OC6 #2", TipoEmbarcacao.OC6, 6, StatusEmbarcacao.INTERDITADA);
        Embarcacao oc6c = criarEmb("OC6 #3", TipoEmbarcacao.OC6, 6, StatusEmbarcacao.DISPONIVEL);
        Embarcacao oc6d = criarEmb("OC6 #4", TipoEmbarcacao.OC6, 6, StatusEmbarcacao.MANUTENCAO);
        Embarcacao oc6e = criarEmb("OC6 #5", TipoEmbarcacao.OC6, 6, StatusEmbarcacao.DISPONIVEL);
        Embarcacao oc6f = criarEmb("OC6 #6", TipoEmbarcacao.OC6, 6, StatusEmbarcacao.INTERDITADA);
        lista.addAll(List.of(oc6a, oc6b, oc6c, oc6d, oc6e, oc6f));

        Embarcacao kat1 = criarEmb("Katamarã Hōkūleʻa", TipoEmbarcacao.KATAMARA, 12, StatusEmbarcacao.DISPONIVEL);
        Embarcacao kat2 = criarEmb("Katamarã Moana", TipoEmbarcacao.KATAMARA, 12, StatusEmbarcacao.DISPONIVEL);
        lista.add(kat1);
        lista.add(kat2);

        Embarcacao tri1 = criarEmb("Trimarã Kai", TipoEmbarcacao.TRIMARA, 18, StatusEmbarcacao.DISPONIVEL);
        Embarcacao tri2 = criarEmb("Trimarã Nalu", TipoEmbarcacao.TRIMARA, 18, StatusEmbarcacao.DISPONIVEL);
        lista.add(tri1);
        lista.add(tri2);

        composicaoRepository.save(ComposicaoEmbarcacao.builder().embarcacaoPrincipal(tri1).embarcacaoFilha(oc6a).build());
        composicaoRepository.save(ComposicaoEmbarcacao.builder().embarcacaoPrincipal(tri1).embarcacaoFilha(oc6c).build());
        composicaoRepository.save(ComposicaoEmbarcacao.builder().embarcacaoPrincipal(tri1).embarcacaoFilha(oc6e).build());

        composicaoRepository.save(ComposicaoEmbarcacao.builder().embarcacaoPrincipal(tri2).embarcacaoFilha(oc6b).build());
        composicaoRepository.save(ComposicaoEmbarcacao.builder().embarcacaoPrincipal(tri2).embarcacaoFilha(oc6d).build());
        composicaoRepository.save(ComposicaoEmbarcacao.builder().embarcacaoPrincipal(tri2).embarcacaoFilha(oc6f).build());

        manutencaoRepository.save(Manutencao.builder()
                .embarcacao(oc6d).descricao("Reparo casco").dataInicio(RelogioSaoPaulo.hoje().minusDays(2))
                .status(StatusManutencao.EM_ANDAMENTO).build());

        return lista;
    }

    private Embarcacao criarEmb(String nome, TipoEmbarcacao tipo, int cap, StatusEmbarcacao status) {
        return embarcacaoRepository.save(Embarcacao.builder()
                .nome(nome).tipo(tipo).capacidade(cap).status(status).build());
    }

    private List<HorarioColetivo> criarHorariosColetivos() {
        Map<DiaSemana, List<LocalTime[]>> mapa = HorariosFixos.horarios();
        List<HorarioColetivo> horarios = new ArrayList<>();
        for (var entry : mapa.entrySet()) {
            DiaSemana dia = entry.getKey();
            for (LocalTime[] slot : entry.getValue()) {
                String titulo = String.format("Coletiva %s %s-%s", dia.name().substring(0, 3), slot[0], slot[1]);
                horarios.add(horarioRepository.save(HorarioColetivo.builder()
                        .titulo(titulo)
                        .diaSemana(dia)
                        .horarioInicio(slot[0])
                        .horarioFim(slot[1])
                        .capacidadeSlot(42)
                        .build()));
            }
        }
        return horarios;
    }

    private void criarReservasColetivas(List<Usuario> alunos, List<HorarioColetivo> horarios, List<Embarcacao> embarcacoes) {
        LocalDate hoje = RelogioSaoPaulo.hoje();
        LocalTime agora = RelogioSaoPaulo.hora();
        DiaSemana dia = diaFromDate(hoje);
        int[] quantidades = {0, 3, 5, 8, 18, 4, 7, 2, 9, 11, 6, 1};
        OrigemReserva[] origens = {OrigemReserva.APP, OrigemReserva.MANUAL, OrigemReserva.WELLHUB};

        List<HorarioColetivo> horariosHoje = horarios.stream()
                .filter(h -> h.getDiaSemana() == dia)
                .sorted(Comparator.comparing(HorarioColetivo::getHorarioInicio))
                .toList();

        int alunoIdx = 0;
        for (int i = 0; i < horariosHoje.size(); i++) {
            HorarioColetivo horario = horariosHoje.get(i);
            int qtd = Math.min(quantidades[i % quantidades.length], horario.getCapacidadeSlot());
            Set<Long> usados = new HashSet<>();
            int criadas = 0;
            while (criadas < qtd && alunoIdx < alunos.size() * 3) {
                Usuario aluno = alunos.get(alunoIdx % alunos.size());
                alunoIdx++;
                if (!usados.add(aluno.getId())) continue;
                boolean slotIniciou = !agora.isBefore(horario.getHorarioInicio());
                boolean presente = slotIniciou && criadas < 2;
                reservaColetivaRepository.save(ReservaColetiva.builder()
                        .horario(horario).aluno(aluno)
                        .status(StatusReserva.CONFIRMADA)
                        .origem(origens[criadas % origens.length])
                        .dataReserva(hoje)
                        .presente(presente)
                        .criadoEm(RelogioSaoPaulo.dataHora())
                        .build());
                criadas++;
            }
        }

        Embarcacao oc1 = embarcacoes.stream().filter(e -> e.getTipo() == TipoEmbarcacao.OC1).findFirst().orElseThrow();
        reservaEmbarcacaoRepository.save(ReservaEmbarcacao.builder()
                .aluno(alunos.get(0)).embarcacao(oc1).data(hoje)
                .horarioInicio(LocalTime.of(10, 0)).horarioFim(LocalTime.of(11, 0))
                .status(StatusReserva.CONFIRMADA).criadoEm(RelogioSaoPaulo.dataHora()).build());

        Embarcacao tri = embarcacoes.stream().filter(e -> e.getNome().contains("Kai")).findFirst().orElseThrow();
        reservaEmbarcacaoRepository.save(ReservaEmbarcacao.builder()
                .aluno(alunos.get(1)).embarcacao(tri).data(hoje.plusDays(1))
                .horarioInicio(LocalTime.of(7, 0)).horarioFim(LocalTime.of(8, 0))
                .status(StatusReserva.CONFIRMADA).criadoEm(RelogioSaoPaulo.dataHora()).build());
    }

    private void criarOcorrenciasEManutencoes(List<Embarcacao> embarcacoes, Usuario admin) {
        Embarcacao oc6Interditada = embarcacoes.stream()
                .filter(e -> e.getStatus() == StatusEmbarcacao.INTERDITADA).findFirst().orElseThrow();

        ocorrenciaRepository.save(Ocorrencia.builder()
                .embarcacao(oc6Interditada).usuario(admin)
                .titulo("Rachadura no casco").descricao("Identificada na inspeção matinal")
                .gravidade(GravidadeOcorrencia.ALTA).status(StatusOcorrencia.ABERTA)
                .dataAbertura(RelogioSaoPaulo.dataHora().minusDays(1)).build());

        ocorrenciaRepository.save(Ocorrencia.builder()
                .embarcacao(embarcacoes.get(0)).usuario(admin)
                .titulo("Remo com desgaste").descricao("Trocar remo #3")
                .gravidade(GravidadeOcorrencia.BAIXA).status(StatusOcorrencia.EM_ANALISE)
                .dataAbertura(RelogioSaoPaulo.dataHora().minusHours(5)).build());
    }

    private DiaSemana diaFromDate(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}
