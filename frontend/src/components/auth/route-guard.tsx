"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/contexts/auth-context";
import { usePermissoes } from "@/hooks/use-permissoes";
import { Loading } from "@/components/ui/loading";

const ROTAS_PUBLICAS = ["/login"];

export function RouteGuard({ children }: { children: React.ReactNode }) {
  const { usuario, loading: authLoading } = useAuth();
  const { loading: permLoading, home, podeAcessar } = usePermissoes();
  const pathname = usePathname();
  const router = useRouter();

  const isPublica = ROTAS_PUBLICAS.some((r) => pathname === r || pathname.startsWith(r + "/"));
  const loading = authLoading || (!isPublica && permLoading);

  useEffect(() => {
    if (authLoading) return;
    if (!usuario && !isPublica) {
      router.replace("/login");
      return;
    }
    if (usuario && pathname === "/login") {
      router.replace(home);
      return;
    }
    if (usuario && !isPublica && !permLoading && !podeAcessar(pathname)) {
      router.replace(home);
    }
  }, [authLoading, permLoading, usuario, isPublica, pathname, home, podeAcessar, router]);

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <Loading />
      </div>
    );
  }

  if (!usuario && !isPublica) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <Loading />
      </div>
    );
  }

  if (usuario && !isPublica && !permLoading && !podeAcessar(pathname)) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <Loading />
      </div>
    );
  }

  return <>{children}</>;
}
