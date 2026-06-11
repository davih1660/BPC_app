"use client";

import { useAuth } from "@/contexts/auth-context";
import {
  funcionalidadesDoPerfil,
  menuDoPerfil,
  PERFIL_LABEL,
  PERFIL_SUBTITULO,
  rotaPermitida,
  ROTAS_POR_PERFIL,
  type Funcionalidades,
  type ItemMenu,
} from "@/lib/permissoes";
import type { TipoUsuario } from "@/lib/types";

export function usePermissoes() {
  const { usuario, loading } = useAuth();
  const perfil: TipoUsuario = usuario?.tipoUsuario ?? "ADMIN";
  const funcionalidades: Funcionalidades = funcionalidadesDoPerfil(perfil);
  const menu: ItemMenu[] = menuDoPerfil(perfil);

  return {
    usuario,
    loading,
    perfil,
    perfilLabel: PERFIL_LABEL[perfil],
    perfilSubtitulo: PERFIL_SUBTITULO[perfil],
    home: ROTAS_POR_PERFIL[perfil],
    menu,
    funcionalidades,
    podeAcessar: (path: string) => rotaPermitida(perfil, path),
  };
}
