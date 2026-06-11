export type TipoUsuario = "ADMIN" | "PROFESSOR" | "ALUNO" | "MANUTENCAO";
export type TipoPlano =
  | "UMA_AULA_SEMANA"
  | "DUAS_AULAS_SEMANA"
  | "TRES_AULAS_SEMANA"
  | "ILIMITADO"
  | "AVULSO_REMADAS"
  | "WELLHUB";
export type CategoriaPlano = "RECORRENTE" | "AVULSO" | "WELLHUB" | "EQUIPAMENTO";
export type PeriodicidadePlano = "MENSAL" | "TRIMESTRAL" | "SEMESTRAL" | "ANUAL";
export type SituacaoAluno = "PLANO" | "AVULSO" | "PACOTE" | "WELLHUB" | "SEM_PLANO";
export type OrigemReserva = "APP" | "WELLHUB" | "MANUAL";
export type EstadoSessao = "AGUARDANDO" | "CHAMADA" | "EM_AGUA" | "ENCERRADA";
export type TipoEmbarcacao = "OC1" | "OC2" | "OC3" | "OC4" | "OC6" | "KATAMARA" | "TRIMARA";
export type StatusEmbarcacao = "DISPONIVEL" | "RESERVADA" | "EM_AULA" | "MANUTENCAO" | "INTERDITADA";
export type DiaSemana = "SEGUNDA" | "TERCA" | "QUARTA" | "QUINTA" | "SEXTA" | "SABADO" | "DOMINGO";
export type StatusReserva = "CONFIRMADA" | "CANCELADA" | "CONCLUIDA";
export type StatusOcorrencia = "ABERTA" | "EM_ANALISE" | "RESOLVIDA";
export type StatusManutencao = "AGENDADA" | "EM_ANDAMENTO" | "CONCLUIDA";
export type GravidadeOcorrencia = "BAIXA" | "MEDIA" | "ALTA";
export type StatusSlot = "EM_ANDAMENTO" | "PROXIMA" | "ENCERRADA" | "AGENDADA";
export type StatusListaEspera = "AGUARDANDO" | "PROMOVIDO" | "CANCELADO";
export type TipoNotificacao =
  | "RESERVA_CONFIRMADA"
  | "LISTA_ESPERA_PROMOVIDO"
  | "RESERVA_CANCELADA"
  | "COBRANCA_VENCIDA"
  | "AGENDA_BLOQUEADA"
  | "USO_LIVRE_APROVADO"
  | "USO_LIVRE_RECUSADO";
export type StatusSolicitacaoUsoLivre = "PENDENTE" | "APROVADA" | "RECUSADA" | "CANCELADA";
export type TipoCanoaUsoLivre = "OC1" | "OC2" | "OC3" | "OC4" | "OC6";
export type StatusCobranca = "PENDENTE" | "PAGO" | "INADIMPLENTE";
export type TipoBloqueioAgenda = "FERIADO" | "CHUVA" | "OUTRO";

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
  categoriaPlano: CategoriaPlano;
  periodicidade?: PeriodicidadePlano;
  quantidadeAulasSemana?: number;
  quantidadeAulasMes?: number;
  quantidadeRemadas?: number;
  validadeMeses?: number;
  valor?: number;
  ilimitado: boolean;
}

