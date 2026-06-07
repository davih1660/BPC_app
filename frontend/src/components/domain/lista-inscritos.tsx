"use client";

import { Button } from "@/components/ui/button";
import { Check } from "lucide-react";
import type { ReservaAula } from "@/lib/types";

interface ListaInscritosProps {
  inscritos: ReservaAula[];
  onPresenca: (id: number, presente: boolean) => void;
  compact?: boolean;
}

export function ListaInscritos({ inscritos, onPresenca, compact }: ListaInscritosProps) {
  if (inscritos.length === 0) {
    return <p className="text-sm text-slate-500 py-2">Nenhum aluno agendado neste horário</p>;
  }

  return (
    <ul className={compact ? "space-y-1" : "space-y-2"}>
      {inscritos.map((r) => (
        <li
          key={r.id}
          className="flex items-center justify-between gap-2 text-sm border-b border-slate-100 pb-2 last:border-0"
        >
          <span className={r.presente ? "text-emerald-700 font-medium" : ""}>{r.alunoNome}</span>
          <Button
            size="sm"
            variant={r.presente ? "default" : "outline"}
            onClick={() => onPresenca(r.id, !r.presente)}
          >
            <Check className="h-3 w-3 mr-1" />
            {r.presente ? "Presente" : "Check-in"}
          </Button>
        </li>
      ))}
    </ul>
  );
}
