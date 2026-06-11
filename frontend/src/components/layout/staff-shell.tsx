"use client";

import { StaffSidebar } from "@/components/layout/sidebar";
import { UserSwitcher } from "@/components/layout/user-switcher";
import { NotificationBell } from "@/components/domain/notification-bell";
import { useAuth } from "@/contexts/auth-context";
import { Button } from "@/components/ui/button";
import { LogOut } from "lucide-react";
import { usePermissoes } from "@/hooks/use-permissoes";

export function StaffShell({ children }: { children: React.ReactNode }) {
  const { perfilLabel } = usePermissoes();
  const { logout } = useAuth();

  return (
    <div className="flex min-h-screen bg-background">
      <StaffSidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <header className="sticky top-0 z-40 flex h-14 items-center justify-between border-b border-outline bg-surface/95 backdrop-blur px-4 md:px-6">
          <p className="text-sm text-muted md:hidden">BPC Remo · {perfilLabel}</p>
          <div className="hidden md:block" />
          <div className="flex items-center gap-1">
            <NotificationBell />
            <Button variant="ghost" size="icon" onClick={() => logout()} aria-label="Sair">
              <LogOut className="h-4 w-4" />
            </Button>
            <UserSwitcher />
          </div>
        </header>
        <main className="flex-1 p-4 md:p-6 overflow-auto max-w-7xl w-full mx-auto">
          {children}
        </main>
      </div>
    </div>
  );
}
