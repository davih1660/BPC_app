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
        setUsuarios(data.content);
        const saved = localStorage.getItem("mockUsuarioId");
        const found = saved ? data.content.find((u) => u.id === Number(saved)) : null;
        const initial = found ?? data.content.find((u) => u.tipoUsuario === "ADMIN") ?? data.content[0];
        if (initial) {
          setUsuarioState(initial);
          setUsuarioId(initial.id);
        }
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
