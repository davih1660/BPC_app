"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Embarcacao, PageResponse, Ocorrencia, Manutencao } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Loading } from "@/components/ui/loading";
import { statusEmbarcacaoClass, statusEmbarcacaoLabel } from "@/lib/labels";
import { Ban } from "lucide-react";
import { toast } from "sonner";

export default function EmbarcacoesPage() {
  const [data, setData] = useState<PageResponse<Embarcacao> | null>(null);
  const [ocorrencias, setOcorrencias] = useState<Ocorrencia[]>([]);
  const [manutencoes, setManutencoes] = useState<Manutencao[]>([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    const params = new URLSearchParams({ page: "0", size: "50" });
    if (q) params.set("q", q);
    Promise.all([
      api.get<PageResponse<Embarcacao>>(`/embarcacoes?${params}`),
      api.get<Ocorrencia[]>("/ocorrencias"),
      api.get<Manutencao[]>("/manutencoes"),
    ])
      .then(([emb, oc, man]) => {
        setData(emb);
        setOcorrencias(oc);
        setManutencoes(man);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [q]);

  const interditar = async (id: number) => {
    const motivo = prompt("Motivo da interdição:");
    if (!motivo) return;
    try {
      await api.post(`/embarcacoes/${id}/interditar`, { motivo });
      toast.success("Embarcação interditada");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const status = (e: Embarcacao) => e.statusEfetivo ?? e.status;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Gestão de Embarcações</h1>
      <Input placeholder="Buscar..." value={q} onChange={(e) => setQ(e.target.value)} className="max-w-sm" />

      <Tabs defaultValue="lista">
        <TabsList>
          <TabsTrigger value="lista">Lista</TabsTrigger>
          <TabsTrigger value="ocorrencias">Ocorrências</TabsTrigger>
          <TabsTrigger value="manutencoes">Manutenções</TabsTrigger>
        </TabsList>

        <TabsContent value="lista">
          {loading ? <Loading /> : (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {data?.content.map((e) => (
                <Card key={e.id}>
                  <CardContent className="pt-4">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-semibold">{e.nome}</p>
                        <p className="text-xs text-slate-500">{e.tipo} · cap. {e.capacidade}</p>
                      </div>
                      <Badge className={statusEmbarcacaoClass[status(e)]}>
                        {statusEmbarcacaoLabel[status(e)]}
                      </Badge>
                    </div>
                    {e.observacoes && <p className="text-xs text-slate-500 mt-2">{e.observacoes}</p>}
                    {status(e) !== "INTERDITADA" && (
                      <Button variant="destructive" size="sm" className="mt-3 w-full" onClick={() => interditar(e.id)}>
                        <Ban className="h-3 w-3 mr-1" /> Interditar
                      </Button>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="ocorrencias">
          <ul className="space-y-2">
            {ocorrencias.map((o) => (
              <li key={o.id} className="p-3 border rounded-lg bg-white text-sm">
                <span className="font-medium">{o.titulo}</span> — {o.embarcacaoNome} ({o.status})
              </li>
            ))}
          </ul>
        </TabsContent>

        <TabsContent value="manutencoes">
          <ul className="space-y-2">
            {manutencoes.map((m) => (
              <li key={m.id} className="p-3 border rounded-lg bg-white text-sm">
                {m.embarcacaoNome}: {m.descricao} ({m.status})
              </li>
            ))}
          </ul>
        </TabsContent>
      </Tabs>
    </div>
  );
}
