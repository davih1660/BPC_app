"use client";

import { createContext, useContext, useEffect, useState, useCallback } from "react";
import type { Usuario } from "@/lib/types";
import { api, setUsuarioId } from "@/lib/api";

interface AuthContextType {
  usuario: Usuario | null;
  usuarios: Usuario[];
  setUsuario: (u: Usuario) => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [usuario, setUsuarioState] = useState<Usuario | null>(null);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<{ content: Usuario[] }>("/usuarios?size=100")
      .then((data) => {
        const loginUsuarios = data.content.filter((u) => u.tipoUsuario !== "ALUNO");
        setUsuarios(loginUsuarios);
        const saved = localStorage.getItem("mockUsuarioId");
        const found = saved ? loginUsuarios.find((u) => u.id === Number(saved)) : null;
        const initial =
          found ??
          loginUsuarios.find((u) => u.tipoUsuario === "ADMIN") ??
          loginUsuarios[0];
        if (initial) {
          setUsuarioState(initial);
          setUsuarioId(initial.id);
        }
      })
      .catch(() => {
        setUsuarios([]);
      })
      .finally(() => setLoading(false));
  }, []);

  const setUsuario = useCallback((u: Usuario) => {
    setUsuarioState(u);
    setUsuarioId(u.id);
    localStorage.setItem("mockUsuarioId", String(u.id));
  }, []);

  return (
    <AuthContext.Provider value={{ usuario, usuarios, setUsuario, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
