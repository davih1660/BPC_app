"use client";

import { useCallback, useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { BlocoHorarioOperacao, Dashboard } from "@/lib/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/layout/page-header";
import { StatTile } from "@/components/layout/stat-tile";
import { ListaInscritos } from "@/components/domain/lista-inscritos";
import { statusEmbarcacaoVariant, statusSlotVariant, statusSlotLabel, formatHorario } from "@/lib/labels";
import { Ship, AlertTriangle, Calendar, Clock, Users } from "lucide-react";
import { toast } from "sonner";
import { usePermissoes } from "@/hooks/use-permissoes";

function BlocoHorarios({ titulo, bloco }: { titulo: string; bloco: BlocoHorarioOperacao }) {
  return (
    <div className="space-y-2">
      <div className="flex flex-wrap items-center gap-2">
        <p className="text-sm font-semibold text-foreground">{titulo}</p>
        <span className="text-sm text-muted">
          {formatHorario(bloco.horarioInicio, bloco.horarioFim)}
        </span>
      </div>
      <ul className="space-y-2">
        {bloco.horarios.map((item) => (
          <li key={item.horario.id} className="flex flex-wrap items-center gap-2 text-sm">
            <span className="font-medium">{item.horario.titulo}</span>
            <Badge variant="neutral">
              {item.totalInscritos}/{item.capacidade} inscritos
            </Badge>
            {item.lotada && <Badge variant="warning">Lotada</Badge>}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function DashboardPage() {
  const { funcionalidades, perfilLabel } = usePermissoes();
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
      await api.patch(`/reservas-coletivas/${reservaId}/presenca`, { presente });
      load();
      toast.success("Presença atualizada");
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  if (loading && !data) return <Loading />;
  if (!data) return <EmptyState title="Sem dados" description="Não foi possível carregar o dashboard." />;

  const destaque = data.destaque;
  const proximas = data.proximasAulas;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Dashboard"
        description={`Visão ${perfilLabel.toLowerCase()} · atualiza a cada 1 min`}
        actions={
          <p className="text-xs text-muted flex items-center gap-1">
            <Users className="h-3 w-3" /> {data.alunosNoDia} alunos hoje
          </p>
        }
      />

      <div className="grid gap-4 sm:grid-cols-3">
        <StatTile label="Embarcações disponíveis" value={data.embarcacoesDisponiveis.length} icon={Ship} tone="success" />
        <StatTile label="Ocorrências abertas" value={data.ocorrenciasAbertas.length} icon={AlertTriangle} tone="error" />
        <StatTile label="Horários lotados hoje" value={data.horariosLotados.length} icon={Calendar} tone="warning" />
      </div>

      <Card variant="elevated">
        <CardHeader className="pb-2">
          <div className="flex flex-wrap items-center gap-2">
            <Clock className="h-5 w-5 text-primary" />
            <CardTitle className="text-xl">
              {destaque ? destaque.horario.titulo : "Sem horário em andamento hoje"}
            </CardTitle>
            {destaque && (
              <Badge variant={statusSlotVariant[destaque.statusSlot]}>
                {statusSlotLabel[destaque.statusSlot]}
              </Badge>
            )}
            {destaque?.lotada && <Badge variant="warning">Lotada</Badge>}
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {destaque ? (
            <>
              <div className="flex flex-wrap gap-4 text-sm text-muted">
                <span className="font-medium text-foreground">
                  {formatHorario(destaque.horario.horarioInicio, destaque.horario.horarioFim)}
                </span>
                <span className="font-semibold text-foreground">
                  {destaque.totalInscritos}/{destaque.capacidade} inscritos
                </span>
              </div>
              <ListaInscritos
                inscritos={destaque.inscritos}
                onPresenca={funcionalidades.checkInPresenca ? togglePresenca : undefined}
              />
            </>
          ) : (
            <EmptyState title="Expediente encerrado" description="Não há horários em andamento ou próximos hoje." className="py-8" />
          )}
        </CardContent>
      </Card>

      <Card variant="outlined">
        <CardHeader className="pb-2">
          <CardTitle className="text-lg">Próximos horários</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {!proximas.imediato && !proximas.seguinte ? (
            <p className="text-sm text-muted">Não há mais horários programados para hoje.</p>
          ) : (
            <>
              {proximas.imediato && (
                <BlocoHorarios
                  titulo={proximas.imediato.statusBloco === "EM_ANDAMENTO" ? "Agora" : "Próximo imediato"}
                  bloco={proximas.imediato}
                />
              )}
              {proximas.imediato && proximas.seguinte && <div className="border-t border-outline pt-4" />}
              {proximas.seguinte && <BlocoHorarios titulo="Em seguida" bloco={proximas.seguinte} />}
            </>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card variant="outlined">
          <CardHeader><CardTitle>Embarcações disponíveis</CardTitle></CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            {data.embarcacoesDisponiveis.map((e) => (
              <Badge key={e.id} variant={statusEmbarcacaoVariant.DISPONIVEL}>{e.nome}</Badge>
            ))}
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardHeader><CardTitle>Ocorrências abertas</CardTitle></CardHeader>
          <CardContent>
            {data.ocorrenciasAbertas.length === 0 ? (
              <p className="text-sm text-muted">Nenhuma ocorrência aberta.</p>
            ) : (
              <ul className="space-y-2">
                {data.ocorrenciasAbertas.map((o) => (
                  <li key={o.id} className="text-sm">
                    <span className="font-medium">{o.titulo}</span>
                    <span className="text-muted"> — {o.embarcacaoNome}</span>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
