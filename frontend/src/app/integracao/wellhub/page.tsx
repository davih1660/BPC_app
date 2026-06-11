"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { WellhubSyncErro } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Loading } from "@/components/ui/loading";
import { toast } from "sonner";
import { Check } from "lucide-react";

export default function WellhubIntegracaoPage() {
  const [erros, setErros] = useState<WellhubSyncErro[]>([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    api
      .get<WellhubSyncErro[]>("/integracao/wellhub/erros")
      .then(setErros)
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const resolver = async (id: number) => {
    try {
      await api.patch(`/integracao/wellhub/erros/${id}/resolver`);
      toast.success("Erro marcado como resolvido");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const simularImport = async () => {
    try {
      await api.post("/integracao/wellhub/reservas", {
        wellhubReservaId: `WH-${Date.now()}`,
        horarioId: 1,
        alunoId: 30,
        data: new Date().toISOString().slice(0, 10),
      });
      toast.success("Reserva Wellhub importada (demo)");
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  if (loading) return <Loading />;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Integração Wellhub</h1>
          <p className="text-sm text-slate-500">Fila de erros de sincronização e importação manual</p>
        </div>
        <Button variant="outline" onClick={simularImport}>Simular import (demo)</Button>
      </div>

      {erros.length === 0 ? (
        <p className="text-sm text-slate-500">Nenhum erro pendente de sincronização.</p>
      ) : (
        <div className="space-y-3">
          {erros.map((e) => (
            <Card key={e.id}>
              <CardContent className="pt-4 flex justify-between gap-4">
                <div className="min-w-0">
                  <p className="font-medium text-red-700">{e.mensagem}</p>
                  <p className="text-xs text-slate-500 truncate">{e.payload}</p>
                  <p className="text-xs text-slate-400">{e.criadoEm}</p>
                </div>
                <Button size="sm" onClick={() => resolver(e.id)}>
                  <Check className="h-4 w-4 mr-1" /> Resolver
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
