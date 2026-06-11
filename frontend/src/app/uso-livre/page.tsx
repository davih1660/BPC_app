"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Embarcacao, SolicitacaoUsoLivre } from "@/lib/types";
import { useAuth } from "@/contexts/auth-context";
import { PageHeader } from "@/components/layout/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Chip } from "@/components/ui/chip";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";
import {
  formatHorario,
  statusSolicitacaoUsoLivreLabel,
  statusSolicitacaoUsoLivreVariant,
  tipoCanoaUsoLivreLabel,
} from "@/lib/labels";
import { Ship, Check, X } from "lucide-react";
import { toast } from "sonner";

export default function UsoLivrePage() {
  const { usuario } = useAuth();
  const [solicitacoes, setSolicitacoes] = useState<SolicitacaoUsoLivre[]>([]);
  const [embarcacoes, setEmbarcacoes] = useState<Embarcacao[]>([]);
  const [loading, setLoading] = useState(true);
  const [processando, setProcessando] = useState<number | null>(null);
  const [dialogAprovar, setDialogAprovar] = useState<SolicitacaoUsoLivre | null>(null);
  const [dialogRecusar, setDialogRecusar] = useState<SolicitacaoUsoLivre | null>(null);
  const [embarcacaoId, setEmbarcacaoId] = useState("");
  const [motivoRecusa, setMotivoRecusa] = useState("");

  const carregar = useCallback(() => {
    setLoading(true);
    Promise.all([
      api.get<SolicitacaoUsoLivre[]>("/solicitacoes-uso-livre"),
      api.get<{ content: Embarcacao[] }>("/embarcacoes?size=100"),
    ])
      .then(([s, emb]) => {
        setSolicitacoes(s);
        setEmbarcacoes(emb.content.filter((e) => ["OC1", "OC2", "OC3", "OC4", "OC6"].includes(e.tipo)));
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const pendentes = useMemo(
    () => solicitacoes.filter((s) => s.status === "PENDENTE"),
    [solicitacoes]
  );
  const historico = useMemo(
    () => solicitacoes.filter((s) => s.status !== "PENDENTE"),
    [solicitacoes]
  );

  const embarcacoesCompatíveis = useMemo(() => {
    if (!dialogAprovar) return [];
    return embarcacoes.filter((e) => e.tipo === dialogAprovar.tipoCanoaDesejada);
  }, [embarcacoes, dialogAprovar]);

  const aprovar = async () => {
    if (!dialogAprovar || !usuario?.id || !embarcacaoId) return;
    setProcessando(dialogAprovar.id);
    try {
      await api.patch(`/solicitacoes-uso-livre/${dialogAprovar.id}/aprovar`, {
        embarcacaoId: Number(embarcacaoId),
        processadoPorId: usuario.id,
      });
      toast.success("Solicitação aprovada");
      setDialogAprovar(null);
      setEmbarcacaoId("");
      carregar();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setProcessando(null);
    }
  };

  const recusar = async () => {
    if (!dialogRecusar || !usuario?.id) return;
    setProcessando(dialogRecusar.id);
    try {
      await api.patch(`/solicitacoes-uso-livre/${dialogRecusar.id}/recusar`, {
        processadoPorId: usuario.id,
        motivo: motivoRecusa || "Solicitação recusada pela equipe.",
      });
      toast.success("Solicitação recusada");
      setDialogRecusar(null);
      setMotivoRecusa("");
      carregar();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setProcessando(null);
    }
  };

  const renderCard = (s: SolicitacaoUsoLivre, acoes?: boolean) => (
    <Card key={s.id}>
      <CardContent className="py-4 space-y-3">
        <div className="flex flex-wrap items-start justify-between gap-2">
          <div>
            <p className="font-semibold text-foreground">{s.alunoNome}</p>
            <p className="text-sm text-muted">
              {s.data} · {formatHorario(s.horarioInicio, s.horarioFim)} · {s.horarioTitulo}
            </p>
            <p className="text-sm text-muted mt-1">
              Canoa desejada: {tipoCanoaUsoLivreLabel[s.tipoCanoaDesejada]}
            </p>
            {s.observacao && <p className="text-sm text-muted mt-1">Obs.: {s.observacao}</p>}
            {s.embarcacaoNome && (
              <p className="text-sm text-primary mt-1">Atribuída: {s.embarcacaoNome}</p>
            )}
            {s.motivoRecusa && (
              <p className="text-sm text-error mt-1">{s.motivoRecusa}</p>
            )}
          </div>
          <Chip variant={statusSolicitacaoUsoLivreVariant[s.status]}>
            {statusSolicitacaoUsoLivreLabel[s.status]}
          </Chip>
        </div>
        {acoes && (
          <div className="flex gap-2">
            <Button
              size="sm"
              onClick={() => {
                setDialogAprovar(s);
                setEmbarcacaoId("");
              }}
              disabled={processando === s.id}
            >
              <Check className="h-4 w-4" /> Aprovar
            </Button>
            <Button
              size="sm"
              variant="destructive"
              onClick={() => setDialogRecusar(s)}
              disabled={processando === s.id}
            >
              <X className="h-4 w-4" /> Recusar
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );

  return (
    <div className="space-y-4">
      <PageHeader
        title="Uso livre (sem professor)"
        description="Fila de solicitações de canoa OC — substitui a lista manual por WhatsApp"
      />

      {loading ? (
        <Loading />
      ) : (
        <Tabs defaultValue="pendentes">
          <TabsList>
            <TabsTrigger value="pendentes">
              Pendentes{pendentes.length > 0 ? ` (${pendentes.length})` : ""}
            </TabsTrigger>
            <TabsTrigger value="historico">Histórico</TabsTrigger>
          </TabsList>

          <TabsContent value="pendentes" className="space-y-3 mt-4">
            {pendentes.length === 0 ? (
              <EmptyState
                icon={Ship}
                title="Nenhuma solicitação pendente"
                description="Quando alunos solicitarem uso livre de canoa, aparecerão aqui."
              />
            ) : (
              pendentes.map((s) => renderCard(s, true))
            )}
          </TabsContent>

          <TabsContent value="historico" className="space-y-3 mt-4">
            {historico.length === 0 ? (
              <EmptyState title="Sem histórico" description="Solicitações processadas aparecerão aqui." />
            ) : (
              historico.map((s) => renderCard(s))
            )}
          </TabsContent>
        </Tabs>
      )}

      {dialogAprovar && (
        <Dialog open onOpenChange={() => setDialogAprovar(null)}>
          <DialogContent title="Aprovar uso livre">
            <div className="space-y-3">
              <p className="text-sm text-muted">
                {dialogAprovar.alunoNome} · {tipoCanoaUsoLivreLabel[dialogAprovar.tipoCanoaDesejada]} ·{" "}
                {dialogAprovar.data} {formatHorario(dialogAprovar.horarioInicio, dialogAprovar.horarioFim)}
              </p>
              <Select value={embarcacaoId} onChange={(e) => setEmbarcacaoId(e.target.value)}>
                <option value="">Selecione a embarcação...</option>
                {embarcacoesCompatíveis.map((e) => (
                  <option key={e.id} value={e.id}>
                    {e.nome} ({e.status})
                  </option>
                ))}
              </Select>
              {embarcacoesCompatíveis.length === 0 && (
                <p className="text-sm text-warning">Nenhuma embarcação compatível cadastrada.</p>
              )}
              <Button className="w-full" onClick={aprovar} disabled={!embarcacaoId || processando !== null}>
                Confirmar aprovação
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {dialogRecusar && (
        <Dialog open onOpenChange={() => setDialogRecusar(null)}>
          <DialogContent title="Recusar solicitação">
            <div className="space-y-3">
              <Input
                placeholder="Motivo (opcional)"
                value={motivoRecusa}
                onChange={(e) => setMotivoRecusa(e.target.value)}
              />
              <Button variant="destructive" className="w-full" onClick={recusar} disabled={processando !== null}>
                Confirmar recusa
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
