const FUSO_SAO_PAULO = "America/Sao_Paulo";

/** Data de hoje no fuso de São Paulo (yyyy-MM-dd). */
export function hojeSaoPaulo(): string {
  return new Intl.DateTimeFormat("en-CA", { timeZone: FUSO_SAO_PAULO }).format(new Date());
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
