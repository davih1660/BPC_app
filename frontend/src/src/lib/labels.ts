import type { StatusEmbarcacao, StatusOcorrencia, GravidadeOcorrencia, DiaSemana, StatusSlot } from "./types";

export const statusEmbarcacaoLabel: Record<StatusEmbarcacao, string> = {
  DISPONIVEL: "Disponível",
  RESERVADA: "Reservada",
  EM_AULA: "Em aula",
  MANUTENCAO: "Manutenção",
  INTERDITADA: "Interditada",
};

export const statusEmbarcacaoClass: Record<StatusEmbarcacao, string> = {
  DISPONIVEL: "bg-emerald-100 text-emerald-800 border-emerald-200",
  RESERVADA: "bg-amber-100 text-amber-800 border-amber-200",
  EM_AULA: "bg-blue-100 text-blue-800 border-blue-200",
  MANUTENCAO: "bg-orange-100 text-orange-800 border-orange-200",
  INTERDITADA: "bg-red-100 text-red-800 border-red-200",
};

export const statusOcorrenciaClass: Record<StatusOcorrencia, string> = {
  ABERTA: "bg-red-100 text-red-800",
  EM_ANALISE: "bg-amber-100 text-amber-800",
  RESOLVIDA: "bg-emerald-100 text-emerald-800",
};

export const gravidadeClass: Record<GravidadeOcorrencia, string> = {
  BAIXA: "bg-slate-100 text-slate-700",
  MEDIA: "bg-amber-100 text-amber-800",
  ALTA: "bg-red-100 text-red-800",
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

export const statusSlotClass: Record<StatusSlot, string> = {
  EM_ANDAMENTO: "bg-emerald-100 text-emerald-800 border-emerald-300",
  PROXIMA: "bg-sky-100 text-sky-800 border-sky-300",
  ENCERRADA: "bg-slate-100 text-slate-600 border-slate-200",
  AGENDADA: "bg-slate-50 text-slate-700 border-slate-200",
};

export const statusSlotCardClass: Record<StatusSlot, string> = {
  EM_ANDAMENTO: "border-emerald-400 ring-2 ring-emerald-100",
  PROXIMA: "border-sky-400 ring-2 ring-sky-100",
  ENCERRADA: "border-slate-200 opacity-70",
  AGENDADA: "border-slate-200",
};

export function formatHorario(inicio: string, fim: string) {
  return `${inicio.slice(0, 5)} – ${fim.slice(0, 5)}`;
}
