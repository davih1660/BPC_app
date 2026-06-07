"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Ocorrencia, Embarcacao, StatusOcorrencia, GravidadeOcorrencia } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { statusOcorrenciaClass, gravidadeClass } from "@/lib/labels";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/auth-context";

export default function OcorrenciasPage() {
  const { usuario } = useAuth();
  const [lista, setLista] = useState<Ocorrencia[]>([]);
  const [embarcacoes, setEmbarcacoes] = useState<Embarcacao[]>([]);
  const [filtro, setFiltro] = useState<StatusOcorrencia | "">("");
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState({
    embarcacaoId: "",
    titulo: "",
    descricao: "",
    gravidade: "MEDIA" as GravidadeOcorrencia,
  });

  const load = () => {
    setLoading(true);
    const url = filtro ? `/ocorrencias?status=${filtro}` : "/ocorrencias";
    Promise.all([
      api.get<Ocorrencia[]>(url),
      api.get<{ content: Embarcacao[] }>("/embarcacoes?size=100"),
    ])
      .then(([oc, emb]) => {
        setLista(oc);
        setEmbarcacoes(emb.content);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [filtro]);

  const criar = async () => {
    if (!usuario) {
      toast.error("Selecione um usuário no header");
      return;
    }
    try {
      await api.post("/ocorrencias", {
        embarcacaoId: Number(form.embarcacaoId),
        titulo: form.titulo,
        descricao: form.descricao,
        gravidade: form.gravidade,
        usuarioId: usuario.id,
      });
      toast.success("Ocorrência registrada");
      setDialogOpen(false);
      setForm({ embarcacaoId: "", titulo: "", descricao: "", gravidade: "MEDIA" });
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const atualizarStatus = async (id: number, status: StatusOcorrencia) => {
    try {
      await api.patch(`/ocorrencias/${id}/status`, { status });
      toast.success("Status atualizado");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap justify-between gap-4">
        <h1 className="text-2xl font-bold">Ocorrências</h1>
        <Button onClick={() => setDialogOpen(true)}><Plus className="h-4 w-4" /> Abrir ocorrência</Button>
      </div>

      <Select value={filtro} onChange={(e) => setFiltro(e.target.value as StatusOcorrencia | "")}>
        <option value="">Todos</option>
        <option value="ABERTA">Abertas</option>
        <option value="EM_ANALISE">Em análise</option>
        <option value="RESOLVIDA">Resolvidas</option>
      </Select>

      {loading ? <Loading /> : (
        <div className="space-y-3">
          {lista.map((o) => (
            <Card key={o.id}>
              <CardContent className="pt-4">
                <div className="flex flex-wrap justify-between gap-2">
                  <div>
                    <p className="font-semibold">{o.titulo}</p>
                    <p className="text-sm text-slate-500">{o.embarcacaoNome} · {o.usuarioNome}</p>
                    <p className="text-sm mt-1">{o.descricao}</p>
                  </div>
                  <div className="flex flex-col gap-2 items-end">
                    <Badge className={gravidadeClass[o.gravidade]}>{o.gravidade}</Badge>
                    <Badge className={statusOcorrenciaClass[o.status]}>{o.status}</Badge>
                    {o.status === "ABERTA" && (
                      <Button size="sm" variant="secondary" onClick={() => atualizarStatus(o.id, "EM_ANALISE")}>
                        Em análise
                      </Button>
                    )}
                    {o.status !== "RESOLVIDA" && (
                      <Button size="sm" onClick={() => atualizarStatus(o.id, "RESOLVIDA")}>
                        Resolver
                      </Button>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent title="Nova ocorrência">
          <div className="space-y-3">
            <Select value={form.embarcacaoId} onChange={(e) => setForm({ ...form, embarcacaoId: e.target.value })}>
              <option value="">Embarcação...</option>
              {embarcacoes.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
            </Select>
            <Input placeholder="Título" value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} />
            <Input placeholder="Descrição" value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} />
            <Select value={form.gravidade} onChange={(e) => setForm({ ...form, gravidade: e.target.value as GravidadeOcorrencia })}>
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Média</option>
              <option value="ALTA">Alta</option>
            </Select>
            <Button className="w-full" onClick={criar}>Registrar</Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
