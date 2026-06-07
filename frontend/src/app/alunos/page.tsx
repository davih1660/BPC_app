"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Usuario, PageResponse, Plano, AlunoPlano } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { Plus, Pencil } from "lucide-react";
import { toast } from "sonner";
import { hojeSaoPaulo } from "@/lib/relogio";

export default function AlunosPage() {
  const [alunos, setAlunos] = useState<PageResponse<Usuario> | null>(null);
  const [planos, setPlanos] = useState<Plano[]>([]);
  const [q, setQ] = useState("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [planosAluno, setPlanosAluno] = useState<AlunoPlano[]>([]);
  const [selected, setSelected] = useState<Usuario | null>(null);
  const [form, setForm] = useState({ nome: "", email: "", telefone: "", tipoUsuario: "ALUNO" as const });

  const load = () => {
    setLoading(true);
    const params = new URLSearchParams({ page: String(page), size: "10", tipo: "ALUNO" });
    if (q) params.set("q", q);
    api.get<PageResponse<Usuario>>(`/usuarios?${params}`).then(setAlunos).catch((e: ApiError) => toast.error(e.message)).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [page, q]);
  useEffect(() => { api.get<Plano[]>("/planos").then(setPlanos).catch(() => {}); }, []);

  const openEdit = (u?: Usuario) => {
    if (u) {
      setSelected(u);
      setForm({ nome: u.nome, email: u.email, telefone: u.telefone || "", tipoUsuario: "ALUNO" });
      api.get<AlunoPlano[]>(`/alunos/${u.id}/planos`).then(setPlanosAluno).catch(() => setPlanosAluno([]));
    } else {
      setSelected(null);
      setForm({ nome: "", email: "", telefone: "", tipoUsuario: "ALUNO" });
      setPlanosAluno([]);
    }
    setDialogOpen(true);
  };

  const save = async () => {
    try {
      if (selected) {
        await api.put(`/usuarios/${selected.id}`, { ...form, id: selected.id, tipoUsuario: "ALUNO" });
        toast.success("Aluno atualizado");
      } else {
        await api.post("/usuarios", { ...form, tipoUsuario: "ALUNO" });
        toast.success("Aluno cadastrado");
      }
      setDialogOpen(false);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const vincularPlano = async (planoId: number) => {
    if (!selected) return;
    try {
      await api.post(`/alunos/${selected.id}/planos`, { planoId, dataInicio: hojeSaoPaulo() });
      toast.success("Plano vinculado");
      const p = await api.get<AlunoPlano[]>(`/alunos/${selected.id}/planos`);
      setPlanosAluno(p);
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-bold">Gestão de Alunos</h1>
        <Button onClick={() => openEdit()}><Plus className="h-4 w-4" /> Novo aluno</Button>
      </div>

      <div className="flex gap-2">
        <Input placeholder="Buscar nome ou email..." value={q} onChange={(e) => { setQ(e.target.value); setPage(0); }} className="max-w-sm" />
      </div>

      {loading ? <Loading /> : (
        <Card>
          <CardContent className="p-0">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 border-b">
                <tr>
                  <th className="text-left p-3">Nome</th>
                  <th className="text-left p-3">Email</th>
                  <th className="text-left p-3">Telefone</th>
                  <th className="p-3"></th>
                </tr>
              </thead>
              <tbody>
                {alunos?.content.map((a) => (
                  <tr key={a.id} className="border-b hover:bg-slate-50">
                    <td className="p-3">{a.nome}</td>
                    <td className="p-3 text-slate-600">{a.email}</td>
                    <td className="p-3">{a.telefone}</td>
                    <td className="p-3 text-right">
                      <Button variant="ghost" size="sm" onClick={() => openEdit(a)}><Pencil className="h-4 w-4" /></Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {alunos && alunos.totalPages > 1 && (
              <div className="flex justify-center gap-2 p-4">
                <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Anterior</Button>
                <span className="text-sm self-center">Página {page + 1} de {alunos.totalPages}</span>
                <Button variant="outline" size="sm" disabled={page >= alunos.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Próxima</Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent title={selected ? "Editar aluno" : "Novo aluno"}>
          <div className="space-y-3">
            <Input placeholder="Nome" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} />
            <Input placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            <Input placeholder="Telefone" value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} />
            {selected && (
              <div className="border-t pt-3">
                <p className="text-sm font-medium mb-2">Planos</p>
                <ul className="text-sm text-slate-600 mb-2">
                  {planosAluno.map((ap) => (
                    <li key={ap.id}>{ap.plano.nome} {ap.ativo ? "(ativo)" : "(inativo)"}</li>
                  ))}
                </ul>
                <Select onChange={(e) => e.target.value && vincularPlano(Number(e.target.value))} defaultValue="">
                  <option value="">Vincular plano...</option>
                  {planos.map((p) => <option key={p.id} value={p.id}>{p.nome}</option>)}
                </Select>
              </div>
            )}
            <Button onClick={save} className="w-full">Salvar</Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
