export type TipoUsuario = "ADMIN" | "PROFESSOR" | "ALUNO";
export type TipoPlano = "UMA_AULA_SEMANA" | "DUAS_AULAS_SEMANA" | "ILIMITADO" | "AVULSO_REMADAS";
export type TipoEmbarcacao = "OC1" | "OC2" | "OC3" | "OC4" | "OC6" | "KATAMARA" | "TRIMARA";
export type StatusEmbarcacao = "DISPONIVEL" | "RESERVADA" | "EM_AULA" | "MANUTENCAO" | "INTERDITADA";
export type DiaSemana = "SEGUNDA" | "TERCA" | "QUARTA" | "QUINTA" | "SEXTA" | "SABADO" | "DOMINGO";
export type StatusReserva = "CONFIRMADA" | "CANCELADA" | "CONCLUIDA";
export type StatusOcorrencia = "ABERTA" | "EM_ANALISE" | "RESOLVIDA";
export type GravidadeOcorrencia = "BAIXA" | "MEDIA" | "ALTA";
export type StatusSlot = "EM_ANDAMENTO" | "PROXIMA" | "ENCERRADA" | "AGENDADA";

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  telefone?: string;
  tipoUsuario: TipoUsuario;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Plano {
  id: number;
  nome: string;
  tipoPlano: TipoPlano;
  quantidadeAulasSemana?: number;
  quantidadeRemadas?: number;
  validadeMeses?: number;
  ilimitado: boolean;
}

export interface AlunoPlano {
  id: number;
  alunoId: number;
  alunoNome: string;
  plano: Plano;
  dataInicio: string;
  dataFim?: string;
  aulasConsumidasSemana: number;
  remadasConsumidas: number;
  ativo: boolean;
}

export interface Embarcacao {
  id: number;
  nome: string;
  tipo: TipoEmbarcacao;
  capacidade: number;
  status: StatusEmbarcacao;
  statusEfetivo?: StatusEmbarcacao;
  observacoes?: string;
}

export interface Aula {
  id: number;
  titulo: string;
  diaSemana: DiaSemana;
  horarioInicio: string;
  horarioFim: string;
  capacidadeMaxima: number;
  professorId: number;
  professorNome: string;
  embarcacaoPrincipalId: number;
  embarcacaoPrincipalNome: string;
}

export interface ReservaAula {
  id: number;
  aulaId: number;
  aulaTitulo: string;
  alunoId: number;
  alunoNome: string;
  status: StatusReserva;
  dataReserva: string;
  presente: boolean;
}

export interface ReservaEmbarcacao {
  id: number;
  alunoId: number;
  alunoNome: string;
  embarcacaoId: number;
  embarcacaoNome: string;
  data: string;
  horarioInicio: string;
  horarioFim: string;
  status: StatusReserva;
}

export interface OcorrenciaImagem {
  id: number;
  nomeOriginal: string;
  contentType: string;
  tamanhoBytes: number;
}

export interface Ocorrencia {
  id: number;
  embarcacaoId: number;
  embarcacaoNome: string;
  usuarioId: number;
  usuarioNome: string;
  titulo: string;
  descricao?: string;
  gravidade: GravidadeOcorrencia;
  status: StatusOcorrencia;
  dataAbertura: string;
  imagens: OcorrenciaImagem[];
}

export interface Manutencao {
  id: number;
  embarcacaoId: number;
  embarcacaoNome: string;
  descricao?: string;
  dataInicio: string;
  dataFim?: string;
  status: string;
}

export interface OperacaoAula {
  aula: Aula;
  statusSlot: StatusSlot;
  data: string;
  inscritos: ReservaAula[];
  totalInscritos: number;
  capacidade: number;
  lotada: boolean;
}

export interface OperacaoDia {
  destaque: OperacaoAula | null;
  slotsDoDia: OperacaoAula[];
  alunosNoDia: number;
}

export interface OperacaoAulaResumo {
  aula: Aula;
  totalInscritos: number;
  capacidade: number;
  lotada: boolean;
}

export interface BlocoHorarioOperacao {
  horarioInicio: string;
  horarioFim: string;
  statusBloco: StatusSlot;
  aulas: OperacaoAulaResumo[];
}

export interface ProximasAulasOperacionais {
  imediato: BlocoHorarioOperacao | null;
  seguinte: BlocoHorarioOperacao | null;
}

export interface Dashboard {
  destaque: OperacaoAula | null;
  proximasAulas: ProximasAulasOperacionais;
  embarcacoesDisponiveis: Embarcacao[];
  ocorrenciasAbertas: Ocorrencia[];
  aulasLotadas: { aulaId: number; titulo: string; data: { data: string }; inscritos: number; capacidade: number }[];
  alunosNoDia: number;
}

export interface AgendaEvento {
  tipo: string;
  id: number;
  titulo: string;
  diaSemana: DiaSemana;
  data: string;
  horarioInicio: string;
  horarioFim: string;
  embarcacaoNome: string;
  statusEmbarcacao?: StatusEmbarcacao;
  alunoNome?: string;
  inscritos: number;
  capacidade: number;
}

export interface Agenda {
  dataInicio: string;
  dataFim: string;
  eventos: AgendaEvento[];
}
