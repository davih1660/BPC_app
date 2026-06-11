"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { api, ApiError, imagemOcorrenciaUrl } from "@/lib/api";
import type { Ocorrencia, Embarcacao, OcorrenciaImagem, StatusOcorrencia, TipoOcorrencia } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Select } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import { FileInput } from "@/components/ui/file-input";
import { statusOcorrenciaVariant, statusOcorrenciaLabel, gravidadeVariant, gravidadeLabel } from "@/lib/labels";
import { PageHeader } from "@/components/layout/page-header";
import { EmptyState } from "@/components/ui/empty-state";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";
import { ChevronLeft, ChevronRight, ImagePlus, Plus, Settings, X } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/auth-context";
import { usePermissoes } from "@/hooks/use-permissoes";

const DESCRICAO_MAX = 10_000;
const MAX_IMAGENS = 10;

async function enviarImagens(ocorrenciaId: number, arquivos: File[]) {
  if (arquivos.length === 0) return;
  const formData = new FormData();
  arquivos.forEach((file) => formData.append("arquivos", file));
  await api.upload(`/ocorrencias/${ocorrenciaId}/imagens`, formData);
}

function VisualizadorImagem({
  imagens,
  indice,
  onFechar,
  onAnterior,
  onProxima,
}: {
  imagens: OcorrenciaImagem[];
  indice: number;
  onFechar: () => void;
  onAnterior: () => void;
  onProxima: () => void;
}) {
  const img = imagens[indice];
  const temAnterior = indice > 0;
  const temProxima = indice < imagens.length - 1;

  return (
    <Dialog open onOpenChange={(open) => !open && onFechar()}>
      <DialogContent
        hideTitle
        className="w-auto max-w-[min(92vw,56rem)] max-h-[90vh] overflow-hidden p-3 pt-12"
      >
        <div className="relative flex items-center justify-center rounded-xl bg-surface-variant/50">
          {temAnterior && (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="absolute left-2 z-10 h-10 w-10 rounded-full bg-surface/90 shadow-sm"
              onClick={onAnterior}
              aria-label="Imagem anterior"
            >
              <ChevronLeft className="h-5 w-5" />
            </Button>
          )}
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={imagemOcorrenciaUrl(img.id)}
            alt=""
            className="max-h-[calc(90vh-6rem)] max-w-[min(88vw,52rem)] w-auto object-contain rounded-lg"
          />
          {temProxima && (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="absolute right-2 z-10 h-10 w-10 rounded-full bg-surface/90 shadow-sm"
              onClick={onProxima}
              aria-label="Próxima imagem"
            >
              <ChevronRight className="h-5 w-5" />
            </Button>
          )}
        </div>
        {imagens.length > 1 && (
          <p className="text-center text-xs text-muted mt-2 shrink-0">
            {indice + 1} de {imagens.length}
          </p>
        )}
      </DialogContent>
    </Dialog>
  );
}

