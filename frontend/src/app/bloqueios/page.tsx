"use client";

import { useCallback, useEffect, useState } from "react";
import { addDays, format } from "date-fns";
import { api, ApiError } from "@/lib/api";
import type { BloqueioAgenda, HorarioColetivo, TipoBloqueioAgenda } from "@/lib/types";
import { PageHeader } from "@/components/layout/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";
import { CloudRain, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { hojeSaoPaulo } from "@/lib/relogio";

const tipos: TipoBloqueioAgenda[] = ["FERIADO", "CHUVA", "OUTRO"];

const tipoLabel: Record<TipoBloqueioAgenda, string> = {
  FERIADO: "Feriado",
  CHUVA: "Chuva",
  OUTRO: "Outro",
};

export default function BloqueiosPage() {
  const [bloqueios, setBloqueios] = useState<BloqueioAgenda[]>([]);
  const [horarios, setHorarios] = useState<HorarioColetivo[]>([]);
  const [loading, setLoading] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [form, setForm] = useState({
    data: hojeSaoPaulo(),
    horarioId: "",
    tipo: "CHUVA" as TipoBloqueioAgenda,
    motivo: "",
  });

  const carregar = useCallback(() => {
    setLoading(true);
    const de = hojeSaoPaulo();
    const ate = format(addDays(new Date(de + "T12:00:00"), 60), "yyyy-MM-dd");
    Promise.all([
      api.get<BloqueioAgenda[]>(`/bloqueios-agenda?de=${de}&ate=${ate}`),
      api.get<HorarioColetivo[]>("/horarios-coletivos"),
    ])
      .then(([b, h]) => {
        setBloqueios(b);
        setHorarios(h);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const criar = async () => {
    if (!form.motivo.trim()) {
      toast.error("Informe o motivo do bloqueio");
      return;
    }
    setSalvando(true);
    try {
      await api.post("/bloqueios-agenda", {
        data: form.data,
        horarioId: form.horarioId ? Number(form.horarioId) : null,
        tipo: form.tipo,
        motivo: form.motivo,
        cancelarInscritos: true,
      });
      toast.success("Bloqueio criado");
      setForm((f) => ({ ...f, motivo: "" }));
      carregar();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setSalvando(false);
    }
  };

  const remover = async (id: number) => {
    if (!confirm("Remover este bloqueio?")) return;
    try {
      await api.delete(`/bloqueios-agenda/${id}`);
      toast.success("Bloqueio removido");
      carregar();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Bloqueios de agenda" description="Feriados, chuva e indisponibilidades" />

      <Card>
        <CardContent className="pt-4 space-y-3">
          <p className="text-sm font-medium">Novo bloqueio</p>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <label className="text-xs text-muted">Data</label>
              <Input
                type="date"
                value={form.data}
                onChange={(e) => setForm((f) => ({ ...f, data: e.target.value }))}
              />
            </div>
            <div>
              <label className="text-xs text-muted">Horário (vazio = dia inteiro)</label>
              <Select
                value={form.horarioId}
                onChange={(e) => setForm((f) => ({ ...f, horarioId: e.target.value }))}
              >
                <option value="">Dia inteiro</option>
                {horarios.map((h) => (
                  <option key={h.id} value={h.id}>
                    {h.titulo} ({h.horarioInicio?.slice(0, 5)})
                  </option>
                ))}
              </Select>
            </div>
            <div>
              <label className="text-xs text-muted">Tipo</label>
              <Select
                value={form.tipo}
                onChange={(e) => setForm((f) => ({ ...f, tipo: e.target.value as TipoBloqueioAgenda }))}
              >
                {tipos.map((t) => (
                  <option key={t} value={t}>
                    {tipoLabel[t]}
                  </option>
                ))}
              </Select>
            </div>
            <div>
              <label className="text-xs text-muted">Motivo</label>
              <Input
                value={form.motivo}
                onChange={(e) => setForm((f) => ({ ...f, motivo: e.target.value }))}
                placeholder="Ex.: Chuva forte"
              />
            </div>
          </div>
          <Button onClick={criar} disabled={salvando}>
            {salvando ? "Salvando..." : "Criar bloqueio"}
          </Button>
        </CardContent>
      </Card>

      {loading ? (
        <Loading />
      ) : bloqueios.length === 0 ? (
        <EmptyState icon={CloudRain} title="Nenhum bloqueio" description="Crie bloqueios para feriados ou dias de chuva." />
      ) : (
        <ul className="space-y-2">
          {bloqueios.map((b) => (
            <li key={b.id}>
              <Card>
                <CardContent className="py-3 flex items-center justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2 flex-wrap">
                      <p className="font-medium">
                        {new Date(b.data + "T12:00:00").toLocaleDateString("pt-BR")}
                        {b.horarioTitulo ? ` · ${b.horarioTitulo}` : " · Dia inteiro"}
                      </p>
                      <Badge variant="warning">{tipoLabel[b.tipo]}</Badge>
                    </div>
                    <p className="text-sm text-muted mt-1">{b.motivo}</p>
                  </div>
                  <Button variant="ghost" size="icon" onClick={() => remover(b.id)} aria-label="Remover">
                    <Trash2 className="h-4 w-4 text-error" />
                  </Button>
                </CardContent>
              </Card>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
