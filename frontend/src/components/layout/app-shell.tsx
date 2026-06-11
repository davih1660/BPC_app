"use client";

import { usePathname } from "next/navigation";
import { usePermissoes } from "@/hooks/use-permissoes";
import { StudentShell } from "@/components/layout/student-shell";
import { StaffShell } from "@/components/layout/staff-shell";
import { Loading } from "@/components/ui/loading";

const SEM_SHELL = ["/login"];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { loading, perfil } = usePermissoes();

  if (SEM_SHELL.some((r) => pathname === r)) {
    return <>{children}</>;
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center p-8">
        <Loading rows={2} />
      </div>
    );
  }

  if (perfil === "ALUNO") {
    return <StudentShell>{children}</StudentShell>;
  }

  return <StaffShell>{children}</StaffShell>;
}
