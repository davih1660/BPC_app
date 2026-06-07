"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Calendar,
  Users,
  Ship,
  GraduationCap,
  Bookmark,
  AlertTriangle,
  Waves,
} from "lucide-react";
import { cn } from "@/lib/utils";

const links = [
  { href: "/", label: "Dashboard", icon: LayoutDashboard },
  { href: "/agenda", label: "Agenda", icon: Calendar },
  { href: "/alunos", label: "Alunos", icon: Users },
  { href: "/embarcacoes", label: "Embarcações", icon: Ship },
  { href: "/aulas", label: "Aulas", icon: GraduationCap },
  { href: "/reservas", label: "Reservas", icon: Bookmark },
  { href: "/ocorrencias", label: "Ocorrências", icon: AlertTriangle },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="hidden md:flex w-64 flex-col border-r border-slate-200 bg-slate-950 text-white min-h-screen">
      <div className="flex items-center gap-2 px-6 py-5 border-b border-slate-800">
        <Waves className="h-8 w-8 text-sky-400" />
        <div>
          <p className="font-bold text-lg leading-tight">BPC Remo</p>
          <p className="text-xs text-slate-400">Gestão operacional</p>
        </div>
      </div>
      <nav className="flex-1 p-4 space-y-1">
        {links.map(({ href, label, icon: Icon }) => (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
              pathname === href
                ? "bg-sky-600 text-white"
                : "text-slate-300 hover:bg-slate-800 hover:text-white"
            )}
          >
            <Icon className="h-4 w-4" />
            {label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
