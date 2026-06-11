import type { ChipVariant } from "./design-tokens";
import type {
  StatusEmbarcacao,
  StatusOcorrencia,
  StatusManutencao,
  GravidadeOcorrencia,
  DiaSemana,
  StatusSlot,
  SituacaoAluno,
  CategoriaPlano,
  PeriodicidadePlano,
  TipoPlano,
  OrigemReserva,
  EstadoSessao,
  StatusSolicitacaoUsoLivre,
  TipoCanoaUsoLivre,
  TipoEmbarcacao,
} from "./types";

export const statusEmbarcacaoLabel: Record<StatusEmbarcacao, string> = {
  DISPONIVEL: "Disponível",
  RESERVADA: "Reservada",
  EM_AULA: "Em aula",
  MANUTENCAO: "Manutenção",
  INTERDITADA: "Interditada",
};

export const statusEmbarcacaoVariant: Record<StatusEmbarcacao, ChipVariant> = {
  DISPONIVEL: "success",
  RESERVADA: "warning",
  EM_AULA: "info",
  MANUTENCAO: "warning",
  INTERDITADA: "error",
};

/** @deprecated use statusEmbarcacaoVariant */
export const statusEmbarcacaoClass: Record<StatusEmbarcacao, string> = {
  DISPONIVEL: "",
  RESERVADA: "",
  EM_AULA: "",
  MANUTENCAO: "",
  INTERDITADA: "",
};

export const statusOcorrenciaLabel: Record<StatusOcorrencia, string> = {
  ABERTA: "Aberta",
  EM_ANALISE: "Em análise",
  RESOLVIDA: "Resolvida",
};

export const statusOcorrenciaVariant: Record<StatusOcorrencia, ChipVariant> = {
  ABERTA: "error",
  EM_ANALISE: "warning",
  RESOLVIDA: "success",
};

/** @deprecated use statusOcorrenciaVariant */
export const statusOcorrenciaClass = statusOcorrenciaVariant;

export const statusManutencaoLabel: Record<StatusManutencao, string> = {
  AGENDADA: "Agendada",
  EM_ANDAMENTO: "Em andamento",
  CONCLUIDA: "Concluída",
};

export const statusManutencaoVariant: Record<StatusManutencao, ChipVariant> = {
  AGENDADA: "info",
  EM_ANDAMENTO: "warning",
  CONCLUIDA: "success",
};

export const gravidadeVariant: Record<GravidadeOcorrencia, ChipVariant> = {
  BAIXA: "neutral",
  MEDIA: "warning",
  ALTA: "error",
};

/** @deprecated use gravidadeVariant */
export const gravidadeClass = gravidadeVariant;

export const gravidadeLabel: Record<GravidadeOcorrencia, string> = {
  BAIXA: "Baixa",
  MEDIA: "Média",
  ALTA: "Alta",
};

export const diaSemanaLabel: Record<DiaSemana, string> = {
  SEGUNDA: "Segunda",
  TERCA: "Terça",
  QUARTA: "Quarta",
  QUINTA: "Quinta",
  SEXTA: "Sexta",
  SABADO: "Sábado",
  DOMINGO: "Domingo",
};

export const statusSlotLabel: Record<StatusSlot, string> = {
  EM_ANDAMENTO: "Aula em andamento",
  PROXIMA: "Próxima aula",
  ENCERRADA: "Encerrada",
  AGENDADA: "Agendada",
};

export const statusSlotVariant: Record<StatusSlot, ChipVariant> = {
  EM_ANDAMENTO: "success",
  PROXIMA: "info",
  ENCERRADA: "neutral",
  AGENDADA: "neutral",
};

/** @deprecated use statusSlotVariant */
export const statusSlotClass = statusSlotVariant;

export const statusSlotCardClass: Record<StatusSlot, string> = {
  EM_ANDAMENTO: "border-success/40 bg-success-bg/30",
  PROXIMA: "border-primary/40 bg-primary-container/50",
  ENCERRADA: "border-outline opacity-70",
  AGENDADA: "border-outline",
};

export function formatHorario(inicio: string, fim: string) {
  return `${inicio.slice(0, 5)} – ${fim.slice(0, 5)}`;
}

export const situacaoAlunoLabel: Record<SituacaoAluno, string> = {
  PLANO: "Plano",
  AVULSO: "Avulso",
  PACOTE: "Pacote",
  WELLHUB: "Wellhub",
  SEM_PLANO: "Sem plano",
};

export const situacaoAlunoVariant: Record<SituacaoAluno, ChipVariant> = {
  PLANO: "info",
  AVULSO: "warning",
  PACOTE: "warning",
  WELLHUB: "default",
  SEM_PLANO: "neutral",
};

/** @deprecated use situacaoAlunoVariant */
export const situacaoAlunoClass = situacaoAlunoVariant;

export const categoriaPlanoLabel: Record<CategoriaPlano, string> = {
  RECORRENTE: "Plano recorrente",
  AVULSO: "Avulso",
  WELLHUB: "Wellhub",
  EQUIPAMENTO: "Equipamentos",
};

export const periodicidadePlanoLabel: Record<PeriodicidadePlano, string> = {
  MENSAL: "Mensal",
  TRIMESTRAL: "Trimestral",
  SEMESTRAL: "Semestral",
  ANUAL: "Anual",
};

export const tipoPlanoLabel: Record<TipoPlano, string> = {
  UMA_AULA_SEMANA: "1x/semana",
  DUAS_AULAS_SEMANA: "2x/semana",
  TRES_AULAS_SEMANA: "3x/semana",
  ILIMITADO: "Livre",
  AVULSO_REMADAS: "Remadas avulsas",
  WELLHUB: "8 aulas/mês",
};

export const origemReservaLabel: Record<OrigemReserva, string> = {
  APP: "App BPC",
  WELLHUB: "Wellhub",
  MANUAL: "Manual",
};

export const origemReservaVariant: Record<OrigemReserva, ChipVariant> = {
  APP: "info",
  WELLHUB: "default",
  MANUAL: "neutral",
};

/** @deprecated use origemReservaVariant */
export const origemReservaClass = origemReservaVariant;

export const estadoSessaoLabel: Record<EstadoSessao, string> = {
  AGUARDANDO: "Aguardando",
  CHAMADA: "Chamada",
  EM_AGUA: "Em água",
  ENCERRADA: "Encerrada",
};

export function formatMoeda(valor?: number) {
  if (valor == null) return "—";
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export const tipoCanoaUsoLivreLabel: Record<TipoCanoaUsoLivre, string> = {
  OC1: "OC1 (1 lugar)",
  OC2: "OC2 (2 lugares)",
  OC3: "OC3 (3 lugares)",
  OC4: "OC4 (4 lugares)",
  OC6: "OC6 (6 lugares)",
};

export const tipoEmbarcacaoLabel: Record<TipoEmbarcacao, string> = {
  OC1: "OC1",
  OC2: "OC2",
  OC3: "OC3",
  OC4: "OC4",
  OC6: "OC6",
  KATAMARA: "Katamarã",
  TRIMARA: "Trimarã",
};

export const statusSolicitacaoUsoLivreLabel: Record<StatusSolicitacaoUsoLivre, string> = {
  PENDENTE: "Aguardando aprovação",
  APROVADA: "Aprovada",
  RECUSADA: "Recusada",
  CANCELADA: "Cancelada",
};

export const statusSolicitacaoUsoLivreVariant: Record<StatusSolicitacaoUsoLivre, ChipVariant> = {
  PENDENTE: "warning",
  APROVADA: "success",
  RECUSADA: "error",
  CANCELADA: "neutral",
};
