"use client";

import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import type { AuthResponse, TipoUsuario, Usuario } from "@/lib/types";
import { api, setAuthToken, setUsuarioId, loadStoredToken, getAuthToken } from "@/lib/api";
import { ROTAS_POR_PERFIL } from "@/lib/permissoes";

interface AuthContextType {
  usuario: Usuario | null;
  usuarios: Usuario[];
  login: (email: string, senha: string) => Promise<void>;
  logout: () => Promise<void>;
  setUsuario: (u: Usuario) => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

const ORDEM_PERFIL: TipoUsuario[] = ["ADMIN", "PROFESSOR", "MANUTENCAO", "ALUNO"];

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [usuario, setUsuarioState] = useState<Usuario | null>(null);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  const carregarUsuariosDev = useCallback(() => {
    if (process.env.NODE_ENV !== "development") return;
    api
      .get<{ content: Usuario[] }>("/usuarios?size=100")
      .then((data) => {
        const ordenados = [...data.content].sort((a, b) => {
          const ia = ORDEM_PERFIL.indexOf(a.tipoUsuario);
          const ib = ORDEM_PERFIL.indexOf(b.tipoUsuario);
          if (ia !== ib) return ia - ib;
          return a.nome.localeCompare(b.nome);
        });
        setUsuarios(ordenados);
      })
      .catch(() => setUsuarios([]));
  }, []);

  useEffect(() => {
    const token = loadStoredToken();
    if (token) {
      api
        .get<Usuario>("/auth/me")
        .then((u) => {
          setUsuarioState(u);
          setUsuarioId(u.id);
        })
        .catch(() => {
          setAuthToken(null);
          setUsuarioId(null);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
    carregarUsuariosDev();
  }, [carregarUsuariosDev]);

  const login = useCallback(
    async (email: string, senha: string) => {
      const res = await api.post<AuthResponse>("/auth/login", { email, senha });
      setAuthToken(res.token);
      setUsuarioState(res.usuario);
      setUsuarioId(res.usuario.id);
      router.push(ROTAS_POR_PERFIL[res.usuario.tipoUsuario]);
    },
    [router]
  );

  const logout = useCallback(async () => {
    if (getAuthToken()) {
      try {
        await api.post("/auth/logout");
      } catch {
        /* noop */
      }
    }
    setAuthToken(null);
    setUsuarioId(null);
    setUsuarioState(null);
    router.push("/login");
  }, [router]);

  const setUsuario = useCallback(
    (u: Usuario) => {
      if (process.env.NODE_ENV !== "development") return;
      setAuthToken(null);
      setUsuarioState(u);
      setUsuarioId(u.id);
      router.push(ROTAS_POR_PERFIL[u.tipoUsuario]);
    },
    [router]
  );

  return (
    <AuthContext.Provider value={{ usuario, usuarios, login, logout, setUsuario, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
