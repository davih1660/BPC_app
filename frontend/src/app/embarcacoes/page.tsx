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
import {
  gravidadeLabel,
  gravidadeVariant,
  statusEmbarcacaoVariant,
  statusEmbarcacaoLabel,
  statusManutencaoLabel,
  statusManutencaoVariant,
  statusOcorrenciaLabel,
  statusOcorrenciaVariant,
} from "@/lib/labels";
import { PageHeader } from "@/components/layout/page-header";
import { EmptyState } from "@/components/ui/empty-state";
import { Ban } from "lucide-react";
import { toast } from "sonner";
import { usePermissoes } from "@/hooks/use-permissoes";

export default function EmbarcacoesPage() {
  const { funcionalidades, perfilLabel } = usePermissoes();
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
      <PageHeader
        title="Gestão de Embarcações"
        description={
          funcionalidades.interditarEmbarcacao
            ? "Interdição e monitoramento de status"
            : `Visão ${perfilLabel.toLowerCase()} — consulta`
        }
        actions={
          <Input placeholder="Buscar..." value={q} onChange={(e) => setQ(e.target.value)} className="max-w-xs" />
        }
      />

      <Tabs defaultValue="lista">
        <TabsList>
          <TabsTrigger value="lista">Lista</TabsTrigger>
          <TabsTrigger value="ocorrencias">Ocorrências</TabsTrigger>
          <TabsTrigger value="manutencoes">Manutenções</TabsTrigger>
        </TabsList>

        <TabsContent value="lista">
          {loading ? <Loading /> : data?.content.length === 0 ? (
            <EmptyState title="Nenhuma embarcação encontrada" description={q ? "Tente outro termo de busca." : undefined} />
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {data?.content.map((e) => (
                <Card key={e.id} variant="outlined">
                  <CardContent className="pt-4">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-semibold text-foreground">{e.nome}</p>
                        <p className="text-xs text-muted">{e.tipo} · cap. {e.capacidade}</p>
                      </div>
                      <Badge variant={statusEmbarcacaoVariant[status(e)]}>
                        {statusEmbarcacaoLabel[status(e)]}
                      </Badge>
                    </div>
                    {e.observacoes && <p className="text-xs text-muted mt-2">{e.observacoes}</p>}
                    {funcionalidades.interditarEmbarcacao && status(e) !== "INTERDITADA" && (
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
          {ocorrencias.length === 0 ? (
            <EmptyState title="Nenhuma ocorrência" description="Não há ocorrências registradas." />
          ) : (
            <ul className="space-y-2">
              {ocorrencias.map((o) => (
                <li key={o.id}>
                  <Card variant="outlined">
                    <CardContent className="py-3 flex flex-wrap items-center justify-between gap-2">
                      <div className="min-w-0">
                        <p className="font-medium text-foreground">{o.titulo}</p>
                        <p className="text-sm text-muted">{o.embarcacaoNome}</p>
                      </div>
                      <div className="flex flex-wrap items-center gap-1.5 shrink-0">
                        <Badge variant={gravidadeVariant[o.gravidade]}>
                          {gravidadeLabel[o.gravidade]}
                        </Badge>
                        <Badge variant={statusOcorrenciaVariant[o.status]}>
                          {statusOcorrenciaLabel[o.status]}
                        </Badge>
                      </div>
                    </CardContent>
                  </Card>
                </li>
              ))}
            </ul>
          )}
        </TabsContent>

        <TabsContent value="manutencoes">
          {manutencoes.length === 0 ? (
            <EmptyState title="Nenhuma manutenção" description="Não há manutenções registradas." />
          ) : (
            <ul className="space-y-2">
              {manutencoes.map((m) => (
                <li key={m.id}>
                  <Card variant="outlined">
                    <CardContent className="py-3 flex flex-wrap items-center justify-between gap-2">
                      <div className="min-w-0">
                        <p className="font-medium text-foreground">{m.embarcacaoNome}</p>
                        {m.descricao && <p className="text-sm text-muted">{m.descricao}</p>}
                        <p className="text-xs text-muted mt-0.5">
                          {m.dataInicio}
                          {m.dataFim ? ` → ${m.dataFim}` : ""}
                        </p>
                      </div>
                      <Badge variant={statusManutencaoVariant[m.status]}>
                        {statusManutencaoLabel[m.status]}
                      </Badge>
                    </CardContent>
                  </Card>
                </li>
              ))}
            </ul>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
