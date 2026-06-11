"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { addDays, format, startOfWeek } from "date-fns";
import { ptBR } from "date-fns/locale";
import { api, ApiError } from "@/lib/api";
import type { Agenda, AgendaEvento, BloqueioAgenda, ListaEspera, ReservaColetiva } from "@/lib/types";
import { ProximaAulaCard } from "@/components/domain/proxima-aula-card";
import { Button } from "@/components/ui/button";
import { Chip } from "@/components/ui/chip";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/layout/page-header";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { statusEmbarcacaoVariant, formatHorario } from "@/lib/labels";
import { CalendarDays, ChevronLeft, ChevronRight, CloudRain } from "lucide-react";
import { toast } from "sonner";
import { dataReferenciaSaoPaulo, hojeSaoPaulo } from "@/lib/relogio";
import { useAuth } from "@/contexts/auth-context";
import { usePermissoes } from "@/hooks/use-permissoes";
import { cn } from "@/lib/utils";

type VisaoAgenda = "dia" | "semana";

function isColetiva(ev: AgendaEvento) {
  return ev.tipo === "HORARIO_COLETIVO" || ev.tipo === "AULA";
}

function vagasRestantes(ev: AgendaEvento) {
  return Math.max(0, ev.capacidade - ev.inscritos);
}

function eventosDoDia(
  eventos: Agenda["eventos"] | undefined,
  dateStr: string,
  somenteColetivas: boolean
) {
  return (eventos?.filter((e) => {
    if (e.data !== dateStr) return false;
    if (somenteColetivas) return isColetiva(e);
    return true;
  }) ?? []).sort((a, b) =>
    a.horarioInicio.localeCompare(b.horarioInicio) ||
    a.horarioFim.localeCompare(b.horarioFim)
  );
}

