"use client";

import { useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { HorarioColetivo, DiaSemana } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { diaSemanaLabel, formatHorario } from "@/lib/labels";
import { Plus, Pencil } from "lucide-react";
import { toast } from "sonner";

const DIAS: DiaSemana[] = [
  "SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO", "DOMINGO",
];

const ORDEM_DIA: Record<DiaSemana, number> = Object.fromEntries(
  DIAS.map((d, i) => [d, i])
) as Record<DiaSemana, number>;

type FormHorario = {
  titulo: string;
  diaSemana: DiaSemana;
  horarioInicio: string;
  horarioFim: string;
  capacidadeSlot: string;
};

const formVazio = (): FormHorario => ({
  titulo: "",
  diaSemana: "SEGUNDA",
  horarioInicio: "07:00",
  horarioFim: "08:00",
  capacidadeSlot: "42",
});

function toApiTime(hora: string) {
  return hora.length === 5 ? `${hora}:00` : hora;
}

function fromApiTime(hora: string) {
  return hora?.slice(0, 5) ?? "";
}

export default function HorariosColetivosPage() {
  const [horarios, setHorarios] = useState<HorarioColetivo[]>([]);
  const [filtroDia, setFiltroDia] = useState("");
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selected, setSelected] = useState<HorarioColetivo | null>(null);
  const [form, setForm] = useState<FormHorario>(formVazio());

  const load = () => {
    setLoading(true);
    const url = filtroDia ? `/horarios-coletivos?dia=${filtroDia}` : "/horarios-coletivos";
    api
      .get<HorarioColetivo[]>(url)
      .then(setHorarios)
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [filtroDia]);

  const ordenados = useMemo(
    () => [...horarios].sort((a, b) => {
      const d = ORDEM_DIA[a.diaSemana] - ORDEM_DIA[b.diaSemana];
      if (d !== 0) return d;
      return a.horarioInicio.localeCompare(b.horarioInicio);
    }),
    [horarios]
  );

  const abrirNovo = () => {
    setSelected(null);
    setForm(formVazio());
    setDialogOpen(true);
  };

  const abrirEditar = (h: HorarioColetivo) => {
    setSelected(h);
    setForm({
      titulo: h.titulo,
      diaSemana: h.diaSemana,
      horarioInicio: fromApiTime(h.horarioInicio),
      horarioFim: fromApiTime(h.horarioFim),
      capacidadeSlot: String(h.capacidadeSlot),
    });
    setDialogOpen(true);
  };

  const salvar = async () => {
    const payload = {
      titulo: form.titulo,
      diaSemana: form.diaSemana,
      horarioInicio: toApiTime(form.horarioInicio),
      horarioFim: toApiTime(form.horarioFim),
      capacidadeSlot: Number(form.capacidadeSlot),
    };
    try {
      if (selected) {
        await api.put(`/horarios-coletivos/${selected.id}`, payload);
        toast.success("Horário atualizado");
      } else {
        await api.post("/horarios-coletivos", payload);
        toast.success("Horário criado");
      }
      setDialogOpen(false);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  if (loading) return <Loading />;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Horários coletivos</h1>
          <p className="text-sm text-slate-500">
            Cadastro de slots da grade — sem professor nem embarcação fixos
          </p>
        </div>
        <div className="flex gap-2">
          <Select value={filtroDia} onChange={(e) => setFiltroDia(e.target.value)} className="w-40">
            <option value="">Todos os dias</option>
            {DIAS.map((d) => (
              <option key={d} value={d}>{diaSemanaLabel[d]}</option>
            ))}
          </Select>
          <Button onClick={abrirNovo}><Plus className="h-4 w-4" /> Novo horário</Button>
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {ordenados.map((h) => (
          <Card key={h.id}>
            <CardContent className="pt-4">
              <div className="flex justify-between items-start gap-2">
                <div>
                  <p className="font-semibold text-slate-900">{h.titulo}</p>
                  <p className="text-sm text-slate-600">{diaSemanaLabel[h.diaSemana]}</p>
                  <p className="text-sm text-slate-500">
                    {formatHorario(h.horarioInicio, h.horarioFim)}
                  </p>
                  <p className="text-xs text-slate-400 mt-1">
                    Capacidade do slot: {h.capacidadeSlot} vagas
                  </p>
                </div>
                <Button variant="ghost" size="sm" onClick={() => abrirEditar(h)}>
                  <Pencil className="h-4 w-4" />
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent title={selected ? "Editar horário" : "Novo horário coletivo"}>
          <div className="space-y-3">
            <Input
              placeholder="Título"
              value={form.titulo}
              onChange={(e) => setForm({ ...form, titulo: e.target.value })}
            />
            <Select value={form.diaSemana} onChange={(e) => setForm({ ...form, diaSemana: e.target.value as DiaSemana })}>
              {DIAS.map((d) => <option key={d} value={d}>{diaSemanaLabel[d]}</option>)}
            </Select>
            <div className="grid grid-cols-2 gap-2">
              <Input type="time" value={form.horarioInicio} onChange={(e) => setForm({ ...form, horarioInicio: e.target.value })} />
              <Input type="time" value={form.horarioFim} onChange={(e) => setForm({ ...form, horarioFim: e.target.value })} />
            </div>
            <Input
              type="number"
              placeholder="Capacidade do slot"
              value={form.capacidadeSlot}
              onChange={(e) => setForm({ ...form, capacidadeSlot: e.target.value })}
            />
            <Button className="w-full" onClick={salvar}>Salvar</Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
