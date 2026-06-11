import type { LucideIcon } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

const iconColors = {
  primary: "bg-primary-container text-primary",
  success: "bg-success-bg text-success",
  warning: "bg-warning-bg text-warning",
  error: "bg-error-bg text-error",
  info: "bg-info-bg text-info",
  neutral: "bg-surface-variant text-muted",
} as const;

export function StatTile({
  label,
  value,
  icon: Icon,
  tone = "neutral",
}: {
  label: string;
  value: string | number;
  icon: LucideIcon;
  tone?: keyof typeof iconColors;
}) {
  return (
    <Card variant="outlined">
      <CardContent className="pt-5 flex items-center gap-4">
        <div className={cn("p-3 rounded-xl", iconColors[tone])}>
          <Icon className="h-5 w-5" />
        </div>
        <div>
          <p className="text-2xl font-bold text-foreground">{value}</p>
          <p className="text-sm text-muted">{label}</p>
        </div>
      </CardContent>
    </Card>
  );
}
