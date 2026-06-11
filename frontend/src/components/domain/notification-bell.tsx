"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Bell } from "lucide-react";
import { api } from "@/lib/api";
import type { Notificacao } from "@/lib/types";
import { useAuth } from "@/contexts/auth-context";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { formatDistanceToNow } from "date-fns";
import { ptBR } from "date-fns/locale";

export function NotificationBell({ compact = false }: { compact?: boolean }) {
  const { usuario } = useAuth();
  const [naoLidas, setNaoLidas] = useState(0);
  const [recentes, setRecentes] = useState<Notificacao[]>([]);
  const [aberto, setAberto] = useState(false);

  const carregar = useCallback(() => {
    if (!usuario?.id) return;
    Promise.all([
      api.get<{ naoLidas: number }>(`/notificacoes/contagem?usuarioId=${usuario.id}`),
      api.get<Notificacao[]>(`/notificacoes?usuarioId=${usuario.id}&somenteNaoLidas=true`),
    ])
      .then(([contagem, lista]) => {
        setNaoLidas(contagem.naoLidas);
        setRecentes(lista.slice(0, 5));
      })
      .catch(() => {
        setNaoLidas(0);
        setRecentes([]);
      });
  }, [usuario?.id]);

  useEffect(() => {
    carregar();
    const interval = setInterval(carregar, 30_000);
    const onFocus = () => carregar();
    window.addEventListener("focus", onFocus);
    return () => {
      clearInterval(interval);
      window.removeEventListener("focus", onFocus);
    };
  }, [carregar]);

  const marcarLida = async (id: number) => {
    await api.patch(`/notificacoes/${id}/lida`);
    carregar();
  };

  if (!usuario) return null;

  return (
    <div className="relative">
      <Button
        variant="ghost"
        size="icon"
        className={cn("relative", compact ? "h-9 w-9" : "h-9 w-9")}
        onClick={() => setAberto(!aberto)}
        aria-label="Notificações"
      >
        <Bell className="h-4 w-4" />
        {naoLidas > 0 && (
          <span className="absolute -top-0.5 -right-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-error px-1 text-[10px] font-bold text-on-error">
            {naoLidas > 9 ? "9+" : naoLidas}
          </span>
        )}
      </Button>
      {aberto && (
        <>
          <div className="fixed inset-0 z-40" onClick={() => setAberto(false)} />
          <div className="absolute right-0 top-full mt-1 z-50 w-80 rounded-xl border border-outline bg-surface shadow-md">
            <div className="flex items-center justify-between border-b border-outline px-3 py-2">
              <p className="text-sm font-semibold">Notificações</p>
              <Link
                href="/notificacoes"
                className="text-xs text-primary hover:underline"
                onClick={() => setAberto(false)}
              >
                Ver todas
              </Link>
            </div>
            <ul className="max-h-72 overflow-y-auto">
              {recentes.length === 0 ? (
                <li className="px-3 py-4 text-sm text-muted text-center">Nenhuma notificação nova</li>
              ) : (
                recentes.map((n) => (
                  <li key={n.id}>
                    <button
                      type="button"
                      className="w-full text-left px-3 py-2.5 hover:bg-surface-variant border-b border-outline/50 last:border-0"
                      onClick={() => marcarLida(n.id)}
                    >
                      <p className="text-sm font-medium text-foreground">{n.titulo}</p>
                      <p className="text-xs text-muted line-clamp-2 mt-0.5">{n.mensagem}</p>
                      <p className="text-[10px] text-muted mt-1">
                        {formatDistanceToNow(new Date(n.criadoEm), { addSuffix: true, locale: ptBR })}
                      </p>
                    </button>
                  </li>
                ))
              )}
            </ul>
          </div>
        </>
      )}
    </div>
  );
}
