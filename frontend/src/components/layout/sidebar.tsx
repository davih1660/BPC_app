"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Waves } from "lucide-react";
import { cn } from "@/lib/utils";
import { usePermissoes } from "@/hooks/use-permissoes";
import type { ItemMenu } from "@/lib/permissoes";

const SECOES: { titulo: string; rotas: string[] }[] = [
  { titulo: "Operação", rotas: ["/", "/agenda", "/reservas", "/uso-livre", "/notificacoes"] },
  { titulo: "Cadastros", rotas: ["/alunos", "/planos", "/embarcacoes", "/aulas", "/ocorrencias", "/tipos-ocorrencia"] },
  { titulo: "Financeiro", rotas: ["/cobrancas", "/bloqueios", "/relatorios"] },
  { titulo: "Integrações", rotas: ["/integracao/wellhub"] },
];

function itensDaSecao(menu: ItemMenu[], rotas: string[]) {
  const rotasSet = new Set(rotas);
  return menu.filter((item) => rotasSet.has(item.href));
}

export function StaffSidebar() {
  const pathname = usePathname();
  const { menu, perfilLabel, perfilSubtitulo } = usePermissoes();

  const renderLink = ({ href, label, icon: Icon }: ItemMenu) => {
    const ativo = pathname === href || (href !== "/" && pathname.startsWith(href));
    return (
      <Link
        key={href}
        href={href}
        className={cn(
          "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
          ativo
            ? "bg-primary-container text-on-primary-container"
            : "text-muted hover:bg-surface-variant hover:text-foreground"
        )}
      >
        <Icon className={cn("h-4 w-4", ativo && "text-primary")} />
        {label}
      </Link>
    );
  };

  return (
    <aside className="hidden md:flex w-60 flex-col border-r border-outline bg-surface min-h-screen shrink-0">
      <div className="flex items-center gap-2 px-5 py-5 border-b border-outline">
        <Waves className="h-7 w-7 text-primary" />
        <div>
          <p className="font-semibold text-foreground leading-tight">BPC Remo</p>
          <p className="text-xs text-muted">{perfilSubtitulo}</p>
        </div>
      </div>
      <div className="px-4 py-3 border-b border-outline">
        <p className="text-[10px] uppercase tracking-wider text-muted">Perfil</p>
        <p className="text-sm font-medium text-primary">{perfilLabel}</p>
      </div>
      <nav className="flex-1 p-3 space-y-4 overflow-y-auto">
        {SECOES.map((secao) => {
          const itens = itensDaSecao(menu, secao.rotas);
          if (itens.length === 0) return null;
          return (
            <div key={secao.titulo}>
              <p className="px-3 mb-1.5 text-[10px] font-semibold uppercase tracking-wider text-muted">
                {secao.titulo}
              </p>
              <div className="space-y-0.5">{itens.map(renderLink)}</div>
            </div>
          );
        })}
        {(() => {
          const rotasMapeadas = new Set(SECOES.flatMap((s) => s.rotas));
          const extras = menu.filter((m) => !rotasMapeadas.has(m.href));
          if (extras.length === 0) return null;
          return (
            <div>
              <p className="px-3 mb-1.5 text-[10px] font-semibold uppercase tracking-wider text-muted">
                Outros
              </p>
              <div className="space-y-0.5">{extras.map(renderLink)}</div>
            </div>
          );
        })()}
      </nav>
    </aside>
  );
}

/** @deprecated Use StaffSidebar via StaffShell */
export function Sidebar() {
  return <StaffSidebar />;
}
