"use client";

import { useCallback, useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { BlocoHorarioOperacao, Dashboard } from "@/lib/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loading } from "@/components/ui/loading";
import { ListaInscritos } from "@/components/domain/lista-inscritos";
import { statusEmbarcacaoClass, statusSlotClass, statusSlotLabel, formatHorario } from "@/lib/labels";
import { Ship, AlertTriangle, Calendar, Clock, Users } from "lucide-react";
import { toast } from "sonner";

function BlocoAulas({ titulo, bloco }: { titulo: string; bloco: BlocoHorarioOperacao }) {
  return (
    <div className="space-y-2">
      <div className="flex flex-wrap items-center gap-2">
        <p className="text-sm font-semibold text-slate-800">{titulo}</p>
        <span className="text-sm text-slate-600">
          {formatHorario(bloco.horarioInicio, bloco.horarioFim)}
        </span>
      </div>
      <ul className="space-y-2">
        {bloco.aulas.map((item) => (
          <li key={item.aula.id} className="flex flex-wrap items-center gap-2 text-sm">
            <span className="font-medium">Prof. {item.aula.professorNome}</span>
            <span className="text-slate-500">{item.aula.embarcacaoPrincipalNome}</span>
            <Badge className="bg-slate-100 text-slate-700">
              {item.totalInscritos}/{item.capacidade} inscritos
            </Badge>
            {item.lotada && (
              <Badge className="bg-amber-100 text-amber-800">Lotada</Badge>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    api
      .get<Dashboard>("/dashboard")
      .then(setData)
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
    const id = setInterval(load, 60_000);
    return () => clearInterval(id);
  }, [load]);

  const togglePresenca = async (reservaId: number, presente: boolean) => {
    try {
      await api.patch(`/reservas-aula/${reservaId}/presenca`, { presente });
      load();
      toast.success("Presença atualizada");
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  if (loading && !data) return <Loading />;
  if (!data) return <p className="text-slate-500">Sem dados</p>;

  const destaque = data.destaque;
  const proximas = data.proximasAulas;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-2">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Operação — Recepção</h1>
          <p className="text-slate-500 text-sm">Controle por horário da aula · atualiza a cada 1 min</p>
        </div>
        <p className="text-xs text-slate-400 flex items-center gap-1">
          <Users className="h-3 w-3" /> {data.alunosNoDia} alunos no dia (total)
        </p>
      </div>

      <Card className={destaque ? "border-2 border-sky-200 shadow-md" : ""}>
        <CardHeader className="pb-2">
          <div className="flex flex-wrap items-center gap-2">
            <Clock className="h-5 w-5 text-sky-600" />
            <CardTitle className="text-xl">
              {destaque ? destaque.aula.titulo : "Sem aula em andamento ou próxima hoje"}
            </CardTitle>
            {destaque && (
              <Badge className={statusSlotClass[destaque.statusSlot]}>
                {statusSlotLabel[destaque.statusSlot]}
              </Badge>
            )}
            {destaque?.lotada && (
              <Badge className="bg-amber-100 text-amber-800">Lotada</Badge>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {destaque ? (
            <>
              <div className="flex flex-wrap gap-4 text-sm text-slate-600">
                <span className="font-medium text-slate-900">
                  {formatHorario(destaque.aula.horarioInicio, destaque.aula.horarioFim)}
                </span>
                <span>Prof. {destaque.aula.professorNome}</span>
                <span>{destaque.aula.embarcacaoPrincipalNome}</span>
                <span className="font-semibold">
                  {destaque.totalInscritos}/{destaque.capacidade} inscritos
                </span>
              </div>
              <div>
                <p className="text-sm font-medium text-slate-700 mb-2">Alunos agendados</p>
                <ListaInscritos
                  inscritos={destaque.inscritos}
                  onPresenca={togglePresenca}
                />
              </div>
            </>
          ) : (
            <p className="text-sm text-slate-500">Não há mais aulas programadas para hoje ou o expediente encerrou.</p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-lg">Próximas aulas</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {!proximas.imediato && !proximas.seguinte ? (
            <p className="text-sm text-slate-500">Não há mais aulas programadas para hoje.</p>
          ) : (
            <>
              {proximas.imediato && (
                <BlocoAulas
                  titulo={proximas.imediato.statusBloco === "EM_ANDAMENTO" ? "Agora" : "Próxima imediata"}
                  bloco={proximas.imediato}
                />
              )}
              {proximas.imediato && proximas.seguinte && (
                <div className="border-t border-slate-100 pt-4" />
              )}
              {proximas.seguinte && (
                <BlocoAulas titulo="Em seguida" bloco={proximas.seguinte} />
              )}
            </>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardContent className="pt-6 flex items-center gap-4">
            <div className="p-3 rounded-lg bg-emerald-100"><Ship className="h-6 w-6 text-emerald-600" /></div>
            <div>
              <p className="text-2xl font-bold">{data.embarcacoesDisponiveis.length}</p>
              <p className="text-sm text-slate-500">Embarcações disponíveis</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6 flex items-center gap-4">
            <div className="p-3 rounded-lg bg-red-100"><AlertTriangle className="h-6 w-6 text-red-600" /></div>
            <div>
              <p className="text-2xl font-bold">{data.ocorrenciasAbertas.length}</p>
              <p className="text-sm text-slate-500">Ocorrências abertas</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6 flex items-center gap-4">
            <div className="p-3 rounded-lg bg-amber-100"><Calendar className="h-6 w-6 text-amber-600" /></div>
            <div>
              <p className="text-2xl font-bold">{data.aulasLotadas.length}</p>
              <p className="text-sm text-slate-500">Horários lotados hoje</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Embarcações disponíveis</CardTitle></CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            {data.embarcacoesDisponiveis.map((e) => (
              <Badge key={e.id} className={statusEmbarcacaoClass.DISPONIVEL}>
                {e.nome}
              </Badge>
            ))}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Ocorrências abertas</CardTitle></CardHeader>
          <CardContent>
            <ul className="space-y-2">
              {data.ocorrenciasAbertas.map((o) => (
                <li key={o.id} className="text-sm">
                  <span className="font-medium">{o.titulo}</span>
                  <span className="text-slate-500"> — {o.embarcacaoNome}</span>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
