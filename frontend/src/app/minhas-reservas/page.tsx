"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type {
  HorarioColetivo,
  ReservaColetiva,
  ReservaEmbarcacao,
  ListaEspera,
  SolicitacaoUsoLivre,
  TipoCanoaUsoLivre,
} from "@/lib/types";
import { ProximaAulaCard } from "@/components/domain/proxima-aula-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/layout/page-header";
import { useAuth } from "@/contexts/auth-context";
import { usePermissoes } from "@/hooks/use-permissoes";
import {
  diaSemanaLabel,
  formatHorario,
  situacaoAlunoLabel,
  statusSolicitacaoUsoLivreLabel,
  statusSolicitacaoUsoLivreVariant,
  tipoCanoaUsoLivreLabel,
} from "@/lib/labels";
import { compararCronologicamente, hojeSaoPaulo, reservaAindaVigente } from "@/lib/relogio";
import { toast } from "sonner";
import { Calendar, ListOrdered, Plus, Ship, Trash2 } from "lucide-react";

export default function MinhasReservasPage() {
  const { usuario } = useAuth();
  const { funcionalidades, perfilLabel } = usePermissoes();
  const alunoId = usuario?.id;

  const [coletivas, setColetivas] = useState<ReservaColetiva[]>([]);
  const [embarcacoesRes, setEmbarcacoesRes] = useState<ReservaEmbarcacao[]>([]);
  const [horarios, setHorarios] = useState<HorarioColetivo[]>([]);
  const [listaEspera, setListaEspera] = useState<ListaEspera[]>([]);
  const [solicitacoesUsoLivre, setSolicitacoesUsoLivre] = useState<SolicitacaoUsoLivre[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogColetiva, setDialogColetiva] = useState(false);
  const [dialogUsoLivre, setDialogUsoLivre] = useState(false);
  const [formColetiva, setFormColetiva] = useState({ horarioId: "", dataReserva: hojeSaoPaulo() });
  const [formUsoLivre, setFormUsoLivre] = useState({
    horarioId: "",
    data: hojeSaoPaulo(),
    tipoCanoaDesejada: "OC1" as TipoCanoaUsoLivre,
    observacao: "",
  });

  const isWellhub = coletivas.some((r) => r.situacaoAluno === "WELLHUB");
  const podeReservar = funcionalidades.reservarColetivaApp;
  const podeSolicitarUsoLivre = funcionalidades.reservarEmbarcacaoAluno && !isWellhub;
  const solicitacoesPendentes = solicitacoesUsoLivre.filter((s) => s.status === "PENDENTE");
  const situacao = coletivas.find((r) => r.situacaoAluno)?.situacaoAluno;

  const horariosPorId = useMemo(
    () => new Map(horarios.map((h) => [h.id, h])),
    [horarios]
  );

  const coletivasProximas = useMemo(() => {
    if (!alunoId) return [];
    return coletivas
      .filter((r) => r.alunoId === alunoId && r.status === "CONFIRMADA")
      .filter((r) => {
        const horario = horariosPorId.get(r.horarioId);
        if (!horario) return reservaAindaVigente(r.dataReserva, "23:59");
        return reservaAindaVigente(r.dataReserva, horario.horarioFim);
      })
      .sort((a, b) => {
        const ha = horariosPorId.get(a.horarioId);
        const hb = horariosPorId.get(b.horarioId);
        return compararCronologicamente(
          { data: a.dataReserva, horarioInicio: ha?.horarioInicio ?? "00:00" },
          { data: b.dataReserva, horarioInicio: hb?.horarioInicio ?? "00:00" }
        );
      });
  }, [coletivas, alunoId, horariosPorId]);

  const embarcacoesProximas = useMemo(() => {
    if (!alunoId) return [];
    return embarcacoesRes
      .filter((r) => r.alunoId === alunoId && r.status === "CONFIRMADA")
      .filter((r) => reservaAindaVigente(r.data, r.horarioFim))
      .sort((a, b) =>
        compararCronologicamente(
          { data: a.data, horarioInicio: a.horarioInicio },
          { data: b.data, horarioInicio: b.horarioInicio }
        )
      );
  }, [embarcacoesRes, alunoId]);

  const load = useCallback(() => {
    if (!alunoId) return;
    setLoading(true);
    Promise.all([
      api.get<ReservaColetiva[]>(`/reservas-coletivas?alunoId=${alunoId}`),
      api.get<ReservaEmbarcacao[]>(`/reservas-embarcacao?alunoId=${alunoId}&status=CONFIRMADA`),
      api.get<HorarioColetivo[]>("/horarios-coletivos"),
      api.get<ListaEspera[]>(`/lista-espera?alunoId=${alunoId}`),
      api.get<SolicitacaoUsoLivre[]>(`/solicitacoes-uso-livre?alunoId=${alunoId}`),
    ])
      .then(([c, e, h, fila, solicitacoes]) => {
        setColetivas(c);
        setEmbarcacoesRes(e);
        setHorarios(h);
        setListaEspera(fila.filter((f) => f.status === "AGUARDANDO"));
        setSolicitacoesUsoLivre(solicitacoes);
      })
      .catch((err: ApiError) => toast.error(err.message))
      .finally(() => setLoading(false));
  }, [alunoId]);

  useEffect(() => { load(); }, [load]);

  const reservarColetiva = async () => {
    if (!alunoId) return;
    try {
      await api.post("/reservas-coletivas", {
        horarioId: Number(formColetiva.horarioId),
        alunoId,
        dataReserva: formColetiva.dataReserva,
        origem: "APP",
      });
      toast.success("Horário reservado");
      setDialogColetiva(false);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const solicitarUsoLivre = async () => {
    if (!alunoId) return;
    try {
      await api.post("/solicitacoes-uso-livre", {
        alunoId,
        horarioId: Number(formUsoLivre.horarioId),
        data: formUsoLivre.data,
        tipoCanoaDesejada: formUsoLivre.tipoCanoaDesejada,
        observacao: formUsoLivre.observacao || undefined,
      });
      toast.success("Solicitação enviada! Aguarde aprovação da equipe.");
      setDialogUsoLivre(false);
      setFormUsoLivre((f) => ({ ...f, observacao: "" }));
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const cancelarSolicitacao = async (id: number) => {
    if (!alunoId || !confirm("Cancelar esta solicitação?")) return;
    try {
      await api.delete(`/solicitacoes-uso-livre/${id}?alunoId=${alunoId}`);
      toast.success("Solicitação cancelada");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const cancelarColetiva = async (id: number) => {
    if (!confirm("Cancelar esta reserva?")) return;
    try {
      await api.delete(`/reservas-coletivas/${id}`);
      toast.success("Reserva cancelada");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const sairListaEspera = async (id: number) => {
    if (!alunoId || !confirm("Sair da lista de espera?")) return;
    try {
      await api.delete(`/lista-espera/${id}?alunoId=${alunoId}`);
      toast.success("Você saiu da lista de espera");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const cancelarEmb = async (id: number) => {
    if (!confirm("Cancelar reserva de embarcação?")) return;
    try {
      await api.delete(`/reservas-embarcacao/${id}`);
      toast.success("Reserva cancelada");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  if (loading) return <Loading />;

  const horariosDoDia = (data: string) => {
    const d = new Date(data + "T12:00:00");
    const dias = ["DOMINGO", "SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO"];
    return horarios.filter((h) => h.diaSemana === dias[d.getDay()]);
  };

  const horariosFiltrados = horariosDoDia(formColetiva.dataReserva);
  const horariosUsoLivre = horariosDoDia(formUsoLivre.data);

  return (
    <div className="space-y-4">
      <PageHeader
        title="Minhas reservas"
        description={`Olá, ${usuario?.nome} · ${perfilLabel}${situacao ? ` · ${situacaoAlunoLabel[situacao]}` : ""}`}
      />

      <ProximaAulaCard onCancelado={load} />

      {isWellhub && (
        <Card variant="filled" className="border-primary/20 bg-primary-container/40">
          <CardContent className="py-3 text-sm text-on-primary-container">
            Plano Wellhub: até 8 aulas por mês. Reservas novas pelo app Wellhub.
          </CardContent>
        </Card>
      )}

      <Tabs defaultValue="coletiva">
        <TabsList className="w-full">
          <TabsTrigger value="coletiva" className="flex-1">Aulas coletivas</TabsTrigger>
          <TabsTrigger value="fila" className="flex-1">
            Lista de espera{listaEspera.length > 0 ? ` (${listaEspera.length})` : ""}
          </TabsTrigger>
          {podeSolicitarUsoLivre && (
            <TabsTrigger value="uso-livre" className="flex-1">
              Uso livre{solicitacoesPendentes.length > 0 ? ` (${solicitacoesPendentes.length})` : ""}
            </TabsTrigger>
          )}
        </TabsList>

        <TabsContent value="coletiva" className="space-y-3 mt-4">
          {podeReservar && !isWellhub && (
            <Button className="w-full sm:w-auto" onClick={() => setDialogColetiva(true)}>
              <Plus className="h-4 w-4" /> Reservar horário
            </Button>
          )}
          {coletivasProximas.length === 0 ? (
            <EmptyState
              icon={Calendar}
              title="Nenhuma reserva agendada"
              description="Suas próximas aulas aparecerão aqui. Veja a agenda para reservar."
              action={
                <Button variant="tonal" onClick={() => window.location.href = "/agenda"}>
                  Ver agenda
                </Button>
              }
            />
          ) : (
            coletivasProximas.map((r) => {
              const horario = horariosPorId.get(r.horarioId);
              return (
              <Card key={r.id} variant="student">
                <CardContent className="p-4">
                  <div className="flex gap-4 items-center">
                    <div className="shrink-0 text-center min-w-[52px]">
                      <p className="text-lg font-bold text-primary leading-none">
                        {horario?.horarioInicio.slice(0, 5) ?? "--:--"}
                      </p>
                      <p className="text-xs text-muted mt-0.5">
                        {r.dataReserva.slice(8, 10)}/{r.dataReserva.slice(5, 7)}
                      </p>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-foreground">{r.horarioTitulo}</p>
                      <p className="text-sm text-muted">
                        {r.dataReserva}
                        {horario && ` · ${formatHorario(horario.horarioInicio, horario.horarioFim)}`}
                      </p>
                      {r.origem === "WELLHUB" && <Chip variant="default" className="mt-1">Wellhub</Chip>}
                    </div>
                    {funcionalidades.cancelarReservaColetiva && (
                      <Button variant="outline" size="sm" onClick={() => cancelarColetiva(r.id)} aria-label="Cancelar reserva">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </CardContent>
              </Card>
            );
            })
          )}
        </TabsContent>

        <TabsContent value="fila" className="space-y-3 mt-4">
          {listaEspera.length === 0 ? (
            <EmptyState
              icon={ListOrdered}
              title="Você não está em nenhuma fila"
              description="Quando um horário estiver lotado, você pode entrar na lista de espera pela agenda."
              action={
                <Button variant="tonal" onClick={() => (window.location.href = "/agenda")}>
                  Ver agenda
                </Button>
              }
            />
          ) : (
            listaEspera.map((f) => (
              <Card key={f.id} variant="student">
                <CardContent className="p-4 flex justify-between items-center gap-3">
                  <div>
                    <p className="font-semibold text-foreground">{f.horarioTitulo}</p>
                    <p className="text-sm text-muted">
                      {f.dataReserva} · {formatHorario(f.horarioInicio, f.horarioFim)}
                    </p>
                    <Chip variant="warning" className="mt-2">Posição #{f.posicao}</Chip>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => sairListaEspera(f.id)}>
                    Sair da fila
                  </Button>
                </CardContent>
              </Card>
            ))
          )}
        </TabsContent>

        {podeSolicitarUsoLivre && (
          <TabsContent value="uso-livre" className="space-y-3 mt-4">
            <Card variant="filled" className="border-primary/20 bg-primary-container/30">
              <CardContent className="py-3 text-sm text-muted">
                Uso de canoa sem professor: escolha um horário fixo da grade e aguarde aprovação da equipe.
              </CardContent>
            </Card>
            <Button className="w-full sm:w-auto" onClick={() => setDialogUsoLivre(true)}>
              <Plus className="h-4 w-4" /> Solicitar uso livre
            </Button>

            {solicitacoesUsoLivre.length === 0 && embarcacoesProximas.length === 0 ? (
              <EmptyState
                icon={Ship}
                title="Nenhuma solicitação"
                description="Solicite uso de OC1 a OC6 em um horário da grade. A equipe aprova e atribui a canoa."
              />
            ) : (
              <>
                {solicitacoesUsoLivre.map((s) => (
                  <Card key={s.id} variant="student">
                    <CardContent className="p-4 flex justify-between items-start gap-3">
                      <div>
                        <p className="font-semibold text-foreground">
                          {tipoCanoaUsoLivreLabel[s.tipoCanoaDesejada]}
                        </p>
                        <p className="text-sm text-muted">
                          {s.data} · {formatHorario(s.horarioInicio, s.horarioFim)}
                        </p>
                        {s.embarcacaoNome && (
                          <p className="text-sm text-primary mt-1">{s.embarcacaoNome}</p>
                        )}
                        {s.motivoRecusa && (
                          <p className="text-sm text-error mt-1">{s.motivoRecusa}</p>
                        )}
                        <Chip variant={statusSolicitacaoUsoLivreVariant[s.status]} className="mt-2">
                          {statusSolicitacaoUsoLivreLabel[s.status]}
                        </Chip>
                      </div>
                      {s.status === "PENDENTE" && (
                        <Button variant="outline" size="sm" onClick={() => cancelarSolicitacao(s.id)}>
                          Cancelar
                        </Button>
                      )}
                    </CardContent>
                  </Card>
                ))}
                {embarcacoesProximas.map((r) => (
                  <Card key={r.id} variant="student">
                    <CardContent className="p-4 flex justify-between items-center">
                      <div>
                        <p className="font-semibold text-foreground">{r.embarcacaoNome}</p>
                        <p className="text-sm text-muted">
                          {r.data} · {r.horarioInicio?.slice(0, 5)}–{r.horarioFim?.slice(0, 5)}
                        </p>
                        <Chip variant="success" className="mt-2">Confirmado</Chip>
                      </div>
                      <Button variant="destructive" size="sm" onClick={() => cancelarEmb(r.id)}>
                        Cancelar
                      </Button>
                    </CardContent>
                  </Card>
                ))}
              </>
            )}
          </TabsContent>
        )}
      </Tabs>

      {podeReservar && (
        <Dialog open={dialogColetiva} onOpenChange={setDialogColetiva}>
          <DialogContent title="Reservar aula coletiva">
            <div className="space-y-3">
              <Input
                type="date"
                value={formColetiva.dataReserva}
                onChange={(e) => setFormColetiva({ ...formColetiva, dataReserva: e.target.value })}
              />
              <Select
                value={formColetiva.horarioId}
                onChange={(e) => setFormColetiva({ ...formColetiva, horarioId: e.target.value })}
              >
                <option value="">Horário...</option>
                {horariosFiltrados.map((h) => (
                  <option key={h.id} value={h.id}>
                    {diaSemanaLabel[h.diaSemana]} {formatHorario(h.horarioInicio, h.horarioFim)}
                  </option>
                ))}
              </Select>
              <Button className="w-full" onClick={reservarColetiva}>Confirmar</Button>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {podeSolicitarUsoLivre && (
        <Dialog open={dialogUsoLivre} onOpenChange={setDialogUsoLivre}>
          <DialogContent title="Solicitar uso livre">
            <div className="space-y-3">
              <p className="text-sm text-muted">
                Escolha data e horário fixo da grade. A equipe aprova e informa qual canoa ficará disponível.
              </p>
              <Input
                type="date"
                value={formUsoLivre.data}
                onChange={(e) => setFormUsoLivre({ ...formUsoLivre, data: e.target.value, horarioId: "" })}
              />
              <Select
                value={formUsoLivre.horarioId}
                onChange={(e) => setFormUsoLivre({ ...formUsoLivre, horarioId: e.target.value })}
              >
                <option value="">Horário da grade...</option>
                {horariosUsoLivre.map((h) => (
                  <option key={h.id} value={h.id}>
                    {formatHorario(h.horarioInicio, h.horarioFim)} — {h.titulo}
                  </option>
                ))}
              </Select>
              <Select
                value={formUsoLivre.tipoCanoaDesejada}
                onChange={(e) =>
                  setFormUsoLivre({
                    ...formUsoLivre,
                    tipoCanoaDesejada: e.target.value as TipoCanoaUsoLivre,
                  })
                }
              >
                {(Object.keys(tipoCanoaUsoLivreLabel) as TipoCanoaUsoLivre[]).map((t) => (
                  <option key={t} value={t}>
                    {tipoCanoaUsoLivreLabel[t]}
                  </option>
                ))}
              </Select>
              <Input
                placeholder="Observação (opcional)"
                value={formUsoLivre.observacao}
                onChange={(e) => setFormUsoLivre({ ...formUsoLivre, observacao: e.target.value })}
              />
              <Button className="w-full" onClick={solicitarUsoLivre} disabled={!formUsoLivre.horarioId}>
                Enviar solicitação
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
