"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, ApiError } from "@/lib/api";
import type { TipoOcorrencia, GravidadeOcorrencia } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { gravidadeVariant, gravidadeLabel } from "@/lib/labels";
import { PageHeader } from "@/components/layout/page-header";
import { EmptyState } from "@/components/ui/empty-state";
import { ArrowLeft, Pencil, Plus } from "lucide-react";
import { toast } from "sonner";

type FormTipo = {
  nome: string;
  gravidade: GravidadeOcorrencia;
  ordem: string;
  ativo: boolean;
};

const formVazio = (): FormTipo => ({
  nome: "",
  gravidade: "MEDIA",
  ordem: "0",
  ativo: true,
});

export default function CatalogoTiposOcorrenciaPage() {
  const [tipos, setTipos] = useState<TipoOcorrencia[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selected, setSelected] = useState<TipoOcorrencia | null>(null);
  const [form, setForm] = useState<FormTipo>(formVazio());
  const [salvando, setSalvando] = useState(false);

  const load = () => {
    setLoading(true);
    api
      .get<TipoOcorrencia[]>("/tipos-ocorrencia")
      .then(setTipos)
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const abrirNovo = () => {
    setSelected(null);
    setForm(formVazio());
    setDialogOpen(true);
  };

  const abrirEditar = (t: TipoOcorrencia) => {
    setSelected(t);
    setForm({
      nome: t.nome,
      gravidade: t.gravidade,
      ordem: String(t.ordem),
      ativo: t.ativo,
    });
    setDialogOpen(true);
  };

  const salvar = async () => {
    if (!form.nome.trim()) {
      toast.error("Informe o nome do tipo");
      return;
    }
    const payload = {
      nome: form.nome.trim(),
      gravidade: form.gravidade,
      ordem: Number(form.ordem) || 0,
      ativo: form.ativo,
    };
    setSalvando(true);
    try {
      if (selected) {
        await api.put(`/tipos-ocorrencia/${selected.id}`, payload);
        toast.success("Tipo atualizado");
      } else {
        await api.post("/tipos-ocorrencia", payload);
        toast.success("Tipo criado");
      }
      setDialogOpen(false);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className="space-y-6">
      <Link
        href="/ocorrencias"
        className="inline-flex items-center gap-1 text-sm text-muted hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Voltar para ocorrências
      </Link>
      <PageHeader
        title="Tipos de ocorrência"
        description="Catálogo padronizado com prioridade associada"
        actions={
          <Button onClick={abrirNovo}>
            <Plus className="h-4 w-4" />
            Novo tipo
          </Button>
        }
      />

      {loading ? (
        <Loading />
      ) : (
        <div className="space-y-3">
          {tipos.length === 0 && (
            <EmptyState title="Nenhum tipo cadastrado" description="Crie o primeiro tipo de ocorrência." />
          )}
          {tipos.map((t) => (
            <Card key={t.id} className={!t.ativo ? "opacity-60" : undefined}>
              <CardContent className="pt-4 flex flex-wrap justify-between gap-3 items-center">
                <div>
                  <p className="font-semibold">{t.nome}</p>
                  <p className="text-sm text-slate-500">Ordem: {t.ordem}</p>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant={gravidadeVariant[t.gravidade]}>{gravidadeLabel[t.gravidade]}</Badge>
                  {!t.ativo && <Badge variant="neutral">Inativo</Badge>}
                  <Button size="sm" variant="outline" onClick={() => abrirEditar(t)}>
                    <Pencil className="h-4 w-4" />
                    Editar
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent title={selected ? "Editar tipo" : "Novo tipo"}>
          <div className="space-y-3">
            <Input
              placeholder="Nome (ex.: Furo no casco)"
              value={form.nome}
              onChange={(e) => setForm({ ...form, nome: e.target.value })}
            />
            <Select
              value={form.gravidade}
              onChange={(e) => setForm({ ...form, gravidade: e.target.value as GravidadeOcorrencia })}
            >
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Média</option>
              <option value="ALTA">Alta</option>
            </Select>
            <Input
              type="number"
              placeholder="Ordem de exibição"
              value={form.ordem}
              onChange={(e) => setForm({ ...form, ordem: e.target.value })}
            />
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={form.ativo}
                onChange={(e) => setForm({ ...form, ativo: e.target.checked })}
              />
              Ativo no formulário de abertura
            </label>
            <Button className="w-full" onClick={salvar} disabled={salvando}>
              {salvando ? "Salvando..." : "Salvar"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
