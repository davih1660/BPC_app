"use client";

import { useCallback, useEffect, useState } from "react";
import { format, subDays } from "date-fns";
import { api, ApiError } from "@/lib/api";
import type { RelatorioResumo } from "@/lib/types";
import { PageHeader } from "@/components/layout/page-header";
import { StatTile } from "@/components/layout/stat-tile";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Loading } from "@/components/ui/loading";
import { formatMoeda } from "@/lib/labels";
import { BarChart3, Users, UserX, Receipt, ListOrdered } from "lucide-react";
import { toast } from "sonner";
import { hojeSaoPaulo } from "@/lib/relogio";

export default function RelatoriosPage() {
  const hoje = hojeSaoPaulo();
  const [de, setDe] = useState(format(subDays(new Date(hoje + "T12:00:00"), 30), "yyyy-MM-dd"));
  const [ate, setAte] = useState(hoje);
  const [resumo, setResumo] = useState<RelatorioResumo | null>(null);
  const [loading, setLoading] = useState(true);

  const carregar = useCallback(() => {
    setLoading(true);
    api
      .get<RelatorioResumo>(`/relatorios/resumo?de=${de}&ate=${ate}`)
      .then(setResumo)
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  }, [de, ate]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Relatórios"
        description="Ocupação, no-show e receita no período"
        actions={
          <div className="flex flex-wrap items-center gap-2">
            <Input type="date" value={de} onChange={(e) => setDe(e.target.value)} className="w-auto" />
            <span className="text-muted text-sm">até</span>
            <Input type="date" value={ate} onChange={(e) => setAte(e.target.value)} className="w-auto" />
            <Button variant="outline" size="sm" onClick={carregar}>
              Atualizar
            </Button>
          </div>
        }
      />

      {loading ? (
        <Loading />
      ) : resumo ? (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <StatTile
              label="Ocupação média"
              value={`${resumo.ocupacaoMediaPercent.toFixed(1)}%`}
              icon={BarChart3}
            />
            <StatTile label="No-show" value={String(resumo.totalNoShow)} icon={UserX} />
            <StatTile label="Receita (paga)" value={formatMoeda(resumo.receitaPaga)} icon={Receipt} />
            <StatTile label="Alunos ativos" value={String(resumo.alunosAtivos)} icon={Users} />
            <StatTile
              label="Lista de espera"
              value={`${resumo.entradasListaEspera} entradas / ${resumo.promocoesListaEspera} promoções`}
              icon={ListOrdered}
            />
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Ocupação por horário</CardTitle>
            </CardHeader>
            <CardContent>
              {resumo.ocupacaoPorHorario.length === 0 ? (
                <p className="text-sm text-muted">Sem dados no período.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-outline text-left">
                        <th className="pb-2 font-medium">Horário</th>
                        <th className="pb-2 font-medium">Inscritos</th>
                        <th className="pb-2 font-medium">Capacidade</th>
                        <th className="pb-2 font-medium">%</th>
                      </tr>
                    </thead>
                    <tbody>
                      {resumo.ocupacaoPorHorario.map((o) => (
                        <tr key={o.horarioId} className="border-b border-outline/50 last:border-0">
                          <td className="py-2">{o.titulo}</td>
                          <td className="py-2">{o.totalInscritos}</td>
                          <td className="py-2">{o.totalCapacidade}</td>
                          <td className="py-2">{o.percentual.toFixed(1)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>
        </>
      ) : null}
    </div>
  );
}
