"use client";

import { useAuth } from "@/contexts/auth-context";
import { Select } from "@/components/ui/select";

export function UserSwitcher() {
  const { usuario, usuarios, setUsuario } = useAuth();

  return (
    <div className="flex items-center gap-2">
      <span className="text-sm text-slate-500 hidden sm:inline">Usuário mock:</span>
      <Select
        className="w-56"
        value={usuario?.id?.toString() ?? ""}
        onChange={(e) => {
          const id = Number(e.target.value);
          const u = usuarios.find((x) => x.id === id);
          if (u) setUsuario(u);
        }}
      >
        <option value="">Selecione...</option>
        {usuarios.map((u) => (
          <option key={u.id} value={u.id}>
            {u.nome} ({u.tipoUsuario})
          </option>
        ))}
      </Select>
    </div>
  );
}
