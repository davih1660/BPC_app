"use client";

import { useState } from "react";
import { User, ChevronDown } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { usePermissoes } from "@/hooks/use-permissoes";
import { Select } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { PERFIL_LABEL } from "@/lib/permissoes";
import type { TipoUsuario } from "@/lib/types";
import { cn } from "@/lib/utils";

const BADGE_PERFIL: Record<TipoUsuario, "default" | "info" | "success" | "warning" | "neutral"> = {
  ADMIN: "default",
  PROFESSOR: "info",
  ALUNO: "success",
  MANUTENCAO: "warning",
};

export function UserSwitcher({ compact = false }: { compact?: boolean }) {
  const { usuario, usuarios, setUsuario } = useAuth();
  const { perfilLabel } = usePermissoes();
  const [devOpen, setDevOpen] = useState(false);

  if (process.env.NODE_ENV !== "development") return null;

  const porPerfil = (["ADMIN", "PROFESSOR", "MANUTENCAO", "ALUNO"] as TipoUsuario[])
    .map((tipo) => ({
      tipo,
      lista: usuarios.filter((u) => u.tipoUsuario === tipo),
    }))
    .filter((g) => g.lista.length > 0);

  if (compact) {
    return (
      <div className="relative">
        <Button
          variant="ghost"
          size="icon"
          className="h-9 w-9"
          onClick={() => setDevOpen(!devOpen)}
          aria-label="Trocar usuário"
        >
          <User className="h-4 w-4" />
        </Button>
        {devOpen && (
          <>
            <div className="fixed inset-0 z-40" onClick={() => setDevOpen(false)} />
            <div className="absolute right-0 top-full mt-1 z-50 w-64 rounded-xl border border-outline bg-surface p-3 shadow-md">
              <p className="text-xs text-muted mb-2">Simular login (dev)</p>
              <Select
                className="w-full"
                value={usuario?.id?.toString() ?? ""}
                onChange={(e) => {
                  const id = Number(e.target.value);
                  const u = usuarios.find((x) => x.id === id);
                  if (u) setUsuario(u);
                  setDevOpen(false);
                }}
              >
                <option value="">Selecione...</option>
                {porPerfil.map(({ tipo, lista }) => (
                  <optgroup key={tipo} label={PERFIL_LABEL[tipo]}>
                    {lista.map((u) => (
                      <option key={u.id} value={u.id}>{u.nome}</option>
                    ))}
                  </optgroup>
                ))}
              </Select>
            </div>
          </>
        )}
      </div>
    );
  }

  return (
    <div className="flex items-center gap-2 ml-auto">
      {usuario && (
        <Badge variant={BADGE_PERFIL[usuario.tipoUsuario]} className="hidden sm:inline-flex">
          {perfilLabel}
        </Badge>
      )}
      <details className="relative group">
        <summary className={cn(
          "flex items-center gap-2 cursor-pointer list-none rounded-lg px-2 py-1.5 hover:bg-surface-variant text-sm text-muted"
        )}>
          <User className="h-4 w-4" />
          <span className="hidden lg:inline max-w-[120px] truncate">{usuario?.nome ?? "Conta"}</span>
          <ChevronDown className="h-3 w-3" />
        </summary>
        <div className="absolute right-0 top-full mt-1 z-50 w-72 rounded-xl border border-outline bg-surface p-3 shadow-md">
          <p className="text-xs font-medium text-muted mb-2">Simular login (protótipo)</p>
          <Select
            value={usuario?.id?.toString() ?? ""}
            onChange={(e) => {
              const id = Number(e.target.value);
              const u = usuarios.find((x) => x.id === id);
              if (u) setUsuario(u);
            }}
          >
            <option value="">Selecione...</option>
            {porPerfil.map(({ tipo, lista }) => (
              <optgroup key={tipo} label={PERFIL_LABEL[tipo]}>
                {lista.map((u) => (
                  <option key={u.id} value={u.id}>{u.nome}</option>
                ))}
              </optgroup>
            ))}
          </Select>
        </div>
      </details>
    </div>
  );
}