function DisponibilidadeChip({
  ev,
  jaReservou,
  naFila,
}: {
  ev: AgendaEvento;
  jaReservou?: boolean;
  naFila?: ListaEspera;
}) {
  const vagas = vagasRestantes(ev);
  const lotada = vagas === 0;

  if (jaReservou) return <Chip variant="info">Inscrito</Chip>;
  if (naFila) return <Chip variant="warning">Na fila (#{naFila.posicao})</Chip>;
  if (lotada) return <Chip variant="warning">Lotado</Chip>;
  return <Chip variant="success">{vagas} {vagas === 1 ? "vaga" : "vagas"}</Chip>;
}

function slotBloqueado(bloqueios: BloqueioAgenda[], horarioId: number, data: string) {
  return bloqueios.some(
    (b) => b.data === data && (b.horarioId == null || b.horarioId === horarioId)
  );
}

function bloqueioDoSlot(bloqueios: BloqueioAgenda[], horarioId: number, data: string) {
  return bloqueios.find(
    (b) => b.data === data && (b.horarioId == null || b.horarioId === horarioId)
  );
}

function BotaoAcaoHorario({
  ev,
  jaReservou,
  naFila,
  bloqueado,
  podeReservar,
  onAulaClick,
}: {
  ev: AgendaEvento;
  jaReservou: boolean;
  naFila?: ListaEspera;
  bloqueado: boolean;
  podeReservar: boolean;
  onAulaClick?: (ev: AgendaEvento) => void;
}) {
  const vagas = vagasRestantes(ev);
  const lotada = vagas === 0;

  if (!podeReservar || bloqueado) return null;

  if (jaReservou) {
    return (
      <Button variant="outline" size="sm" className="shrink-0" onClick={() => onAulaClick?.(ev)}>
        Gerenciar
      </Button>
    );
  }

  if (naFila) {
    return (
      <Button variant="outline" size="sm" className="shrink-0" disabled>
        Na fila #{naFila.posicao}
      </Button>
    );
  }

  if (lotada) {
    return (
      <Button variant="tonal" size="sm" className="shrink-0" onClick={() => onAulaClick?.(ev)}>
        Lista de espera
      </Button>
    );
  }

  return (
    <Button size="sm" className="shrink-0" onClick={() => onAulaClick?.(ev)}>
      Reservar
    </Button>
  );
}

function EventoItem({
  ev,
  compact = false,
  modoAluno = false,
  jaReservou = false,
  naFila,
  bloqueado = false,
  podeReservar = false,
  onAulaClick,
}: {
  ev: AgendaEvento;
  compact?: boolean;
  modoAluno?: boolean;
  jaReservou?: boolean;
  naFila?: ListaEspera;
  bloqueado?: boolean;
  podeReservar?: boolean;
  onAulaClick?: (ev: AgendaEvento) => void;
}) {
  const coletiva = isColetiva(ev);
  const clicavel = coletiva && onAulaClick && !modoAluno;

  const handleClick = () => {
    if (clicavel) onAulaClick?.(ev);
  };

  if (modoAluno && !compact && coletiva) {
    return (
      <div className="w-full flex gap-4 rounded-2xl border border-outline bg-surface p-4 shadow-sm">
        <div className="shrink-0 text-center min-w-[56px]">
          <p className="text-2xl font-bold text-primary leading-none">
            {ev.horarioInicio?.slice(0, 5)}
          </p>
          <p className="text-xs text-muted mt-1">{ev.horarioFim?.slice(0, 5)}</p>
        </div>
        <div className="flex-1 min-w-0">
          <p className="font-semibold text-foreground">{ev.titulo}</p>
          <p className="text-sm text-muted mt-0.5">
            {ev.inscritos}/{ev.capacidade} inscritos
          </p>
          <div className="mt-2 flex flex-wrap gap-1">
            {bloqueado && <Chip variant="error">Bloqueado</Chip>}
            <DisponibilidadeChip ev={ev} jaReservou={jaReservou} naFila={naFila} />
          </div>
        </div>
        <BotaoAcaoHorario
          ev={ev}
          jaReservou={jaReservou}
          naFila={naFila}
          bloqueado={bloqueado}
          podeReservar={podeReservar}
          onAulaClick={onAulaClick}
        />
      </div>
    );
  }

  return (
    <div
      role={clicavel ? "button" : undefined}
      tabIndex={clicavel ? 0 : undefined}
      onClick={handleClick}
      onKeyDown={(e) => {
        if (clicavel && (e.key === "Enter" || e.key === " ")) {
          e.preventDefault();
          onAulaClick?.(ev);
        }
      }}
      className={cn(
        "rounded-xl border transition-shadow",
        coletiva
          ? "bg-primary-container/30 border-primary/20 cursor-pointer hover:shadow-sm"
          : "bg-warning-bg/30 border-warning/30",
        compact ? "text-xs p-2" : "p-3"
      )}
    >
      <div className="flex items-start justify-between gap-2">
        <p className={cn("font-medium", compact && "truncate")}>{ev.titulo}</p>
        <span className="shrink-0 text-[10px] uppercase tracking-wide text-muted">
          {coletiva ? "Coletiva" : "Reserva"}
        </span>
      </div>
      <p className={cn("text-muted", compact ? "" : "text-sm mt-1")}>
        {ev.horarioInicio?.slice(0, 5)}–{ev.horarioFim?.slice(0, 5)}
      </p>
      {!coletiva && ev.alunoNome && (
        <p className={cn("font-medium text-foreground", compact ? "text-[10px] mt-0.5 truncate" : "text-sm mt-1")}>
          {ev.alunoNome}
        </p>
      )}
      {!coletiva && ev.embarcacaoNome && (
        <Badge variant={ev.statusEmbarcacao ? statusEmbarcacaoVariant[ev.statusEmbarcacao] : "warning"} className="mt-1">
          {ev.embarcacaoNome}
        </Badge>
      )}
      {coletiva && (
        <div className={cn("flex flex-wrap items-center gap-1.5", compact ? "mt-1" : "mt-2")}>
          <span className={cn("text-muted", compact ? "text-[10px]" : "text-xs")}>
            {ev.inscritos}/{ev.capacidade} inscritos
          </span>
          {modoAluno && (
            <>
              {bloqueado && <Chip variant="error">Bloqueado</Chip>}
              <DisponibilidadeChip ev={ev} jaReservou={jaReservou} naFila={naFila} />
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default function AgendaPage() {
  const router = useRouter();
  const { usuario } = useAuth();
  const { perfil, funcionalidades, perfilLabel } = usePermissoes();
  const modoAluno = perfil === "ALUNO";

  const [visao, setVisao] = useState<VisaoAgenda>(modoAluno ? "dia" : "semana");
  const [selectedDay, setSelectedDay] = useState(() => dataReferenciaSaoPaulo());
  const [weekStart, setWeekStart] = useState(() =>
    startOfWeek(dataReferenciaSaoPaulo(), { weekStartsOn: 1 })
  );
  const [agenda, setAgenda] = useState<Agenda | null>(null);
  const [minhasReservas, setMinhasReservas] = useState<ReservaColetiva[]>([]);
  const [listaEspera, setListaEspera] = useState<ListaEspera[]>([]);
  const [bloqueios, setBloqueios] = useState<BloqueioAgenda[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogAberto, setDialogAberto] = useState(false);
  const [eventoSelecionado, setEventoSelecionado] = useState<AgendaEvento | null>(null);
  const [reservando, setReservando] = useState(false);

  const isWellhub = minhasReservas.some((r) => r.situacaoAluno === "WELLHUB");
  const podeReservar = funcionalidades.reservarColetivaApp && !isWellhub;

  const chaveReserva = (horarioId: number, data: string) => `${horarioId}-${data}`;
  const reservasPorSlot = new Set(
    minhasReservas.map((r) => chaveReserva(r.horarioId, r.dataReserva))
  );
  const reservaDoSlot = (ev: AgendaEvento) =>
    minhasReservas.find((r) => r.horarioId === ev.id && r.dataReserva === ev.data);

  const filaDoSlot = (ev: AgendaEvento) =>
    listaEspera.find(
      (f) => f.horarioId === ev.id && f.dataReserva === ev.data && f.status === "AGUARDANDO"
    );

  const load = useCallback(() => {
    setLoading(true);
    const de =
      visao === "dia"
        ? format(selectedDay, "yyyy-MM-dd")
        : format(weekStart, "yyyy-MM-dd");
    const ate =
      visao === "dia"
        ? format(selectedDay, "yyyy-MM-dd")
        : format(addDays(weekStart, 6), "yyyy-MM-dd");

    const reqs: [Promise<Agenda>, ...Promise<unknown>[]] = [
      api.get<Agenda>(`/aulas/agenda?de=${de}&ate=${ate}`),
    ];
    if (modoAluno && usuario?.id) {
      reqs.push(api.get<ReservaColetiva[]>(`/reservas-coletivas?alunoId=${usuario.id}`));
      reqs.push(api.get<ListaEspera[]>(`/lista-espera?alunoId=${usuario.id}`));
    }
    const dataBloqueio =
      visao === "dia"
        ? format(selectedDay, "yyyy-MM-dd")
        : format(weekStart, "yyyy-MM-dd");
    const dataBloqueioFim =
      visao === "dia"
        ? format(selectedDay, "yyyy-MM-dd")
        : format(addDays(weekStart, 6), "yyyy-MM-dd");
    reqs.push(api.get<BloqueioAgenda[]>(`/bloqueios-agenda?de=${dataBloqueio}&ate=${dataBloqueioFim}`));

    Promise.all(reqs)
      .then((results) => {
        const ag = results[0] as Agenda;
        setAgenda(ag);
        let idx = 1;
        if (modoAluno && usuario?.id) {
          setMinhasReservas(results[idx] as ReservaColetiva[]);
          setListaEspera((results[idx + 1] as ListaEspera[]).filter((f) => f.status === "AGUARDANDO"));
          idx += 2;
        }
        setBloqueios(results[idx] as BloqueioAgenda[]);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  }, [visao, selectedDay, weekStart, modoAluno, usuario?.id]);

  useEffect(() => {
    load();
  }, [load]);

  const days = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i));
  const selectedDayStr = format(selectedDay, "yyyy-MM-dd");
  const eventosDia = eventosDoDia(agenda?.eventos, selectedDayStr, modoAluno);

  const periodoLabel =
    visao === "dia"
      ? format(selectedDay, "EEEE, dd 'de' MMMM", { locale: ptBR })
      : `${format(weekStart, "dd/MM", { locale: ptBR })} – ${format(addDays(weekStart, 6), "dd/MM/yyyy", { locale: ptBR })}`;

  const navegarAnterior = () => {
    if (visao === "dia") setSelectedDay((d) => addDays(d, -1));
    else setWeekStart((d) => addDays(d, -7));
  };

  const navegarProximo = () => {
    if (visao === "dia") setSelectedDay((d) => addDays(d, 1));
    else setWeekStart((d) => addDays(d, 7));
  };

  const aoClicarColetiva = (ev: AgendaEvento) => {
    if (modoAluno) {
      setEventoSelecionado(ev);
      setDialogAberto(true);
      return;
    }
    router.push(`/reservas?data=${ev.data}&horarioId=${ev.id}`);
  };

  const confirmarReserva = async () => {
    if (!eventoSelecionado || !usuario?.id) return;
    setReservando(true);
    try {
      await api.post("/reservas-coletivas", {
        horarioId: eventoSelecionado.id,
        alunoId: usuario.id,
        dataReserva: eventoSelecionado.data,
        origem: "APP",
      });
      toast.success("Aula coletiva reservada!");
      setDialogAberto(false);
      setEventoSelecionado(null);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setReservando(false);
    }
  };

  const entrarListaEspera = async () => {
    if (!eventoSelecionado || !usuario?.id) return;
    setReservando(true);
    try {
      await api.post("/lista-espera", {
        horarioId: eventoSelecionado.id,
        alunoId: usuario.id,
        dataReserva: eventoSelecionado.data,
      });
      toast.success("Você entrou na lista de espera!");
      setDialogAberto(false);
      setEventoSelecionado(null);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setReservando(false);
    }
  };

  const cancelarReserva = async () => {
    const reserva = eventoSelecionado ? reservaDoSlot(eventoSelecionado) : null;
    if (!reserva) return;
    if (!confirm("Cancelar esta reserva?")) return;
    setReservando(true);
    try {
      await api.delete(`/reservas-coletivas/${reserva.id}`);
      toast.success("Reserva cancelada");
      setDialogAberto(false);
      setEventoSelecionado(null);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setReservando(false);
    }
  };

  const evDialog = eventoSelecionado;
  const jaReservouDialog = evDialog
    ? reservasPorSlot.has(chaveReserva(evDialog.id, evDialog.data))
    : false;
  const vagasDialog = evDialog ? vagasRestantes(evDialog) : 0;
  const reservaAtual = evDialog ? reservaDoSlot(evDialog) : undefined;
  const filaDialog = evDialog ? filaDoSlot(evDialog) : undefined;
  const bloqueadoDialog = evDialog ? slotBloqueado(bloqueios, evDialog.id, evDialog.data) : false;
  const bloqueioInfo = evDialog ? bloqueioDoSlot(bloqueios, evDialog.id, evDialog.data) : undefined;
  const bloqueiosDia = bloqueios.filter((b) => b.data === selectedDayStr);
  const hoje = hojeSaoPaulo();
  const ehHojeSelecionado = selectedDayStr === hoje;

  return (
    <div className="space-y-4">
      {modoAluno && <ProximaAulaCard onCancelado={load} />}

      <PageHeader
        title="Agenda"
        description={
          modoAluno
            ? `Horários disponíveis · ${perfilLabel.toLowerCase()}`
            : periodoLabel
        }
        actions={
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={navegarAnterior} aria-label="Período anterior">
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button variant="outline" size="sm" onClick={navegarProximo} aria-label="Próximo período">
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        }
      />

      {bloqueiosDia.length > 0 && (
        <Card variant="filled" className="border-warning/30 bg-warning-bg/40">
          <CardContent className="py-3 text-sm flex items-start gap-2">
            <CloudRain className="h-5 w-5 text-warning shrink-0 mt-0.5" />
            <div>
              <p className="font-medium text-foreground">Agenda com bloqueio neste dia</p>
              <ul className="mt-1 text-muted space-y-0.5">
                {bloqueiosDia.map((b) => (
                  <li key={b.id}>
                    {b.horarioTitulo ? `${b.horarioTitulo}: ` : "Dia inteiro: "}
                    {b.motivo}
                  </li>
                ))}
              </ul>
            </div>
          </CardContent>
        </Card>
      )}

      {modoAluno && isWellhub && (
        <Card variant="filled" className="border-primary/20 bg-primary-container/40">
          <CardContent className="py-3 text-sm text-on-primary-container">
            Plano Wellhub: até 8 aulas por mês. Reservas pelo app Wellhub.
          </CardContent>
        </Card>
      )}

      <Tabs value={visao} onValueChange={(v) => setVisao(v as VisaoAgenda)}>
        {!modoAluno && (
          <TabsList>
            <TabsTrigger value="dia">Dia</TabsTrigger>
            <TabsTrigger value="semana">Semana</TabsTrigger>
          </TabsList>
        )}

        <TabsContent value="dia">
          {loading ? (
            <Loading />
          ) : (
            <div className="space-y-3">
              <p
                className={cn(
                  "text-sm font-medium capitalize",
                  ehHojeSelecionado ? "text-primary" : "text-foreground"
                )}
              >
                {format(selectedDay, "EEEE, dd 'de' MMMM", { locale: ptBR })}
                {ehHojeSelecionado && (
                  <span className="ml-2 text-xs font-semibold uppercase tracking-wide text-primary/80">
                    Hoje
                  </span>
                )}
              </p>
              {eventosDia.length === 0 ? (
                <EmptyState
                  icon={CalendarDays}
                  title="Nenhuma aula neste dia"
                  description="Escolha outro dia para ver horários disponíveis."
                />
              ) : (
                eventosDia.map((ev) => (
                  <EventoItem
                    key={`${ev.tipo}-${ev.id}-${ev.horarioInicio}`}
                    ev={ev}
                    modoAluno={modoAluno}
                    jaReservou={reservasPorSlot.has(chaveReserva(ev.id, ev.data))}
                    naFila={filaDoSlot(ev)}
                    bloqueado={slotBloqueado(bloqueios, ev.id, ev.data)}
                    podeReservar={podeReservar}
                    onAulaClick={aoClicarColetiva}
                  />
                ))
              )}
            </div>
          )}
        </TabsContent>

        {!modoAluno && (
          <TabsContent value="semana">
            {loading ? (
              <Loading />
            ) : (
              <div className="grid gap-4 md:grid-cols-7">
                {days.map((day) => {
                  const dateStr = format(day, "yyyy-MM-dd");
                  const eventos = eventosDoDia(agenda?.eventos, dateStr, modoAluno);
                  const ehHoje = dateStr === hoje;
                  return (
                    <Card
                      key={dateStr}
                      variant={ehHoje ? "filled" : "outlined"}
                      className={cn(
                        "min-h-[200px]",
                        ehHoje && "border-primary/35 bg-primary-container/20 shadow-sm"
                      )}
                    >
                      <CardHeader className="p-3 pb-1">
                        <CardTitle
                          className={cn(
                            "text-sm capitalize",
                            ehHoje && "text-primary font-semibold"
                          )}
                        >
                          {format(day, "EEE dd/MM", { locale: ptBR })}
                          {ehHoje && (
                            <span className="ml-1 text-[10px] font-medium uppercase tracking-wide text-primary/70">
                              Hoje
                            </span>
                          )}
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="p-2 space-y-1">
                        {eventos.length === 0 ? (
                          <p className="text-xs text-muted p-2">—</p>
                        ) : (
                          eventos.map((ev) => (
                            <EventoItem
                              key={`${ev.tipo}-${ev.id}-${ev.horarioInicio}`}
                              ev={ev}
                              compact
                              modoAluno={modoAluno}
                              jaReservou={reservasPorSlot.has(chaveReserva(ev.id, ev.data))}
                              onAulaClick={aoClicarColetiva}
                            />
                          ))
                        )}
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            )}
          </TabsContent>
        )}
      </Tabs>

      {modoAluno && evDialog && (
        <Dialog open={dialogAberto} onOpenChange={setDialogAberto}>
          <DialogContent title={jaReservouDialog ? "Gerenciar inscrição" : "Aula coletiva"}>
            <div className="space-y-4">
              <div>
                <p className="font-semibold text-foreground">{evDialog.titulo}</p>
                <p className="text-sm text-muted mt-1 capitalize">
                  {format(new Date(evDialog.data + "T12:00:00"), "EEEE, dd 'de' MMMM", { locale: ptBR })}
                </p>
                <p className="text-sm text-muted">
                  {formatHorario(evDialog.horarioInicio, evDialog.horarioFim)}
                </p>
              </div>

              <Card variant="filled">
                <CardContent className="pt-4 space-y-2">
                  <p className="text-sm font-medium text-foreground">Disponibilidade</p>
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-2xl font-bold text-foreground">
                      {evDialog.inscritos}/{evDialog.capacidade}
                    </span>
                    <span className="text-sm text-muted">inscritos</span>
                    <DisponibilidadeChip ev={evDialog} jaReservou={jaReservouDialog} />
                  </div>
                  {jaReservouDialog && reservaAtual && (
                    <p className="text-sm text-primary">
                      Você já está inscrito
                      {reservaAtual.origem === "WELLHUB" ? " (via Wellhub)" : ""}.
                    </p>
                  )}
                  {bloqueadoDialog && bloqueioInfo && (
                    <p className="text-sm text-error">Horário bloqueado: {bloqueioInfo.motivo}</p>
                  )}
                  {vagasDialog === 0 && !jaReservouDialog && !filaDialog && !bloqueadoDialog && (
                    <p className="text-sm text-warning">Este horário está lotado.</p>
                  )}
                  {filaDialog && (
                    <p className="text-sm text-warning">Você está na fila, posição #{filaDialog.posicao}.</p>
                  )}
                </CardContent>
              </Card>

              <div className="flex flex-col gap-2">
                {podeReservar && !jaReservouDialog && !bloqueadoDialog && vagasDialog > 0 && (
                  <Button className="w-full" onClick={confirmarReserva} disabled={reservando}>
                    {reservando ? "Reservando..." : "Confirmar reserva"}
                  </Button>
                )}
                {podeReservar && !jaReservouDialog && !filaDialog && !bloqueadoDialog && vagasDialog === 0 && (
                  <Button className="w-full" variant="tonal" onClick={entrarListaEspera} disabled={reservando}>
                    {reservando ? "Entrando..." : "Entrar na lista de espera"}
                  </Button>
                )}
                {jaReservouDialog && reservaAtual && (
                  <Button variant="destructive" className="w-full" onClick={cancelarReserva} disabled={reservando}>
                    {reservando ? "Cancelando..." : "Cancelar inscrição"}
                  </Button>
                )}
                {isWellhub && !jaReservouDialog && (
                  <p className="text-sm text-muted text-center">
                    Reservas Wellhub são feitas no aplicativo Wellhub.
                  </p>
                )}
                <Button variant="outline" className="w-full" onClick={() => setDialogAberto(false)}>
                  Fechar
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
