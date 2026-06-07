"use client";

import { useCallback, useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { OperacaoAula, OperacaoDia, StatusSlot } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loading } from "@/components/ui/loading";
import { ListaInscritos } from "@/components/domain/lista-inscritos";
import { formatHorario, statusSlotClass, statusSlotLabel, statusSlotCardClass } from "@/lib/labels";
import { ChevronDown, ChevronUp } from "lucide-react";
import { toast } from "sonner";
import { hojeSaoPaulo } from "@/lib/relogio";

export default function AulasPage() {
  const [data, setData] = useState<OperacaoDia | null>(null);
  const [dataRef, setDataRef] = useState(hojeSaoPaulo());
  const [loading, setLoading] = useState(true);
  const [expandidos, setExpandidos] = useState<Set<number>>(new Set());

  const load = useCallback(() => {
    setLoading(true);
    api
      .get<OperacaoDia>(`/operacao/dia?data=${dataRef}`)
      .then((d) => {
        setData(d);
        const auto = new Set<number>();
        d.slotsDoDia.forEach((s) => {
          if (s.statusSlot === "EM_ANDAMENTO" || s.statusSlot === "PROXIMA") {
            auto.add(s.aula.id);
          }
        });
        setExpandidos(auto);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  }, [dataRef]);

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

  const toggleExpand = (aulaId: number) => {
    setExpandidos((prev) => {
      const next = new Set(prev);
      if (next.has(aulaId)) next.delete(aulaId);
      else next.add(aulaId);
      return next;
    });
  };

  const isDestaque = (status: StatusSlot) =>
    status === "EM_ANDAMENTO" || status === "PROXIMA";

  if (loading && !data) return <Loading />;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">Horários do dia</h1>
          <p className="text-sm text-slate-500">
            Aulas por slot · {data?.alunosNoDia ?? 0} alunos no dia (total)
          </p>
        </div>
        <Input
          type="date"
          value={dataRef}
          onChange={(e) => setDataRef(e.target.value)}
          className="w-auto"
        />
      </div>

      <div className="space-y-3">
        {data?.slotsDoDia.length === 0 && (
          <p className="text-slate-500 text-sm">Nenhuma aula neste dia da semana.</p>
        )}
        {data?.slotsDoDia.map((slot) => (
          <SlotCard
            key={slot.aula.id}
            slot={slot}
            expandido={expandidos.has(slot.aula.id)}
            onToggle={() => toggleExpand(slot.aula.id)}
            onPresenca={togglePresenca}
            destaque={isDestaque(slot.statusSlot)}
          />
        ))}
      </div>
    </div>
  );
}

function SlotCard({
  slot,
  expandido,
  onToggle,
  onPresenca,
  destaque,
}: {
  slot: OperacaoAula;
  expandido: boolean;
  onToggle: () => void;
  onPresenca: (id: number, p: boolean) => void;
  destaque: boolean;
}) {
  return (
    <Card className={`${statusSlotCardClass[slot.statusSlot]} ${destaque ? "shadow-sm" : ""}`}>
      <CardContent className="pt-4">
        <div className="flex flex-wrap items-start justify-between gap-2">
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-semibold text-slate-900">
                {formatHorario(slot.aula.horarioInicio, slot.aula.horarioFim)}
              </span>
              <Badge className={statusSlotClass[slot.statusSlot]}>
                {statusSlotLabel[slot.statusSlot]}
              </Badge>
              {slot.lotada && (
                <Badge className="bg-amber-100 text-amber-800">Lotada</Badge>
              )}
            </div>
            <p className="text-sm text-slate-700">{slot.aula.titulo}</p>
            <p className="text-xs text-slate-500">
              {slot.aula.professorNome} · {slot.aula.embarcacaoPrincipalNome} ·{" "}
              {slot.totalInscritos}/{slot.capacidade} inscritos
            </p>
          </div>
          <Button variant="ghost" size="sm" onClick={onToggle}>
            {expandido ? (
              <>Ocultar <ChevronUp className="h-4 w-4 ml-1" /></>
            ) : (
              <>Inscritos <ChevronDown className="h-4 w-4 ml-1" /></>
            )}
          </Button>
        </div>
        {expandido && (
          <div className="mt-4 pt-3 border-t border-slate-100">
            <ListaInscritos inscritos={slot.inscritos} onPresenca={onPresenca} compact />
          </div>
        )}
      </CardContent>
    </Card>
  );
}
