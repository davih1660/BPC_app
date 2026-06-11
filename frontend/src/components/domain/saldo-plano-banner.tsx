"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { SaldoPlano } from "@/lib/types";
import { useAuth } from "@/contexts/auth-context";
import { Card, CardContent } from "@/components/ui/card";
import { CreditCard } from "lucide-react";

export function SaldoPlanoBanner() {
  const { usuario } = useAuth();
  const [saldo, setSaldo] = useState<SaldoPlano | null>(null);

  useEffect(() => {
    if (!usuario?.id || usuario.tipoUsuario !== "ALUNO") return;
    api
      .get<SaldoPlano>(`/alunos/${usuario.id}/saldo`)
      .then(setSaldo)
      .catch(() => setSaldo(null));
  }, [usuario?.id, usuario?.tipoUsuario]);

  if (!saldo) return null;

  return (
    <Card variant="filled" className="border-primary/20 bg-primary-container/30">
      <CardContent className="py-3 flex items-center gap-3">
        <CreditCard className="h-5 w-5 text-primary shrink-0" />
        <div className="min-w-0">
          <p className="text-sm font-medium text-foreground">{saldo.planoNome}</p>
          <p className="text-sm text-muted">{saldo.descricao}</p>
        </div>
      </CardContent>
    </Card>
  );
}
