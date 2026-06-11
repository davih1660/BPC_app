"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Waves } from "lucide-react";
import { cn } from "@/lib/utils";
import { usePermissoes } from "@/hooks/use-permissoes";
import { useAuth } from "@/contexts/auth-context";
import { UserSwitcher } from "@/components/layout/user-switcher";
import { NotificationBell } from "@/components/domain/notification-bell";
import { SaldoPlanoBanner } from "@/components/domain/saldo-plano-banner";
import { Badge } from "@/components/ui/badge";

export function StudentShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { menu } = usePermissoes();
  const { usuario } = useAuth();

  const primeiroNome = usuario?.nome?.split(" ")[0] ?? "Aluno";

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <header className="sticky top-0 z-40 border-b border-outline bg-surface/95 backdrop-blur">
        <div className="flex h-14 items-center justify-between gap-3 px-4 max-w-lg mx-auto w-full md:max-w-3xl">
          <div className="flex items-center gap-2 min-w-0">
            <Waves className="h-6 w-6 text-primary shrink-0" />
            <div className="min-w-0">
              <p className="text-sm font-semibold text-foreground truncate">Olá, {primeiroNome}</p>
              <p className="text-xs text-muted truncate">BPC Remo</p>
            </div>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <NotificationBell compact />
            <UserSwitcher compact />
          </div>
        </div>
        <div className="px-4 pb-2 max-w-lg mx-auto w-full md:max-w-3xl">
          <SaldoPlanoBanner />
        </div>
      </header>

      <main className="flex-1 px-4 py-5 pb-24 max-w-lg mx-auto w-full md:max-w-3xl">
        {children}
      </main>

      <nav className="fixed bottom-0 inset-x-0 z-50 border-t border-outline bg-surface safe-area-pb shadow-[0_-4px_12px_rgba(0,0,0,0.04)]">
        <ul className="flex max-w-lg mx-auto md:max-w-3xl">
          {menu.map(({ href, label, icon: Icon }) => {
            const ativo = pathname === href || (href !== "/" && pathname.startsWith(href));
            return (
              <li key={href} className="flex-1">
                <Link
                  href={href}
                  className={cn(
                    "relative flex flex-col items-center justify-center gap-0.5 py-2.5 px-1 text-xs font-medium transition-colors",
                    ativo ? "text-primary" : "text-muted"
                  )}
                >
                  {ativo && (
                    <span className="absolute top-0 left-1/2 -translate-x-1/2 w-10 h-0.5 rounded-full bg-primary" />
                  )}
                  <Icon className="h-5 w-5" />
                  <span className="truncate max-w-full text-center leading-tight">{label}</span>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>
    </div>
  );
}
