import { Skeleton } from "@/components/ui/skeleton";

export function Loading({ rows = 3 }: { rows?: number }) {
  return (
    <div className="space-y-3 py-4" role="status" aria-label="Carregando">
      {Array.from({ length: rows }).map((_, i) => (
        <Skeleton key={i} className="h-16 w-full rounded-xl" />
      ))}
    </div>
  );
}
