import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "@/components/providers";
import { AppShell } from "@/components/layout/app-shell";
import { RouteGuard } from "@/components/auth/route-guard";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
});

export const metadata: Metadata = {
  title: "BPC Remo - Gestão Escola",
  description: "Protótipo gestão escola de remo e canoa havaiana",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR" className={inter.variable}>
      <body>
        <Providers>
          <AppShell>
            <RouteGuard>{children}</RouteGuard>
          </AppShell>
        </Providers>
      </body>
    </html>
  );
}
