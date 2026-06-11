"use client";

import { Suspense, useCallback, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import type { ReservaEmbarcacao, HorarioColetivo, Embarcacao, Usuario, OperacaoDia } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/layout/page-header";
import { SlotAulaCard, isSlotDestaque } from "@/components/domain/slot-aula-card";
import { Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { hojeSaoPaulo } from "@/lib/relogio";
import { usePermissoes } from "@/hooks/use-permissoes";

function ReservasContent() {
  const { funcionalidades, perfilLabel } = usePermissoes();
  const searchParams = useSearchParams();
  const dataParam = searchParams.get("data");
  const horarioIdParam = searchParams.get("horarioId") ?? searchParams.get("aulaId");
  const horarioIdNavegacao = horarioIdParam ? Number(horarioIdParam) : null;

  const [operacaoDia, setOperacaoDia] = useState<OperacaoDia | null>(null);
  const [dataRef, setDataRef] = useState(dataParam ?? hojeSaoPaulo());
  const [expandidos, setExpandidos] = useState<Set<number>>(new Set());
  const [reservasEmb, setReservasEmb] = useState<ReservaEmbarcacao[]>([]);
  const [horarios, setHorarios] = useState<HorarioColetivo[]>([]);
  const [embarcacoes, setEmbarcacoes] = useState<Embarcacao[]>([]);
  const [alunos, setAlunos] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingHorarios, setLoadingHorarios] = useState(true);
  const [dialogColetiva, setDialogColetiva] = useState(false);
  const [dialogEmb, setDialogEmb] = useState(false);
  const [formColetiva, setFormColetiva] = useState({ horarioId: "", alunoId: "", dataReserva: hojeSaoPaulo() });
  const [formEmb, setFormEmb] = useState({
    embarcacaoId: "", alunoId: "", data: hojeSaoPaulo(),
    horarioInicio: "10:00", horarioFim: "11:00",
  });

  useEffect(() => {
    if (dataParam) setDataRef(dataParam);
  }, [dataParam]);

  const loadHorarios = useCallback(() => {
    setLoadingHorarios(true);
    api
      .get<OperacaoDia>(`/operacao/dia?data=${dataRef}`)
      .then((d) => {
        setOperacaoDia(d);
        const auto = new Set<number>();
        if (horarioIdNavegacao && d.slotsDoDia.some((s) => s.horario.id === horarioIdNavegacao)) {
          auto.add(horarioIdNavegacao);
        } else {
          d.slotsDoDia.forEach((s) => {
            if (isSlotDestaque(s.statusSlot)) auto.add(s.horario.id);
          });
        }
        setExpandidos(auto);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoadingHorarios(false));
  }, [dataRef, horarioIdNavegacao]);

  useEffect(() => {
    if (!horarioIdNavegacao || loadingHorarios || !operacaoDia) return;
    const timer = setTimeout(() => {
      document.getElementById(`horario-slot-${horarioIdNavegacao}`)?.scrollIntoView({
        behavior: "smooth",
        block: "center",
      });
    }, 100);
    return () => clearTimeout(timer);
  }, [horarioIdNavegacao, loadingHorarios, operacaoDia]);

  const load = () => {
    setLoading(true);
    const reqs: Promise<unknown>[] = [
      api.get<HorarioColetivo[]>("/horarios-coletivos"),
      api.get<{ content: Usuario[] }>("/usuarios?tipo=ALUNO&size=100"),
    ];
    if (funcionalidades.reservarEmbarcacaoStaff) {
      reqs.unshift(api.get<ReservaEmbarcacao[]>(`/reservas-embarcacao?status=CONFIRMADA&data=${dataRef}`));
      reqs.push(api.get<{ content: Embarcacao[] }>("/embarcacoes?size=100"));
    }
    Promise.all(reqs)
      .then((results) => {
        let i = 0;
        if (funcionalidades.reservarEmbarcacaoStaff) {
          setReservasEmb(results[i++] as ReservaEmbarcacao[]);
        }
        setHorarios(results[i++] as HorarioColetivo[]);
        setAlunos((results[i++] as { content: Usuario[] }).content);
        if (funcionalidades.reservarEmbarcacaoStaff) {
          setEmbarcacoes((results[i] as { content: Embarcacao[] }).content);
        }
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [dataRef, funcionalidades.reservarEmbarcacaoStaff]);
  useEffect(() => { loadHorarios(); }, [loadHorarios]);

  const criarReservaColetiva = async () => {
    try {
      await api.post("/reservas-coletivas", {
        horarioId: Number(formColetiva.horarioId),
        alunoId: Number(formColetiva.alunoId),
        dataReserva: formColetiva.dataReserva,
        origem: "MANUAL",
      });
      toast.success("Reserva criada");
      setDialogColetiva(false);
      loadHorarios();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const criarReservaEmb = async () => {
    try {
      await api.post("/reservas-embarcacao", {
        embarcacaoId: Number(formEmb.embarcacaoId),
        alunoId: Number(formEmb.alunoId),
        data: formEmb.data,
        horarioInicio: formEmb.horarioInicio,
        horarioFim: formEmb.horarioFim,
      });
      toast.success("Reserva de embarcação criada");
      setDialogEmb(false);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const togglePresenca = async (reservaId: number, presente: boolean) => {
    try {
      await api.patch(`/reservas-coletivas/${reservaId}/presenca`, { presente });
      loadHorarios();
      toast.success("Presença atualizada");
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const cancelarColetiva = async (id: number) => {
    if (!confirm("Excluir o agendamento deste aluno?")) return;
    try {
      await api.delete(`/reservas-coletivas/${id}`);
      toast.success("Agendamento excluído");
      loadHorarios();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const toggleExpand = (horarioId: number) => {
    setExpandidos((prev) => {
      const next = new Set(prev);
      if (next.has(horarioId)) next.delete(horarioId);
      else next.add(horarioId);
      return next;
    });
  };

  const abrirNovaReserva = () => {
    setFormColetiva({ horarioId: "", alunoId: "", dataReserva: dataRef });
    setDialogColetiva(true);
  };

  const cancelarEmb = async (id: number) => {
    if (!confirm("Cancelar esta reserva de embarcação?")) return;
    try {
      await api.delete(`/reservas-embarcacao/${id}`);
      toast.success("Reserva cancelada");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  if (loading) return <Loading />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Reservas"
        description={`Visão ${perfilLabel.toLowerCase()}`}
      />

      <Tabs defaultValue="coletiva">
        <TabsList>
          <TabsTrigger value="coletiva">Coletivas</TabsTrigger>
          {funcionalidades.reservarEmbarcacaoStaff && (
            <TabsTrigger value="embarcacao">Embarcações</TabsTrigger>
          )}
        </TabsList>

        <TabsContent value="coletiva">
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4 p-3 rounded-xl border border-outline bg-surface-variant/50">
            <p className="text-sm text-muted">
              <span className="font-semibold text-foreground">{operacaoDia?.alunosNoDia ?? 0}</span> alunos agendados no dia
            </p>
            <div className="flex flex-wrap items-center gap-2">
              <Input
                type="date"
                value={dataRef}
                onChange={(e) => setDataRef(e.target.value)}
                className="w-auto"
              />
              {funcionalidades.reservarColetivaManual && (
                <Button onClick={abrirNovaReserva}>
                  <Plus className="h-4 w-4" /> Nova reserva
                </Button>
              )}
            </div>
          </div>
          {loadingHorarios && !operacaoDia ? (
            <Loading />
          ) : (
            <div className="space-y-3">
              {operacaoDia?.slotsDoDia.length === 0 && (
                <EmptyState title="Nenhum horário neste dia" description="Não há horários coletivos programados para este dia da semana." />
              )}
              {operacaoDia?.slotsDoDia.map((slot) => (
                <SlotAulaCard
                  key={slot.horario.id}
                  slot={slot}
                  expandido={expandidos.has(slot.horario.id)}
                  onToggle={() => toggleExpand(slot.horario.id)}
                  onPresenca={funcionalidades.checkInPresenca ? togglePresenca : undefined}
                  onCancelar={funcionalidades.cancelarReservaColetiva ? cancelarColetiva : undefined}
                  destaque={isSlotDestaque(slot.statusSlot)}
                  realce={horarioIdNavegacao === slot.horario.id}
                />
              ))}
            </div>
          )}
        </TabsContent>

        {funcionalidades.reservarEmbarcacaoStaff && <TabsContent value="embarcacao">
          <div className="flex justify-end mb-3">
            <Button onClick={() => setDialogEmb(true)}><Plus className="h-4 w-4" /> Nova reserva</Button>
          </div>
          <div className="space-y-2">
            {reservasEmb.map((r) => (
              <Card key={r.id}>
                <CardContent className="pt-4 flex justify-between items-center">
                  <div>
                    <p className="font-medium">{r.embarcacaoNome}</p>
                    <p className="text-sm text-slate-500">
                      {r.alunoNome} · {r.data} {r.horarioInicio?.slice(0, 5)}–{r.horarioFim?.slice(0, 5)}
                    </p>
                  </div>
                  <Button variant="destructive" size="sm" onClick={() => cancelarEmb(r.id)}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>}
      </Tabs>

      {funcionalidades.reservarColetivaManual && <Dialog open={dialogColetiva} onOpenChange={setDialogColetiva}>
        <DialogContent title="Reservar horário coletivo">
          <div className="space-y-3">
            <Select value={formColetiva.horarioId} onChange={(e) => setFormColetiva({ ...formColetiva, horarioId: e.target.value })}>
              <option value="">Horário...</option>
              {horarios.map((h) => <option key={h.id} value={h.id}>{h.titulo}</option>)}
            </Select>
            <Select value={formColetiva.alunoId} onChange={(e) => setFormColetiva({ ...formColetiva, alunoId: e.target.value })}>
              <option value="">Aluno...</option>
              {alunos.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
            </Select>
            <Input type="date" value={formColetiva.dataReserva} onChange={(e) => setFormColetiva({ ...formColetiva, dataReserva: e.target.value })} />
            <Button className="w-full" onClick={criarReservaColetiva}>Confirmar</Button>
          </div>
        </DialogContent>
      </Dialog>}

      {funcionalidades.reservarEmbarcacaoStaff && <Dialog open={dialogEmb} onOpenChange={setDialogEmb}>
        <DialogContent title="Reservar embarcação">
          <div className="space-y-3">
            <Select value={formEmb.embarcacaoId} onChange={(e) => setFormEmb({ ...formEmb, embarcacaoId: e.target.value })}>
              <option value="">Embarcação...</option>
              {embarcacoes.map((e) => <option key={e.id} value={e.id}>{e.nome} ({e.tipo})</option>)}
            </Select>
            <Select value={formEmb.alunoId} onChange={(e) => setFormEmb({ ...formEmb, alunoId: e.target.value })}>
              <option value="">Aluno...</option>
              {alunos.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
            </Select>
            <Input type="date" value={formEmb.data} onChange={(e) => setFormEmb({ ...formEmb, data: e.target.value })} />
            <div className="grid grid-cols-2 gap-2">
              <Input type="time" value={formEmb.horarioInicio} onChange={(e) => setFormEmb({ ...formEmb, horarioInicio: e.target.value })} />
              <Input type="time" value={formEmb.horarioFim} onChange={(e) => setFormEmb({ ...formEmb, horarioFim: e.target.value })} />
            </div>
            <Button className="w-full" onClick={criarReservaEmb}>Confirmar</Button>
          </div>
        </DialogContent>
      </Dialog>}
    </div>
  );
}

export default function ReservasPage() {
  return (
    <Suspense fallback={<Loading />}>
      <ReservasContent />
    </Suspense>
  );
}
