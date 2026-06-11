"use client";

import { useCallback, useEffect, useState } from "react";
import { formatDistanceToNow } from "date-fns";
import { ptBR } from "date-fns/locale";
import { Bell } from "lucide-react";
import { api } from "@/lib/api";
import type { Notificacao } from "@/lib/types";
import { useAuth } from "@/contexts/auth-context";
import { PageHeader } from "@/components/layout/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loading } from "@/components/ui/loading";
import { EmptyState } from "@/components/ui/empty-state";

export default function NotificacoesPage() {
  const { usuario } = useAuth();
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([]);
  const [loading, setLoading] = useState(true);

  const carregar = useCallback(() => {
    if (!usuario?.id) return;
    setLoading(true);
    api
      .get<Notificacao[]>(`/notificacoes?usuarioId=${usuario.id}`)
      .then(setNotificacoes)
      .finally(() => setLoading(false));
  }, [usuario?.id]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const marcarLida = async (id: number) => {
    await api.patch(`/notificacoes/${id}/lida`);
    carregar();
  };

  return (
    <div className="space-y-4">
      <PageHeader title="Notificações" description="Avisos sobre reservas, cobranças e agenda" />

      {loading ? (
        <Loading />
      ) : notificacoes.length === 0 ? (
        <EmptyState
          icon={Bell}
          title="Nenhuma notificação"
          description="Você será avisado aqui sobre reservas, lista de espera e bloqueios."
        />
      ) : (
        <ul className="space-y-2">
          {notificacoes.map((n) => (
            <li key={n.id}>
              <Card variant={n.lida ? "outlined" : "filled"}>
                <CardContent className="py-3 flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <p className="font-medium text-foreground">{n.titulo}</p>
                      {!n.lida && <Badge variant="info">Nova</Badge>}
                    </div>
                    <p className="text-sm text-muted mt-1">{n.mensagem}</p>
                    <p className="text-xs text-muted mt-2">
                      {formatDistanceToNow(new Date(n.criadoEm), { addSuffix: true, locale: ptBR })}
                    </p>
                  </div>
                  {!n.lida && (
                    <Button variant="outline" size="sm" onClick={() => marcarLida(n.id)}>
                      Marcar lida
                    </Button>
                  )}
                </CardContent>
              </Card>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