function GaleriaImagens({
  ocorrenciaId,
  imagens,
  onUploaded,
  podeAdicionar = true,
}: {
  ocorrenciaId: number;
  imagens: Ocorrencia["imagens"];
  onUploaded: () => void;
  podeAdicionar?: boolean;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [enviando, setEnviando] = useState(false);
  const [indiceExpandido, setIndiceExpandido] = useState<number | null>(null);

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
      {indiceExpandido !== null && (
        <VisualizadorImagem
          imagens={imagens}
          indice={indiceExpandido}
          onFechar={() => setIndiceExpandido(null)}
          onAnterior={() => setIndiceExpandido((i) => Math.max(0, (i ?? 0) - 1))}
          onProxima={() =>
            setIndiceExpandido((i) => Math.min(imagens.length - 1, (i ?? 0) + 1))
          }
        />
      )}
      {imagens.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {imagens.map((img, i) => (
            <button
              key={img.id}
              type="button"
              onClick={() => setIndiceExpandido(i)}
              className="block h-16 w-16 rounded-md overflow-hidden border border-outline hover:ring-2 hover:ring-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary cursor-zoom-in"
              title="Ampliar imagem"
            >
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={imagemOcorrenciaUrl(img.id)}
                alt=""
                className="h-full w-full object-cover"
              />
            </button>
          ))}
        </div>
      )}
      {podeAdicionar && imagens.length < MAX_IMAGENS && (
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
  const { perfil, funcionalidades, perfilLabel } = usePermissoes();
  const modoAluno = perfil === "ALUNO";
  const [lista, setLista] = useState<Ocorrencia[]>([]);
  const [embarcacoes, setEmbarcacoes] = useState<Embarcacao[]>([]);
  const [tipos, setTipos] = useState<TipoOcorrencia[]>([]);
  const [filtro, setFiltro] = useState<StatusOcorrencia | "">("");
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [imagensNovas, setImagensNovas] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [form, setForm] = useState({
    embarcacaoId: "",
    tipoOcorrenciaId: "",
    descricao: "",
  });

  const tipoSelecionado = tipos.find((t) => String(t.id) === form.tipoOcorrenciaId);

  const load = () => {
    setLoading(true);
    const url = filtro ? `/ocorrencias?status=${filtro}` : "/ocorrencias";
    Promise.all([
      api.get<Ocorrencia[]>(url),
      api.get<{ content: Embarcacao[] }>("/embarcacoes?size=100"),
      api.get<TipoOcorrencia[]>("/tipos-ocorrencia?somenteAtivos=true"),
    ])
      .then(([oc, emb, tp]) => {
        setLista(oc);
        const listaEmb = emb.content;
        setEmbarcacoes(
          modoAluno
            ? listaEmb.filter((e) => ["OC1", "OC2", "OC3", "OC4", "OC6"].includes(e.tipo))
            : listaEmb
        );
        setTipos(tp);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [filtro, modoAluno]);

  useEffect(() => {
    const urls = imagensNovas.map((file) => URL.createObjectURL(file));
    setPreviews(urls);
    return () => urls.forEach((url) => URL.revokeObjectURL(url));
  }, [imagensNovas]);

  const resetForm = () => {
    setForm({ embarcacaoId: "", tipoOcorrenciaId: "", descricao: "" });
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
    if (!form.embarcacaoId || !form.tipoOcorrenciaId) {
      toast.error("Preencha embarcação e tipo da ocorrência");
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
        tipoOcorrenciaId: Number(form.tipoOcorrenciaId),
        descricao: form.descricao,
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

  const listaExibida = modoAluno && usuario
    ? lista.filter((o) => o.usuarioId === usuario.id)
    : lista;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Ocorrências"
        description={
          modoAluno
            ? "Registre problemas em embarcações usadas em remadas avulsas ou reservas OC."
            : `Visão ${perfilLabel.toLowerCase()}`
        }
        actions={
          <>
            {funcionalidades.gerenciarTiposOcorrencia && (
              <Button variant="outline" asChild>
                <Link href="/tipos-ocorrencia">
                  <Settings className="h-4 w-4" />
                  Tipos
                </Link>
              </Button>
            )}
            {funcionalidades.abrirOcorrencia && (
              <Button onClick={() => setDialogOpen(true)}><Plus className="h-4 w-4" /> Abrir ocorrência</Button>
            )}
          </>
        }
      />

      <Select value={filtro} onChange={(e) => setFiltro(e.target.value as StatusOcorrencia | "")}>
        <option value="">Todos</option>
        <option value="ABERTA">Abertas</option>
        <option value="EM_ANALISE">Em análise</option>
        <option value="RESOLVIDA">Resolvidas</option>
      </Select>

      {loading ? <Loading /> : (
        <div className="space-y-3">
          {listaExibida.length === 0 && (
            <EmptyState
              title={modoAluno ? "Nenhuma ocorrência sua" : "Nenhuma ocorrência encontrada"}
              description={modoAluno ? "Use o botão acima para reportar um problema." : undefined}
            />
          )}
          {listaExibida.map((o) => (
            <Card key={o.id}>
              <CardContent className="pt-4">
                <div className="flex flex-wrap justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold">{o.titulo}</p>
                    <p className="text-sm text-slate-500">
                      {o.embarcacaoNome}
                      {!modoAluno && ` · ${o.usuarioNome}`}
                    </p>
                    {o.descricao && <p className="text-sm mt-1">{o.descricao}</p>}
                    <GaleriaImagens
                      ocorrenciaId={o.id}
                      imagens={o.imagens ?? []}
                      onUploaded={load}
                      podeAdicionar={!modoAluno || o.usuarioId === usuario?.id}
                    />
                  </div>
                  <div className="flex flex-col gap-2 items-end">
                    <Badge variant={gravidadeVariant[o.gravidade]}>{gravidadeLabel[o.gravidade]}</Badge>
                    <Badge variant={statusOcorrenciaVariant[o.status]}>{statusOcorrenciaLabel[o.status]}</Badge>
                    {funcionalidades.resolverOcorrencia && o.status === "ABERTA" && (
                      <Button size="sm" variant="secondary" onClick={() => atualizarStatus(o.id, "EM_ANALISE")}>
                        Em análise
                      </Button>
                    )}
                    {funcionalidades.resolverOcorrencia && o.status !== "RESOLVIDA" && (
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
        <DialogContent title={modoAluno ? "Reportar problema na embarcação" : "Nova ocorrência"}>
          <div className="space-y-3">
            <Select value={form.embarcacaoId} onChange={(e) => setForm({ ...form, embarcacaoId: e.target.value })}>
              <option value="">Embarcação...</option>
              {embarcacoes.map((e) => <option key={e.id} value={e.id}>{e.nome}</option>)}
            </Select>
            {modoAluno ? (
              <div className="space-y-2">
                <Label>Tipo da ocorrência</Label>
                <div className="space-y-1.5 max-h-48 overflow-y-auto">
                  {tipos.map((t) => (
                    <button
                      key={t.id}
                      type="button"
                      onClick={() => setForm({ ...form, tipoOcorrenciaId: String(t.id) })}
                      className={cn(
                        "w-full flex items-center justify-between rounded-xl border px-3 py-2.5 text-left text-sm transition-colors",
                        form.tipoOcorrenciaId === String(t.id)
                          ? "border-primary bg-primary-container/50"
                          : "border-outline hover:bg-surface-variant"
                      )}
                    >
                      <span className="font-medium">{t.nome}</span>
                      <Badge variant={gravidadeVariant[t.gravidade]}>{gravidadeLabel[t.gravidade]}</Badge>
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <Select
                value={form.tipoOcorrenciaId}
                onChange={(e) => setForm({ ...form, tipoOcorrenciaId: e.target.value })}
              >
                <option value="">Tipo da ocorrência...</option>
                {tipos.map((t) => (
                  <option key={t.id} value={t.id}>{t.nome}</option>
                ))}
              </Select>
            )}
            {tipoSelecionado && !modoAluno && (
              <div className="flex items-center gap-2 text-sm">
                <span className="text-muted">Prioridade:</span>
                <Badge variant={gravidadeVariant[tipoSelecionado.gravidade]}>
                  {gravidadeLabel[tipoSelecionado.gravidade]}
                </Badge>
              </div>
            )}
            <div className="space-y-1">
              <Textarea
                placeholder="Descrição (opcional)"
                value={form.descricao}
                maxLength={DESCRICAO_MAX}
                onChange={(e) => setForm({ ...form, descricao: e.target.value })}
              />
              <p className="text-xs text-slate-400 text-right">
                {form.descricao.length}/{DESCRICAO_MAX}
              </p>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-700">Fotos (opcional)</label>
              <FileInput
                accept="image/jpeg,image/png,image/webp,image/gif"
                multiple
                files={imagensNovas}
                onChange={selecionarImagens}
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
