"use client";

import type { OperacaoHorarioSlot, StatusSlot } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ListaInscritos } from "@/components/domain/lista-inscritos";
import { formatHorario, statusSlotVariant, statusSlotLabel, statusSlotCardClass } from "@/lib/labels";
import { ChevronDown, ChevronUp } from "lucide-react";
import { cn } from "@/lib/utils";

interface SlotHorarioCardProps {
  slot: OperacaoHorarioSlot;
  expandido: boolean;
  onToggle: () => void;
  destaque?: boolean;
  realce?: boolean;
  onPresenca?: (id: number, presente: boolean) => void;
  onCancelar?: (id: number) => void;
  density?: "staff" | "student";
}

export function SlotAulaCard({
  slot,
  expandido,
  onToggle,
  destaque = false,
  realce = false,
  onPresenca,
  onCancelar,
  density = "staff",
}: SlotHorarioCardProps) {
  const presentes = slot.inscritos.filter((i) => i.presente).length;
  const isStudent = density === "student";

  return (
    <div id={`horario-slot-${slot.horario.id}`}>
      <Card
        variant={isStudent ? "student" : "elevated"}
        className={cn(
          statusSlotCardClass[slot.statusSlot],
          destaque && "shadow-md",
          realce && "ring-2 ring-primary/30"
        )}
      >
        <CardContent className={cn("pt-4", isStudent && "p-4")}>
          <div className={cn("flex gap-3", isStudent ? "items-center" : "items-start justify-between flex-wrap")}>
            {isStudent && (
              <div className="shrink-0 text-center min-w-[52px]">
                <p className="text-lg font-bold text-primary leading-none">
                  {slot.horario.horarioInicio?.slice(0, 5)}
                </p>
                <p className="text-xs text-muted mt-0.5">
                  {slot.horario.horarioFim?.slice(0, 5)}
                </p>
              </div>
            )}
            <div className="flex-1 min-w-0 space-y-1">
              {!isStudent && (
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-foreground">
                    {formatHorario(slot.horario.horarioInicio, slot.horario.horarioFim)}
                  </span>
                  <Badge variant={statusSlotVariant[slot.statusSlot]}>
                    {statusSlotLabel[slot.statusSlot]}
                  </Badge>
                  {slot.lotada && <Badge variant="warning">Lotada</Badge>}
                </div>
              )}
              {isStudent && (
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant={statusSlotVariant[slot.statusSlot]}>
                    {statusSlotLabel[slot.statusSlot]}
                  </Badge>
                  {slot.lotada ? (
                    <Badge variant="warning">Lotado</Badge>
                  ) : (
                    <Badge variant="success">
                      {slot.capacidade - slot.totalInscritos} vagas
                    </Badge>
                  )}
                </div>
              )}
              <p className={cn("text-foreground", isStudent ? "font-medium" : "text-sm")}>
                {slot.horario.titulo}
              </p>
              <p className="text-xs text-muted">
                {slot.totalInscritos}/{slot.capacidade} inscritos
                {presentes > 0 && ` · ${presentes} presentes`}
              </p>
            </div>
            <Button variant="ghost" size="sm" onClick={onToggle} className="shrink-0">
              {expandido ? (
                <>Ocultar <ChevronUp className="h-4 w-4 ml-1" /></>
              ) : (
                <>Inscritos <ChevronDown className="h-4 w-4 ml-1" /></>
              )}
            </Button>
          </div>
          {expandido && (
            <div className="mt-4 pt-3 border-t border-outline">
              <ListaInscritos
                inscritos={slot.inscritos}
                onPresenca={onPresenca}
                onCancelar={onCancelar}
                compact
              />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

export function isSlotDestaque(status: StatusSlot) {
  return status === "EM_ANDAMENTO" || status === "PROXIMA";
}
