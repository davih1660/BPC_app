"use client";

import { useEffect, useRef, useState } from "react";
import { api, ApiError, imagemOcorrenciaUrl } from "@/lib/api";
import type { Ocorrencia, Embarcacao, StatusOcorrencia, GravidadeOcorrencia } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { statusOcorrenciaClass, gravidadeClass } from "@/lib/labels";
import { ImagePlus, Plus, X } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/auth-context";

const DESCRICAO_MAX = 10_000;
const MAX_IMAGENS = 10;

async function enviarImagens(ocorrenciaId: number, arquivos: File[]) {
  if (arquivos.length === 0) return;
  const formData = new FormData();
  arquivos.forEach((file) => formData.append("arquivos", file));
  await api.upload(`/ocorrencias/${ocorrenciaId}/imagens`, formData);
}

function GaleriaImagens({
  ocorrenciaId,
  imagens,
  onUploaded,
}: {
  ocorrenciaId: number;
  imagens: Ocorrencia["imagens"];
  onUploaded: () => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [enviando, setEnviando] = useState(false);

  const adicionar = async (files: FileList | null) => {
    if (!files?.length) return;
    const selecionados = Array.from(files);
    if (imagens.length + selecionados.length > MAX_IMAGENS) {
      toast.error(`Máximo de ${MAX_IMAGENS} imagens por ocorrência`);
      return;
    }
    setEnviando(true);
    try {
      await enviarImagens(ocorrenciaId, selecionados);
      toast.success("Imagens adicionadas");
      onUploaded();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setEnviando(false);
      if (inputRef.current) inputRef.current.value = "";
    }
  };

  return (
    <div className="mt-3 space-y-2">
      {imagens.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {imagens.map((img) => (
            <a
              key={img.id}
              href={imagemOcorrenciaUrl(img.id)}
              target="_blank"
              rel="noopener noreferrer"
              className="block h-16 w-16 rounded-md overflow-hidden border border-slate-200 hover:ring-2 hover:ring-sky-400"
              title={img.nomeOriginal}
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={imagemOcorrenciaUrl(img.id)}
                alt={img.nomeOriginal}
                className="h-full w-full object-cover"
              />
            </a>
          ))}
        </div>
      )}
      {imagens.length < MAX_IMAGENS && (
        <>
          <input
            ref={inputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            multiple
            className="hidden"
            onChange={(e) => adicionar(e.target.files)}
          />
          <Button
            type="button"
            size="sm"
            variant="outline"
            disabled={enviando}
            onClick={() => inputRef.current?.click()}
          >
            <ImagePlus className="h-4 w-4" />
            {enviando ? "Enviando..." : "Adicionar fotos"}
          </Button>
        </>
      )}
    </div>
  );
}

export default function OcorrenciasPage() {
  const { usuario } = useAuth();
  const [lista, setLista] = useState<Ocorrencia[]>([]);
  const [embarcacoes, setEmbarcacoes] = useState<Embarcacao[]>([]);
  const [filtro, setFiltro] = useState<StatusOcorrencia | "">("");
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [imagensNovas, setImagensNovas] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [form, setForm] = useState({
    embarcacaoId: "",
    titulo: "",
    descricao: "",
    gravidade: "MEDIA" as GravidadeOcorrencia,
  });

  const load = () => {
    setLoading(true);
    const url = filtro ? `/ocorrencias?status=${filtro}` : "/ocorrencias";
    Promise.all([
      api.get<Ocorrencia[]>(url),
      api.get<{ content: Embarcacao[] }>("/embarcacoes?size=100"),
    ])
      .then(([oc, emb]) => {
        setLista(oc);
        setEmbarcacoes(emb.content);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [filtro]);

  useEffect(() => {
    const urls = imagensNovas.map((file) => URL.createObjectURL(file));
    setPreviews(urls);
    return () => urls.forEach((url) => URL.revokeObjectURL(url));
  }, [imagensNovas]);

  const resetForm = () => {
    setForm({ embarcacaoId: "", titulo: "", descricao: "", gravidade: "MEDIA" });
    setImagensNovas([]);
  };

  const selecionarImagens = (files: FileList | null) => {
    if (!files?.length) return;
    const novos = Array.from(files);
    if (imagensNovas.length + novos.length > MAX_IMAGENS) {
      toast.error(`Máximo de ${MAX_IMAGENS} imagens por ocorrência`);
      return;
    }
    setImagensNovas((prev) => [...prev, ...novos]);
  };

  const removerImagem = (index: number) => {
    setImagensNovas((prev) => prev.filter((_, i) => i !== index));
  };

  const criar = async () => {
    if (!usuario) {
      toast.error("Selecione um usuário no header");
      return;
    }
    if (!form.embarcacaoId || !form.titulo.trim()) {
      toast.error("Preencha embarcação e título");
      return;
    }
    if (form.descricao.length > DESCRICAO_MAX) {
      toast.error(`Descrição deve ter no máximo ${DESCRICAO_MAX} caracteres`);
      return;
    }
    setSalvando(true);
    try {
      const criada = await api.post<Ocorrencia>("/ocorrencias", {
        embarcacaoId: Number(form.embarcacaoId),
        titulo: form.titulo,
        descricao: form.descricao,
        gravidade: form.gravidade,
        usuarioId: usuario.id,
      });
      await enviarImagens(criada.id, imagensNovas);
      toast.success("Ocorrência registrada");
      setDialogOpen(false);
      resetForm();
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setSalvando(false);
    }
  };

  const atualizarStatus = async (id: number, status: StatusOcorrencia) => {
    try {
      await api.patch(`/ocorrencias/${id}/status`, { status });
      toast.success("Status atualizado");
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap justify-between gap-4">
        <h1 className="text-2xl font-bold">Ocorrências</h1>
        <Button onClick={() => setDialogOpen(true)}><Plus className="h-4 w-4" /> Abrir ocorrência</Button>
      </div>

      <Select value={filtro} onChange={(e) => setFiltro(e.target.value as StatusOcorrencia | "")}>
        <option value="">Todos</option>
        <option value="ABERTA">Abertas</option>
        <option value="EM_ANALISE">Em análise</option>
        <option value="RESOLVIDA">Resolvidas</option>
      </Select>

      {loading ? <Loading /> : (
        <div className="space-y-3">
          {lista.map((o) => (
            <Card key={o.id}>
              <CardContent className="pt-4">
                <div className="flex flex-wrap justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold">{o.titulo}</p>
                    <p className="text-sm text-slate-500">{o.embarcacaoNome} · {o.usuarioNome}</p>
                    {o.descricao && <p className="text-sm mt-1">{o.descricao}</p>}
                    <GaleriaImagens ocorrenciaId={o.id} imagens={o.imagens ?? []} onUploaded={load} />
                  </div>
                  <div className="flex flex-col gap-2 items-end">
                    <Badge className={gravidadeClass[o.gravidade]}>{o.gravidade}</Badge>
                    <Badge className={statusOcorrenciaClass[o.status]}>{o.status}</Badge>
                    {o.status === "ABERTA" && (
                      <Button size="sm" variant="secondary" onClick={() => atualizarStatus(o.id, "EM_ANALISE")}>
                        Em análise
                      </Button>
                    )}
                    {o.status !== "RESOLVIDA" && (
                      <Button size="sm" onClick={() => atualizarStatus(o.id, "RESOLVIDA")}>
                        Resolver
                      </Button>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={(open) => {
        setDialogOpen(open);
        if (!open) resetForm();
      }}>
        <DialogContent title="Nova ocorrência">
          <div className="space-y-3">
            <Select value={form.embarcacaoId} onChange={(e) => setForm({ ...form, embarcacaoId: e.target.value })}>
              <option value="">Embarcação...</option>
              {embarcacoes.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
            </Select>
            <Input placeholder="Título" value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} />
            <div className="space-y-1">
              <Textarea
                placeholder="Descrição"
                value={form.descricao}
                maxLength={DESCRICAO_MAX}
                onChange={(e) => setForm({ ...form, descricao: e.target.value })}
              />
              <p className="text-xs text-slate-400 text-right">
                {form.descricao.length}/{DESCRICAO_MAX}
              </p>
            </div>
            <Select value={form.gravidade} onChange={(e) => setForm({ ...form, gravidade: e.target.value as GravidadeOcorrencia })}>
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Média</option>
              <option value="ALTA">Alta</option>
            </Select>
            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-700">Fotos (opcional)</label>
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                multiple
                className="block w-full text-sm text-slate-500 file:mr-3 file:rounded-md file:border-0 file:bg-sky-50 file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-sky-700 hover:file:bg-sky-100"
                onChange={(e) => selecionarImagens(e.target.files)}
              />
              <p className="text-xs text-slate-400">JPEG, PNG, WebP ou GIF · até 5 MB cada · máx. {MAX_IMAGENS}</p>
              {previews.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {previews.map((url, i) => (
                    <div key={url} className="relative h-16 w-16 rounded-md overflow-hidden border border-slate-200">
                      {/* eslint-disable-next-line @next/next/no-img-element */}
                      <img src={url} alt="" className="h-full w-full object-cover" />
                      <button
                        type="button"
                        onClick={() => removerImagem(i)}
                        className="absolute top-0.5 right-0.5 rounded-full bg-black/60 p-0.5 text-white hover:bg-black/80"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <Button className="w-full" onClick={criar} disabled={salvando}>
              {salvando ? "Registrando..." : "Registrar"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
