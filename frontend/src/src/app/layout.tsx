import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "@/components/providers";
import { Sidebar } from "@/components/layout/sidebar";
import { UserSwitcher } from "@/components/layout/user-switcher";

export const metadata: Metadata = {
  title: "BPC Remo - Gestão Escola",
  description: "Protótipo gestão escola de remo e canoa havaiana",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>
        <Providers>
          <div className="flex min-h-screen">
            <Sidebar />
            <div className="flex-1 flex flex-col min-w-0">
              <header className="sticky top-0 z-40 flex h-14 items-center justify-between border-b border-slate-200 bg-white/90 backdrop-blur px-4 md:px-6">
                <p className="text-sm font-medium text-slate-600 md:hidden">BPC Remo</p>
                <UserSwitcher />
              </header>
              <main className="flex-1 p-4 md:p-6 overflow-auto">{children}</main>
            </div>
          </div>
        </Providers>
      </body>
    </html>
  );
}
