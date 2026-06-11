"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Waves } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";

export default function LoginPage() {
  const { login, loading: authLoading, usuario } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (!authLoading && usuario) {
      router.replace("/");
    }
  }, [authLoading, usuario, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    try {
      await login(email.trim(), senha);
      toast.success("Login realizado!");
    } catch (err) {
      toast.error((err as ApiError).message || "Falha no login");
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-2">
          <div className="flex justify-center">
            <Waves className="h-10 w-10 text-primary" />
          </div>
          <CardTitle className="text-xl">BPC Remo</CardTitle>
          <p className="text-sm text-muted">Entre com seu e-mail e senha</p>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="email" className="text-sm font-medium text-foreground">
                E-mail
              </label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="aluno1@bpc.com"
                required
                className="mt-1"
              />
            </div>
            <div>
              <label htmlFor="senha" className="text-sm font-medium text-foreground">
                Senha
              </label>
              <Input
                id="senha"
                type="password"
                autoComplete="current-password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="••••••"
                required
                className="mt-1"
              />
            </div>
            <Button type="submit" className="w-full" disabled={enviando || authLoading}>
              {enviando ? "Entrando..." : "Entrar"}
            </Button>
          </form>
          {(process.env.NODE_ENV === "development" ||
            process.env.NEXT_PUBLIC_SHOW_DEMO_HINT === "true") && (
            <p className="text-xs text-muted text-center mt-4">
              Protótipo: senha padrão <strong>123456</strong> para todos os usuários seed.
              <br />
              Professores: <strong>marina@bpc.com</strong> ou <strong>ricardo@bpc.com</strong>
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
