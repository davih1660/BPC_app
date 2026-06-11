"use client";

import { useCallback, useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Cobranca } from "@/lib/types";
import { PageHeader } from "@/components/layout/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";
import { formatMoeda } from "@/lib/labels";
import { Receipt } from "lucide-react";
import { toast } from "sonner";

const statusVariant: Record<Cobranca["status"], "success" | "warning" | "error" | "neutral"> = {
  PAGO: "success",
  PENDENTE: "warning",
  INADIMPLENTE: "error",
};

const statusLabel: Record<Cobranca["status"], string> = {
  PAGO: "Pago",
  PENDENTE: "Pendente",
  INADIMPLENTE: "Inadimplente",
};

export default function CobrancasPage() {
  const [cobrancas, setCobrancas] = useState<Cobranca[]>([]);
  const [loading, setLoading] = useState(true);
  const [processando, setProcessando] = useState<number | null>(null);

  const carregar = useCallback(() => {
    setLoading(true);
    api
      .get<Cobranca[]>("/cobrancas")
      .then(setCobrancas)
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const marcarPago = async (id: number) => {
    setProcessando(id);
    try {
      await api.patch(`/cobrancas/${id}/pago`);
      toast.success("Cobrança marcada como paga");
      carregar();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setProcessando(null);
    }
  };

  const marcarInadimplente = async (id: number) => {
    setProcessando(id);
    try {
      await api.patch(`/cobrancas/${id}/inadimplente`);
      toast.success("Aluno marcado como inadimplente");
      carregar();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setProcessando(null);
    }
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="Cobranças"
        description="Gestão manual de pagamentos (protótipo)"
      />

      {loading ? (
        <Loading />
      ) : cobrancas.length === 0 ? (
        <EmptyState icon={Receipt} title="Nenhuma cobrança" description="Cobranças aparecem ao vincular planos ou via seed." />
      ) : (
        <div className="overflow-x-auto rounded-xl border border-outline">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-outline bg-surface-variant text-left">
                <th className="p-3 font-medium">Aluno</th>
                <th className="p-3 font-medium">Plano</th>
                <th className="p-3 font-medium">Valor</th>
                <th className="p-3 font-medium">Vencimento</th>
                <th className="p-3 font-medium">Status</th>
                <th className="p-3 font-medium">Ações</th>
              </tr>
            </thead>
            <tbody>
              {cobrancas.map((c) => (
                <tr key={c.id} className="border-b border-outline last:border-0">
                  <td className="p-3">{c.alunoNome}</td>
                  <td className="p-3 text-muted">{c.planoNome ?? "—"}</td>
                  <td className="p-3">{formatMoeda(c.valor)}</td>
                  <td className="p-3">{new Date(c.vencimento + "T12:00:00").toLocaleDateString("pt-BR")}</td>
                  <td className="p-3">
                    <Badge variant={statusVariant[c.status]}>{statusLabel[c.status]}</Badge>
                  </td>
                  <td className="p-3">
                    <div className="flex gap-2 flex-wrap">
                      {c.status !== "PAGO" && (
                        <Button
                          size="sm"
                          variant="outline"
                          disabled={processando === c.id}
                          onClick={() => marcarPago(c.id)}
                        >
                          Marcar pago
                        </Button>
                      )}
                      {c.status !== "INADIMPLENTE" && (
                        <Button
                          size="sm"
                          variant="destructive"
                          disabled={processando === c.id}
                          onClick={() => marcarInadimplente(c.id)}
                        >
                          Inadimplente
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
