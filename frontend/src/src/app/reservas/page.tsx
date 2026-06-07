"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { ReservaAula, ReservaEmbarcacao, Aula, Embarcacao, Usuario } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

export default function ReservasPage() {
  const [reservasAula, setReservasAula] = useState<ReservaAula[]>([]);
  const [reservasEmb, setReservasEmb] = useState<ReservaEmbarcacao[]>([]);
  const [aulas, setAulas] = useState<Aula[]>([]);
  const [embarcacoes, setEmbarcacoes] = useState<Embarcacao[]>([]);
  const [alunos, setAlunos] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogAula, setDialogAula] = useState(false);
  const [dialogEmb, setDialogEmb] = useState(false);
  const [formAula, setFormAula] = useState({ aulaId: "", alunoId: "", dataReserva: new Date().toISOString().slice(0, 10) });
  const [formEmb, setFormEmb] = useState({
    embarcacaoId: "", alunoId: "", data: new Date().toISOString().slice(0, 10),
    horarioInicio: "10:00", horarioFim: "11:00",
  });

  const load = () => {
    setLoading(true);
    Promise.all([
      api.get<ReservaAula[]>("/reservas-aula?status=CONFIRMADA"),
      api.get<ReservaEmbarcacao[]>("/reservas-embarcacao?status=CONFIRMADA"),
      api.get<Aula[]>("/aulas"),
      api.get<{ content: Embarcacao[] }>("/embarcacoes?size=100"),
      api.get<{ content: Usuario[] }>("/usuarios?tipo=ALUNO&size=100"),
    ])
      .then(([ra, re, au, emb, al]) => {
        setReservasAula(ra);
        setReservasEmb(re);
        setAulas(au);
        setEmbarcacoes(emb.content);
        setAlunos(al.content);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const criarReservaAula = async () => {
    try {
      await api.post("/reservas-aula", {
        aulaId: Number(formAula.aulaId),
        alunoId: Number(formAula.alunoId),
        dataReserva: formAula.dataReserva,
      });
      toast.success("Reserva de aula criada");
      setDialogAula(false);
      load();
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

  const cancelarAula = async (id: number) => {
    if (!confirm("Cancelar esta reserva de aula?")) return;
    try {
      await api.delete(`/reservas-aula/${id}`);
      toast.success("Reserva cancelada");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
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
      <h1 className="text-2xl font-bold">Reservas</h1>

      <Tabs defaultValue="aula">
        <TabsList>
          <TabsTrigger value="aula">Aulas</TabsTrigger>
          <TabsTrigger value="embarcacao">Embarcações</TabsTrigger>
        </TabsList>

        <TabsContent value="aula">
          <div className="flex justify-end mb-3">
            <Button onClick={() => setDialogAula(true)}><Plus className="h-4 w-4" /> Nova reserva</Button>
          </div>
          <div className="space-y-2">
            {reservasAula.map((r) => (
              <Card key={r.id}>
                <CardContent className="pt-4 flex justify-between items-center">
                  <div>
                    <p className="font-medium">{r.aulaTitulo}</p>
                    <p className="text-sm text-slate-500">{r.alunoNome} · {r.dataReserva}</p>
                  </div>
                  <Button variant="destructive" size="sm" onClick={() => cancelarAula(r.id)}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="embarcacao">
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
        </TabsContent>
      </Tabs>

      <Dialog open={dialogAula} onOpenChange={setDialogAula}>
        <DialogContent title="Reservar aula">
          <div className="space-y-3">
            <Select value={formAula.aulaId} onChange={(e) => setFormAula({ ...formAula, aulaId: e.target.value })}>
              <option value="">Aula...</option>
              {aulas.map((a) => <option key={a.id} value={a.id}>{a.titulo}</option>)}
            </Select>
            <Select value={formAula.alunoId} onChange={(e) => setFormAula({ ...formAula, alunoId: e.target.value })}>
              <option value="">Aluno...</option>
              {alunos.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
            </Select>
            <Input type="date" value={formAula.dataReserva} onChange={(e) => setFormAula({ ...formAula, dataReserva: e.target.value })} />
            <Button className="w-full" onClick={criarReservaAula}>Confirmar</Button>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={dialogEmb} onOpenChange={setDialogEmb}>
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
      </Dialog>
    </div>
  );
}
