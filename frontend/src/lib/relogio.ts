const FUSO_SAO_PAULO = "America/Sao_Paulo";

/** Data de hoje no fuso de São Paulo (yyyy-MM-dd). */
export function hojeSaoPaulo(): string {
  return new Intl.DateTimeFormat("en-CA", { timeZone: FUSO_SAO_PAULO }).format(new Date());
}

/** Minutos desde meia-noite no fuso de São Paulo. */
export function agoraMinutosSaoPaulo(): number {
  const partes = new Intl.DateTimeFormat("en-US", {
    timeZone: FUSO_SAO_PAULO,
    hour: "numeric",
    minute: "numeric",
    hour12: false,
  }).formatToParts(new Date());
  const hora = Number(partes.find((p) => p.type === "hour")?.value ?? 0);
  const minuto = Number(partes.find((p) => p.type === "minute")?.value ?? 0);
  return hora * 60 + minuto;
}

/** Converte "HH:mm" ou "HH:mm:ss" em minutos desde meia-noite. */
export function horarioParaMinutos(horario: string): number {
  const [hh, mm] = horario.split(":").map(Number);
  return hh * 60 + (mm || 0);
}

/**
 * Reserva ainda visível: data futura ou, no dia de hoje (SP),
 * enquanto o horário de término não passou (ex.: aula 7h–8h some após 8h).
 */
export function reservaAindaVigente(data: string, horarioFim: string): boolean {
  const hoje = hojeSaoPaulo();
  if (data < hoje) return false;
  if (data > hoje) return true;
  return agoraMinutosSaoPaulo() < horarioParaMinutos(horarioFim);
}

/** Ordenação cronológica crescente por data e horário de início. */
export function compararCronologicamente(
  a: { data: string; horarioInicio: string },
  b: { data: string; horarioInicio: string }
): number {
  const porData = a.data.localeCompare(b.data);
  if (porData !== 0) return porData;
  return horarioParaMinutos(a.horarioInicio) - horarioParaMinutos(b.horarioInicio);
}

/** Date com componentes de calendário de São Paulo (para date-fns). */
export function dataReferenciaSaoPaulo(): Date {
  const partes = new Intl.DateTimeFormat("en-US", {
    timeZone: FUSO_SAO_PAULO,
    year: "numeric",
    month: "numeric",
    day: "numeric",
  }).formatToParts(new Date());
  const valor = (tipo: string) =>
    Number(partes.find((p) => p.type === tipo)?.value ?? 0);
  return new Date(valor("year"), valor("month") - 1, valor("day"));
}