export interface AlunoSituacao {
  alunoId: number;
  alunoNome: string;
  alunoEmail: string;
  situacao: SituacaoAluno;
  planoId?: number;
  planoNome?: string;
  dataInicio?: string;
  dataFim?: string;
  remadasConsumidas?: number;
  quantidadeRemadas?: number;
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

export interface HorarioColetivo {
  id: number;
  titulo: string;
  diaSemana: DiaSemana;
  horarioInicio: string;
  horarioFim: string;
  capacidadeSlot: number;
}

export interface SugestaoCanoa {
  presentes: number;
  tipoSugerido: TipoEmbarcacao | null;
  descricao: string;
  capacidade: number | null;
  embarcacaoId: number | null;
  embarcacaoNome: string | null;
  disponivel: boolean;
}

export interface ReservaColetiva {
  id: number;
  horarioId: number;
  horarioTitulo: string;
  alunoId: number;
  alunoNome: string;
  situacaoAluno?: SituacaoAluno;
  status: StatusReserva;
  origem: OrigemReserva;
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

export interface GrupoCanoa {
  id: number;
  professorId: number;
  professorNome: string;
  embarcacaoId: number | null;
  embarcacaoNome: string | null;
  confirmado: boolean;
  sugestao: SugestaoCanoa;
  membros: ReservaColetiva[];
}

export interface SessaoOperacao {
  sessaoId: number;
  horario: HorarioColetivo;
  data: string;
  estado: EstadoSessao;
  reservas: ReservaColetiva[];
  presentes: ReservaColetiva[];
  professorIdsEscalados: number[];
  grupos: GrupoCanoa[];
  totalInscritos: number;
  capacidade: number;
  lotada: boolean;
  sugestaoGeral: SugestaoCanoa;
}

export interface OperacaoHorarioSlot {
  horario: HorarioColetivo;
  statusSlot: StatusSlot;
  data: string;
  inscritos: ReservaColetiva[];
  totalInscritos: number;
  capacidade: number;
  lotada: boolean;
  sugestaoPresentes: SugestaoCanoa;
  sessaoId: number | null;
}

export interface OperacaoDia {
  destaque: OperacaoHorarioSlot | null;
  slotsDoDia: OperacaoHorarioSlot[];
  alunosNoDia: number;
}

export interface OperacaoHorarioResumo {
  horario: HorarioColetivo;
  totalInscritos: number;
  capacidade: number;
  lotada: boolean;
}

export interface BlocoHorarioOperacao {
  horarioInicio: string;
  horarioFim: string;
  statusBloco: StatusSlot;
  horarios: OperacaoHorarioResumo[];
}

export interface ProximasAulasOperacionais {
  imediato: BlocoHorarioOperacao | null;
  seguinte: BlocoHorarioOperacao | null;
}

export interface Dashboard {
  destaque: OperacaoHorarioSlot | null;
  proximasAulas: ProximasAulasOperacionais;
  embarcacoesDisponiveis: Embarcacao[];
  ocorrenciasAbertas: Ocorrencia[];
  horariosLotados: { horarioId: number; titulo: string; data: { data: string }; inscritos: number; capacidade: number }[];
  alunosNoDia: number;
}

export interface WellhubSyncErro {
  id: number;
  payload: string;
  mensagem: string;
  resolvido: boolean;
  criadoEm: string;
}

export interface TipoOcorrencia {
  id: number;
  nome: string;
  gravidade: GravidadeOcorrencia;
  ativo: boolean;
  ordem: number;
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
  tipoOcorrenciaId?: number;
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
  status: StatusManutencao;
}

export interface AgendaEvento {
  tipo: string;
  id: number;
  titulo: string;
  diaSemana: DiaSemana;
  data: string;
  horarioInicio: string;
  horarioFim: string;
  embarcacaoNome?: string;
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

export interface AuthResponse {
  token: string;
  usuario: Usuario;
}

export interface Notificacao {
  id: number;
  titulo: string;
  mensagem: string;
  tipo: TipoNotificacao;
  lida: boolean;
  criadoEm: string;
  refTipo?: string;
  refId?: number;
}

export interface ListaEspera {
  id: number;
  horarioId: number;
  horarioTitulo: string;
  horarioInicio: string;
  horarioFim: string;
  alunoId: number;
  alunoNome: string;
  dataReserva: string;
  status: StatusListaEspera;
  posicao: number;
}

export interface SaldoPlano {
  alunoId: number;
  planoNome: string;
  periodo: string;
  usado: number;
  limite: number | null;
  descricao: string;
}

export interface ProximaReserva {
  reservaId: number;
  horarioId: number;
  horarioTitulo: string;
  dataReserva: string;
  horarioInicio: string;
  horarioFim: string;
  origem: OrigemReserva;
  podeCancelar: boolean;
}

export interface Cobranca {
  id: number;
  alunoId: number;
  alunoNome: string;
  planoId?: number;
  planoNome?: string;
  valor: number;
  vencimento: string;
  status: StatusCobranca;
  pagoEm?: string;
}

export interface BloqueioAgenda {
  id: number;
  data: string;
  horarioId?: number;
  horarioTitulo?: string;
  tipo: TipoBloqueioAgenda;
  motivo: string;
}

export interface SolicitacaoUsoLivre {
  id: number;
  alunoId: number;
  alunoNome: string;
  horarioId: number;
  horarioTitulo: string;
  horarioInicio: string;
  horarioFim: string;
  data: string;
  tipoCanoaDesejada: TipoCanoaUsoLivre;
  observacao?: string;
  status: StatusSolicitacaoUsoLivre;
  embarcacaoId?: number;
  embarcacaoNome?: string;
  reservaEmbarcacaoId?: number;
  motivoRecusa?: string;
  processadoPorNome?: string;
  criadoEm: string;
  processadoEm?: string;
}

export interface RelatorioResumo {
  periodo: { de: string; ate: string };
  ocupacaoMediaPercent: number;
  totalNoShow: number;
  receitaPaga: number;
  alunosAtivos: number;
  entradasListaEspera: number;
  promocoesListaEspera: number;
  ocupacaoPorHorario: {
    horarioId: number;
    titulo: string;
    totalInscritos: number;
    totalCapacidade: number;
    percentual: number;
  }[];
}
