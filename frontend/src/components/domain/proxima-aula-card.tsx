"use client";

import { useCallback, useEffect, useState } from "react";
import { format } from "date-fns";
import { ptBR } from "date-fns/locale";
import { Calendar, Clock, X } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { ProximaReserva } from "@/lib/types";
import { useAuth } from "@/contexts/auth-context";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { formatHorario } from "@/lib/labels";
import { toast } from "sonner";

export function ProximaAulaCard({ onCancelado }: { onCancelado?: () => void }) {
  const { usuario } = useAuth();
  const [proxima, setProxima] = useState<ProximaReserva | null>(null);
  const [loading, setLoading] = useState(true);
  const [cancelando, setCancelando] = useState(false);

  const carregar = useCallback(() => {
    if (!usuario?.id) return;
    setLoading(true);
    api
      .get<ProximaReserva | null>(`/alunos/${usuario.id}/proxima-reserva`)
      .then(setProxima)
      .catch(() => setProxima(null))
      .finally(() => setLoading(false));
  }, [usuario?.id]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const cancelar = async () => {
    if (!proxima || !confirm("Cancelar esta reserva?")) return;
    setCancelando(true);
    try {
      await api.delete(`/reservas-coletivas/${proxima.reservaId}`);
      toast.success("Reserva cancelada");
      setProxima(null);
      onCancelado?.();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setCancelando(false);
    }
  };

  if (loading || !proxima) return null;

  const dataFormatada = format(new Date(proxima.dataReserva + "T12:00:00"), "EEEE, dd 'de' MMMM", {
    locale: ptBR,
  });

  return (
    <Card className="border-primary/30 bg-gradient-to-br from-primary-container/50 to-surface overflow-hidden">
      <CardContent className="p-5 space-y-4">
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-primary">Próxima aula</p>
            <h2 className="text-xl font-bold text-foreground mt-1">{proxima.horarioTitulo}</h2>
          </div>
          {proxima.origem === "WELLHUB" && <Badge variant="info">Wellhub</Badge>}
        </div>
        <div className="flex flex-wrap gap-4 text-sm text-muted">
          <span className="flex items-center gap-1.5 capitalize">
            <Calendar className="h-4 w-4" />
            {dataFormatada}
          </span>
          <span className="flex items-center gap-1.5">
            <Clock className="h-4 w-4" />
            {formatHorario(proxima.horarioInicio, proxima.horarioFim)}
          </span>
        </div>
        <div className="flex gap-2">
          {proxima.podeCancelar && (
            <Button
              variant="outline"
              size="sm"
              onClick={cancelar}
              disabled={cancelando}
              className="gap-1"
            >
              <X className="h-4 w-4" />
              {cancelando ? "Cancelando..." : "Cancelar"}
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
