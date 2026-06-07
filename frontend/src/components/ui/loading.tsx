export function Loading({ text = "Carregando..." }: { text?: string }) {
  return (
    <div className="flex items-center justify-center py-12 text-slate-500">
      <div className="h-6 w-6 animate-spin rounded-full border-2 border-sky-600 border-t-transparent mr-3" />
      {text}
    </div>
  );
}
