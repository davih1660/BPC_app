"use client";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/ui/empty-state";
import { Check, Trash2, Users } from "lucide-react";
import type { ReservaColetiva } from "@/lib/types";
import {
  situacaoAlunoVariant,
  situacaoAlunoLabel,
  origemReservaVariant,
  origemReservaLabel,
} from "@/lib/labels";

function iniciais(nome: string) {
  return nome
    .split(" ")
    .slice(0, 2)
    .map((p) => p[0])
    .join("")
    .toUpperCase();
}

interface ListaInscritosProps {
  inscritos: ReservaColetiva[];
  onPresenca?: (id: number, presente: boolean) => void;
  onCancelar?: (id: number) => void;
  compact?: boolean;
}

function ordenarPorNome(inscritos: ReservaColetiva[]) {
  return [...inscritos].sort((a, b) =>
    a.alunoNome.localeCompare(b.alunoNome, "pt-BR", { sensitivity: "base" })
  );
}

export function ListaInscritos({ inscritos, onPresenca, onCancelar, compact }: ListaInscritosProps) {
  const ordenados = ordenarPorNome(inscritos);

  if (ordenados.length === 0) {
    return (
      <EmptyState
        icon={Users}
        title="Nenhum aluno agendado"
        description="Este horário ainda não tem inscrições."
        className="py-6"
      />
    );
  }

  return (
    <ul className={compact ? "space-y-1" : "space-y-2"}>
      {ordenados.map((r) => (
        <li
          key={r.id}
          className="flex items-center justify-between gap-2 text-sm border-b border-outline pb-2 last:border-0"
        >
          <div className="flex items-center gap-2 min-w-0">
            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary-container text-xs font-semibold text-on-primary-container">
              {iniciais(r.alunoNome)}
            </span>
            <div className="min-w-0">
              <span className={r.presente ? "text-success font-medium" : "text-foreground"}>
                {r.alunoNome}
              </span>
              <div className="flex flex-wrap gap-1 mt-0.5">
                {r.situacaoAluno && (
                  <Badge variant={situacaoAlunoVariant[r.situacaoAluno]} className="text-[10px] px-1.5 py-0">
                    {situacaoAlunoLabel[r.situacaoAluno]}
                  </Badge>
                )}
                {r.origem && (
                  <Badge variant={origemReservaVariant[r.origem]} className="text-[10px] px-1.5 py-0">
                    {origemReservaLabel[r.origem]}
                  </Badge>
                )}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            {onPresenca && (
              <Button
                size="sm"
                variant={r.presente ? "filled" : "outline"}
                onClick={() => onPresenca(r.id, !r.presente)}
              >
                <Check className="h-3 w-3 mr-1" />
                {r.presente ? "Presente" : "Check-in"}
              </Button>
            )}
            {onCancelar && (
              <Button
                size="sm"
                variant="destructive"
                onClick={() => onCancelar(r.id)}
                title="Excluir agendamento"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            )}
          </div>
        </li>
      ))}
    </ul>
  );
}
