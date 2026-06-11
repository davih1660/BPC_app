import type { TipoUsuario } from "./types";
import type { LucideIcon } from "lucide-react";
import {
  LayoutDashboard,
  Calendar,
  Users,
  Ship,
  GraduationCap,
  Bookmark,
  AlertTriangle,
  CreditCard,
  Wrench,
  UserCircle,
  Bell,
  BarChart3,
  CloudRain,
  Receipt,
} from "lucide-react";

export type ItemMenu = {
  href: string;
  label: string;
  icon: LucideIcon;
  perfis: TipoUsuario[];
};

export const ITENS_MENU: ItemMenu[] = [
  { href: "/", label: "Dashboard", icon: LayoutDashboard, perfis: ["ADMIN", "PROFESSOR"] },
  { href: "/agenda", label: "Agenda", icon: Calendar, perfis: ["ADMIN", "PROFESSOR", "ALUNO"] },
  { href: "/reservas", label: "Reservas", icon: Bookmark, perfis: ["ADMIN", "PROFESSOR"] },
  { href: "/uso-livre", label: "Uso livre", icon: Ship, perfis: ["ADMIN", "PROFESSOR"] },
  { href: "/minhas-reservas", label: "Minhas reservas", icon: UserCircle, perfis: ["ALUNO"] },
  { href: "/alunos", label: "Alunos", icon: Users, perfis: ["ADMIN"] },
  { href: "/planos", label: "Planos", icon: CreditCard, perfis: ["ADMIN"] },
  { href: "/embarcacoes", label: "Embarcações", icon: Ship, perfis: ["ADMIN", "MANUTENCAO"] },
  { href: "/aulas", label: "Horários coletivos", icon: GraduationCap, perfis: ["ADMIN"] },
  { href: "/ocorrencias", label: "Ocorrências", icon: AlertTriangle, perfis: ["ADMIN", "PROFESSOR", "ALUNO", "MANUTENCAO"] },
  { href: "/tipos-ocorrencia", label: "Tipos de ocorrência", icon: AlertTriangle, perfis: ["ADMIN"] },
  { href: "/integracao/wellhub", label: "Wellhub", icon: Wrench, perfis: ["ADMIN"] },
  { href: "/cobrancas", label: "Cobranças", icon: Receipt, perfis: ["ADMIN"] },
  { href: "/bloqueios", label: "Bloqueios", icon: CloudRain, perfis: ["ADMIN"] },
  { href: "/relatorios", label: "Relatórios", icon: BarChart3, perfis: ["ADMIN"] },
  { href: "/notificacoes", label: "Notificações", icon: Bell, perfis: ["ADMIN", "PROFESSOR", "ALUNO", "MANUTENCAO"] },
];

export const ROTAS_POR_PERFIL: Record<TipoUsuario, string> = {
  ADMIN: "/",
  PROFESSOR: "/reservas",
  ALUNO: "/minhas-reservas",
  MANUTENCAO: "/embarcacoes",
};

export const PERFIL_LABEL: Record<TipoUsuario, string> = {
  ADMIN: "Administrador",
  PROFESSOR: "Professor",
  ALUNO: "Aluno",
  MANUTENCAO: "Manutenção",
};

export const PERFIL_SUBTITULO: Record<TipoUsuario, string> = {
  ADMIN: "Gestão completa da operação",
  PROFESSOR: "Reservas e agenda do dia",
  ALUNO: "Agenda, reservas, embarcações e ocorrências",
  MANUTENCAO: "Embarcações e ocorrências",
};

/** Rotas permitidas por perfil (prefix match). Admin tem acesso total. */
const ROTAS_PERMITIDAS: Record<Exclude<TipoUsuario, "ADMIN">, string[]> = {
  PROFESSOR: ["/", "/agenda", "/reservas", "/uso-livre", "/ocorrencias", "/notificacoes"],
  ALUNO: ["/agenda", "/minhas-reservas", "/ocorrencias", "/notificacoes"],
  MANUTENCAO: ["/embarcacoes", "/ocorrencias", "/notificacoes"],
};

