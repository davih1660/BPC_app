"use client";

import { useEffect, useState } from "react";
import { addDays, format, startOfWeek } from "date-fns";
import { ptBR } from "date-fns/locale";
import { api } from "@/lib/api";
import type { Agenda } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Loading } from "@/components/ui/loading";
import { statusEmbarcacaoClass } from "@/lib/labels";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { toast } from "sonner";
import { dataReferenciaSaoPaulo } from "@/lib/relogio";

function eventosDoDia(
  eventos: Agenda["eventos"] | undefined,
  dateStr: string
) {
  return (eventos?.filter((e) => e.data === dateStr) ?? []).sort((a, b) =>
    a.horarioInicio.localeCompare(b.horarioInicio) ||
    a.horarioFim.localeCompare(b.horarioFim)
  );
}

export default function AgendaPage() {
  const [weekStart, setWeekStart] = useState(() =>
    startOfWeek(dataReferenciaSaoPaulo(), { weekStartsOn: 1 })
  );
  const [agenda, setAgenda] = useState<Agenda | null>(null);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    const de = format(weekStart, "yyyy-MM-dd");
    const ate = format(addDays(weekStart, 6), "yyyy-MM-dd");
    api
      .get<Agenda>(`/aulas/agenda?de=${de}&ate=${ate}`)
      .then(setAgenda)
      .catch((e) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [weekStart]);

  const days = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Agenda</h1>
          <p className="text-sm text-slate-500">Aulas e reservas da semana</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={() => setWeekStart((d) => addDays(d, -7))}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="sm" onClick={() => setWeekStart((d) => addDays(d, 7))}>
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {loading ? (
        <Loading />
      ) : (
        <div className="grid gap-4 md:grid-cols-7">
          {days.map((day) => {
            const dateStr = format(day, "yyyy-MM-dd");
            const eventos = eventosDoDia(agenda?.eventos, dateStr);
            return (
              <Card key={dateStr} className="min-h-[200px]">
                <CardHeader className="p-3 pb-1">
                  <CardTitle className="text-sm">
                    {format(day, "EEE dd/MM", { locale: ptBR })}
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-2 space-y-1">
                  {eventos.length === 0 ? (
                    <p className="text-xs text-slate-400 p-2">—</p>
                  ) : (
                    eventos.map((ev) => (
                      <div
                        key={`${ev.tipo}-${ev.id}-${ev.horarioInicio}`}
                        className={`text-xs rounded p-2 border ${
                          ev.tipo === "AULA"
                            ? "bg-sky-50 border-sky-200"
                            : "bg-amber-50 border-amber-200"
                        }`}
                      >
                        <p className="font-medium truncate">{ev.titulo}</p>
                        <p className="text-slate-500">
                          {ev.horarioInicio?.slice(0, 5)}–{ev.horarioFim?.slice(0, 5)}
                        </p>
                        {ev.tipo !== "AULA" && ev.statusEmbarcacao && (
                          <span className={`inline-block mt-1 px-1 rounded text-[10px] ${statusEmbarcacaoClass[ev.statusEmbarcacao]}`}>
                            {ev.embarcacaoNome}
                          </span>
                        )}
                        {ev.tipo === "AULA" && (
                          <p className="text-slate-500 mt-0.5">{ev.inscritos}/{ev.capacidade}</p>
                        )}
                      </div>
                    ))
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