export function menuDoPerfil(perfil: TipoUsuario): ItemMenu[] {
  return ITENS_MENU.filter((item) => item.perfis.includes(perfil));
}

export function rotaPermitida(perfil: TipoUsuario, path: string): boolean {
  if (perfil === "ADMIN") return true;
  const rotas = ROTAS_PERMITIDAS[perfil];
  return rotas.some((rota) => path === rota || (rota !== "/" && path.startsWith(rota)));
}

export type Funcionalidades = {
  reservarColetivaManual: boolean;
  reservarEmbarcacaoStaff: boolean;
  cancelarReservaColetiva: boolean;
  checkInPresenca: boolean;
  escalarProfessores: boolean;
  configurarHorarios: boolean;
  gerenciarAlunos: boolean;
  gerenciarPlanos: boolean;
  integracaoWellhub: boolean;
  interditarEmbarcacao: boolean;
  abrirOcorrencia: boolean;
  resolverOcorrencia: boolean;
  gerenciarTiposOcorrencia: boolean;
  reservarColetivaApp: boolean;
  reservarEmbarcacaoAluno: boolean;
};

const TODAS_FUNCIONALIDADES: Funcionalidades = {
  reservarColetivaManual: true,
  reservarEmbarcacaoStaff: true,
  cancelarReservaColetiva: true,
  checkInPresenca: true,
  escalarProfessores: true,
  configurarHorarios: true,
  gerenciarAlunos: true,
  gerenciarPlanos: true,
  integracaoWellhub: true,
  interditarEmbarcacao: true,
  abrirOcorrencia: true,
  resolverOcorrencia: true,
  gerenciarTiposOcorrencia: true,
  reservarColetivaApp: false,
  reservarEmbarcacaoAluno: false,
};

export function funcionalidadesDoPerfil(perfil: TipoUsuario): Funcionalidades {
  switch (perfil) {
    case "ADMIN":
      return { ...TODAS_FUNCIONALIDADES };
    case "PROFESSOR":
      return {
        ...TODAS_FUNCIONALIDADES,
        reservarEmbarcacaoStaff: false,
        escalarProfessores: false,
        configurarHorarios: false,
        gerenciarAlunos: false,
        gerenciarPlanos: false,
        integracaoWellhub: false,
        interditarEmbarcacao: false,
        resolverOcorrencia: false,
        gerenciarTiposOcorrencia: false,
        reservarColetivaApp: false,
        reservarEmbarcacaoAluno: false,
      };
    case "ALUNO":
      return {
        ...TODAS_FUNCIONALIDADES,
        reservarColetivaManual: false,
        reservarEmbarcacaoStaff: false,
        cancelarReservaColetiva: true,
        checkInPresenca: false,
        escalarProfessores: false,
        configurarHorarios: false,
        gerenciarAlunos: false,
        gerenciarPlanos: false,
        integracaoWellhub: false,
        interditarEmbarcacao: false,
        abrirOcorrencia: true,
        resolverOcorrencia: false,
        gerenciarTiposOcorrencia: false,
        reservarColetivaApp: true,
        reservarEmbarcacaoAluno: true,
      };
    case "MANUTENCAO":
      return {
        ...TODAS_FUNCIONALIDADES,
        reservarColetivaManual: false,
        reservarEmbarcacaoStaff: false,
        cancelarReservaColetiva: false,
        checkInPresenca: false,
        escalarProfessores: false,
        configurarHorarios: false,
        gerenciarAlunos: false,
        gerenciarPlanos: false,
        integracaoWellhub: false,
        interditarEmbarcacao: false,
        abrirOcorrencia: true,
        resolverOcorrencia: false,
        gerenciarTiposOcorrencia: false,
        reservarColetivaApp: false,
        reservarEmbarcacaoAluno: false,
      };
  }
}
