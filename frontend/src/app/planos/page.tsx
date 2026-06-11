"use client";

import { useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { AlunoSituacao, CategoriaPlano, PeriodicidadePlano, Plano } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Loading } from "@/components/ui/loading";
import {
  categoriaPlanoLabel,
  formatMoeda,
  periodicidadePlanoLabel,
  situacaoAlunoVariant,
  situacaoAlunoLabel,
  tipoPlanoLabel,
} from "@/lib/labels";
import { Link2, Pencil, Search } from "lucide-react";
import { toast } from "sonner";
import { hojeSaoPaulo } from "@/lib/relogio";
import { PageHeader } from "@/components/layout/page-header";
import { StatTile } from "@/components/layout/stat-tile";
import { EmptyState } from "@/components/ui/empty-state";
import { Users } from "lucide-react";

const categorias: CategoriaPlano[] = ["RECORRENTE", "AVULSO", "WELLHUB", "EQUIPAMENTO"];

const ORDEM_PERIODICIDADE: PeriodicidadePlano[] = [
  "MENSAL",
  "TRIMESTRAL",
  "SEMESTRAL",
  "ANUAL",
];

type GrupoFrequencia = {
  rotulo: string;
  subtitulo?: string;
  planos: Plano[];
};

function ordenarPorPeriodicidade(planos: Plano[]) {
  return [...planos].sort(
    (a, b) =>
      ORDEM_PERIODICIDADE.indexOf(a.periodicidade!) -
      ORDEM_PERIODICIDADE.indexOf(b.periodicidade!)
  );
}

function planoExibivel(p: Plano): boolean {
  if (p.categoriaPlano === "RECORRENTE") {
    return Boolean(p.periodicidade && p.valor != null);
  }
  if (p.categoriaPlano === "AVULSO" || p.categoriaPlano === "EQUIPAMENTO") {
    return p.valor != null;
  }
  return true;
}

export default function PlanosPage() {
  const [planos, setPlanos] = useState<Plano[]>([]);
  const [situacoes, setSituacoes] = useState<AlunoSituacao[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtroAluno, setFiltroAluno] = useState("");
  const [filtroSituacao, setFiltroSituacao] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [planoEditando, setPlanoEditando] = useState<Plano | null>(null);
  const [valorEdit, setValorEdit] = useState("");
  const [validadeEdit, setValidadeEdit] = useState("");
  const [salvandoEdicao, setSalvandoEdicao] = useState(false);
  const [vinculo, setVinculo] = useState({ alunoId: "", planoId: "" });

  const load = () => {
    setLoading(true);
    Promise.all([
      api.get<Plano[]>("/planos"),
      api.get<AlunoSituacao[]>("/planos/situacoes"),
    ])
      .then(([p, s]) => {
        setPlanos(p);
        setSituacoes(s);
      })
      .catch((e: ApiError) => toast.error(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const planosPorCategoria = useMemo(() => {
    const map: Record<CategoriaPlano, Plano[]> = {
      RECORRENTE: [],
      AVULSO: [],
      WELLHUB: [],
      EQUIPAMENTO: [],
    };
    for (const p of planos) {
      if (!planoExibivel(p)) continue;
      map[p.categoriaPlano]?.push(p);
    }
    for (const cat of categorias) {
      map[cat].sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"));
    }
    return map;
  }, [planos]);

  const gruposPorFrequencia = useMemo((): GrupoFrequencia[] => {
    const recorrentes = planosPorCategoria.RECORRENTE;
    const definicoes: Omit<GrupoFrequencia, "planos">[] = [
      { rotulo: "1x semana" },
      { rotulo: "2x semana" },
      { rotulo: "3x semana" },
      {
        rotulo: "Livre",
        subtitulo: "Acesso ilimitado a todas as aulas",
      },
    ];
    return definicoes.map((def, i) => {
      const planosGrupo =
        i === 3
          ? recorrentes.filter((p) => p.ilimitado)
          : recorrentes.filter((p) => p.quantidadeAulasSemana === i + 1);
      return { ...def, planos: ordenarPorPeriodicidade(planosGrupo) };
    });
  }, [planosPorCategoria.RECORRENTE]);

  const remadasAvulsas = useMemo(
    () => planosPorCategoria.AVULSO.filter((p) => p.quantidadeRemadas === 1),
    [planosPorCategoria.AVULSO]
  );

  const pacotesRemadas = useMemo(
    () =>
      [...planosPorCategoria.AVULSO.filter((p) => (p.quantidadeRemadas ?? 0) > 1)].sort(
        (a, b) => (a.quantidadeRemadas ?? 0) - (b.quantidadeRemadas ?? 0)
      ),
    [planosPorCategoria.AVULSO]
  );

  const situacoesFiltradas = useMemo(() => {
    const q = filtroAluno.trim().toLowerCase();
    return situacoes.filter((s) => {
      if (filtroSituacao && s.situacao !== filtroSituacao) return false;
      if (!q) return true;
      return (
        s.alunoNome.toLowerCase().includes(q) ||
        s.alunoEmail.toLowerCase().includes(q) ||
        (s.planoNome?.toLowerCase().includes(q) ?? false)
      );
    });
  }, [situacoes, filtroAluno, filtroSituacao]);

  const resumoSituacoes = useMemo(() => {
    return situacoes.reduce(
      (acc, s) => {
        acc[s.situacao] = (acc[s.situacao] ?? 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );
  }, [situacoes]);

  const abrirVinculo = (alunoId?: number) => {
    setVinculo({
      alunoId: alunoId ? String(alunoId) : "",
      planoId: "",
    });
    setDialogOpen(true);
  };

  const abrirEdicao = (p: Plano) => {
    setPlanoEditando(p);
    setValorEdit(p.valor != null ? String(p.valor) : "");
    setValidadeEdit(p.validadeMeses != null ? String(p.validadeMeses) : "");
    setEditDialogOpen(true);
  };

  const salvarEdicao = async () => {
    if (!planoEditando) return;
    const valor = Number(valorEdit.replace(",", "."));
    if (!valor || valor <= 0) {
      toast.error("Informe um valor válido");
      return;
    }
    const body: { valor: number; validadeMeses?: number } = { valor };
    if (
      planoEditando.categoriaPlano === "AVULSO" &&
      (planoEditando.quantidadeRemadas ?? 0) > 1 &&
      validadeEdit
    ) {
      body.validadeMeses = Number(validadeEdit);
    }
    setSalvandoEdicao(true);
    try {
      await api.put(`/planos/${planoEditando.id}`, body);
      toast.success("Valor atualizado");
      setEditDialogOpen(false);
      setPlanoEditando(null);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    } finally {
      setSalvandoEdicao(false);
    }
  };

  const salvarVinculo = async () => {
    if (!vinculo.alunoId || !vinculo.planoId) {
      toast.error("Selecione aluno e plano");
      return;
    }
    try {
      await api.post(`/alunos/${vinculo.alunoId}/planos`, {
        planoId: Number(vinculo.planoId),
        dataInicio: hojeSaoPaulo(),
      });
      toast.success("Plano vinculado ao aluno");
      setDialogOpen(false);
      load();
    } catch (e) {
      toast.error((e as ApiError).message);
    }
  };

  const descricaoPlano = (p: Plano) => {
    if (p.categoriaPlano === "EQUIPAMENTO") {
      if (p.nome.includes("Pedalinho")) return "20 minutos";
      return "Por hora";
    }
    if (p.categoriaPlano === "AVULSO") {
      return p.quantidadeRemadas === 1
        ? "1 remada"
        : `${p.quantidadeRemadas} remadas`;
    }
    if (p.categoriaPlano === "WELLHUB") {
      return `${p.quantidadeAulasMes ?? 8} aulas/mês`;
    }
    if (p.ilimitado) return "Aulas ilimitadas";
    if (p.quantidadeAulasSemana) return `${p.quantidadeAulasSemana}x/semana`;
    return tipoPlanoLabel[p.tipoPlano];
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Planos"
        description="Catálogo de planos e situação dos alunos"
        actions={
          <Button onClick={() => abrirVinculo()}>
            <Link2 className="h-4 w-4" />
            Vincular plano
          </Button>
        }
      />

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
        {(["PLANO", "AVULSO", "PACOTE", "WELLHUB", "SEM_PLANO"] as const).map((sit) => (
          <StatTile
            key={sit}
            label={situacaoAlunoLabel[sit]}
            value={resumoSituacoes[sit] ?? 0}
            icon={Users}
            tone={sit === "WELLHUB" ? "primary" : sit === "SEM_PLANO" ? "neutral" : "info"}
          />
        ))}
      </div>

      {loading ? (
        <Loading />
      ) : (
        <Tabs defaultValue="catalogo">
          <TabsList>
            <TabsTrigger value="catalogo">Catálogo</TabsTrigger>
            <TabsTrigger value="alunos">Alunos</TabsTrigger>
          </TabsList>

          <TabsContent value="catalogo">
            <div className="grid gap-6 lg:grid-cols-2">
              <Card variant="outlined">
                <CardHeader>
                  <CardTitle className="text-lg">Planos por frequência na semana</CardTitle>
                  <p className="text-sm text-muted">Canoa havaiana — valores por periodicidade</p>
                </CardHeader>
                <CardContent className="space-y-4">
                  {gruposPorFrequencia.map((grupo) => (
                    <div
                      key={grupo.rotulo}
                      className="rounded-xl border border-outline bg-surface p-4 space-y-3"
                    >
                      <div>
                        <p className="font-semibold text-foreground">{grupo.rotulo}</p>
                        {grupo.subtitulo && (
                          <p className="text-xs text-muted mt-0.5">{grupo.subtitulo}</p>
                        )}
                      </div>
                      <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                        {grupo.planos.map((p) => (
                          <div
                            key={p.id}
                            className="relative rounded-lg bg-surface-variant border border-outline px-3 py-2 text-center"
                          >
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              className="absolute top-0.5 right-0.5 h-6 w-6 p-0"
                              onClick={() => abrirEdicao(p)}
                              title="Editar valor"
                            >
                              <Pencil className="h-3 w-3" />
                            </Button>
                            <p className="text-xs text-muted">
                              {p.periodicidade
                                ? periodicidadePlanoLabel[p.periodicidade]
                                : "—"}
                            </p>
                            <p className="text-lg font-bold text-foreground mt-0.5">
                              {formatMoeda(p.valor)}
                            </p>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </CardContent>
              </Card>

              <div className="space-y-6">
                <Card variant="outlined">
                  <CardHeader>
                    <CardTitle className="text-lg">Remadas avulsas</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    {remadasAvulsas.map((p) => (
                      <div
                        key={p.id}
                        className="flex items-center justify-between gap-2 rounded-lg border border-outline bg-surface px-4 py-3"
                      >
                        <span className="font-medium text-foreground">
                          {p.nome.replace("Remada Avulsa — ", "")}
                        </span>
                        <div className="flex items-center gap-2">
                          <span className="text-lg font-bold text-foreground">
                            {formatMoeda(p.valor)}
                          </span>
                          <Button variant="ghost" size="sm" onClick={() => abrirEdicao(p)}>
                            <Pencil className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>

                <Card variant="outlined">
                  <CardHeader>
                    <CardTitle className="text-lg">Pacotes de remadas avulsas</CardTitle>
                    <p className="text-xs text-slate-500">
                      Créditos podem ser compartilhados com outras pessoas
                    </p>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    {pacotesRemadas.map((p) => (
                      <div
                        key={p.id}
                        className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-slate-200 bg-white px-4 py-3"
                      >
                        <span className="font-medium text-foreground">
                          {p.quantidadeRemadas} avulsas
                        </span>
                        <div className="flex items-center gap-2">
                          <div className="text-right">
                            <p className="text-lg font-bold text-slate-900">
                              {formatMoeda(p.valor)}
                            </p>
                            {p.validadeMeses && (
                              <p className="text-xs text-muted">
                                validade {p.validadeMeses} meses
                              </p>
                            )}
                          </div>
                          <Button variant="ghost" size="sm" onClick={() => abrirEdicao(p)}>
                            <Pencil className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>

                {planosPorCategoria.WELLHUB.length > 0 && (
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-lg">{categoriaPlanoLabel.WELLHUB}</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-2">
                      {planosPorCategoria.WELLHUB.map((p) => (
                        <div
                          key={p.id}
                          className="flex items-center justify-between rounded-lg border px-4 py-3"
                        >
                          <span className="font-medium">{p.nome}</span>
                          <span className="text-slate-600">{descricaoPlano(p)}</span>
                        </div>
                      ))}
                    </CardContent>
                  </Card>
                )}

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">{categoriaPlanoLabel.EQUIPAMENTO}</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    {planosPorCategoria.EQUIPAMENTO.map((p) => (
                      <div
                        key={p.id}
                        className="flex items-center justify-between rounded-lg border px-4 py-3"
                      >
                        <span className="font-medium text-foreground">{p.nome}</span>
                        <div className="flex items-center gap-2">
                          <div className="text-right">
                            <p className="text-lg font-bold text-slate-900">
                              {formatMoeda(p.valor)}
                            </p>
                            <p className="text-xs text-muted">{descricaoPlano(p)}</p>
                          </div>
                          <Button variant="ghost" size="sm" onClick={() => abrirEdicao(p)}>
                            <Pencil className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="alunos">
            <div className="flex flex-wrap gap-2 mb-4">
              <div className="relative max-w-sm flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <Input
                  placeholder="Buscar aluno ou plano..."
                  value={filtroAluno}
                  onChange={(e) => setFiltroAluno(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Select
                value={filtroSituacao}
                onChange={(e) => setFiltroSituacao(e.target.value)}
                className="max-w-[180px]"
              >
                <option value="">Todas as situações</option>
                <option value="PLANO">Plano</option>
                <option value="AVULSO">Avulso</option>
                <option value="PACOTE">Pacote</option>
                <option value="WELLHUB">Wellhub</option>
                <option value="SEM_PLANO">Sem plano</option>
              </Select>
            </div>

            <Card>
              <CardContent className="p-0">
                <table className="w-full text-sm">
                  <thead className="bg-surface-variant border-b border-outline sticky top-0">
                    <tr>
                      <th className="text-left p-3">Aluno</th>
                      <th className="text-left p-3">Situação</th>
                      <th className="text-left p-3">Plano ativo</th>
                      <th className="text-left p-3">Validade</th>
                      <th className="text-left p-3">Uso</th>
                      <th className="p-3"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {situacoesFiltradas.map((s) => (
                      <tr key={s.alunoId} className="border-b border-outline even:bg-surface-variant/30 hover:bg-surface-variant/50">
                        <td className="p-3">
                          <p className="font-medium">{s.alunoNome}</p>
                          <p className="text-xs text-slate-500">{s.alunoEmail}</p>
                        </td>
                        <td className="p-3">
                          <Badge variant={situacaoAlunoVariant[s.situacao]}>
                            {situacaoAlunoLabel[s.situacao]}
                          </Badge>
                        </td>
                        <td className="p-3 text-slate-600">{s.planoNome ?? "—"}</td>
                        <td className="p-3 text-slate-600">
                          {s.dataFim
                            ? `até ${s.dataFim.split("-").reverse().join("/")}`
                            : s.dataInicio
                              ? `desde ${s.dataInicio.split("-").reverse().join("/")}`
                              : "—"}
                        </td>
                        <td className="p-3 text-slate-600">
                          {s.quantidadeRemadas != null
                            ? `${s.remadasConsumidas ?? 0}/${s.quantidadeRemadas} remadas`
                            : "—"}
                        </td>
                        <td className="p-3 text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => abrirVinculo(s.alunoId)}
                          >
                            <Link2 className="h-4 w-4" />
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      )}

      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent title="Editar plano">
          {planoEditando && (
            <div className="space-y-3">
              <p className="text-sm text-slate-600">{planoEditando.nome}</p>
              <div>
                <p className="text-sm font-medium mb-1">Valor (R$)</p>
                <Input
                  type="number"
                  min="0"
                  step="0.01"
                  value={valorEdit}
                  onChange={(e) => setValorEdit(e.target.value)}
                />
              </div>
              {planoEditando.categoriaPlano === "AVULSO" &&
                (planoEditando.quantidadeRemadas ?? 0) > 1 && (
                  <div>
                    <p className="text-sm font-medium mb-1">Validade (meses)</p>
                    <Input
                      type="number"
                      min="1"
                      value={validadeEdit}
                      onChange={(e) => setValidadeEdit(e.target.value)}
                    />
                  </div>
                )}
              <Button className="w-full" onClick={salvarEdicao} disabled={salvandoEdicao}>
                {salvandoEdicao ? "Salvando..." : "Salvar"}
              </Button>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent title="Vincular plano ao aluno">
          <div className="space-y-3">
            <div>
              <p className="text-sm font-medium mb-1">Aluno</p>
              <Select
                value={vinculo.alunoId}
                onChange={(e) => setVinculo({ ...vinculo, alunoId: e.target.value })}
              >
                <option value="">Selecione o aluno...</option>
                {situacoes.map((s) => (
                  <option key={s.alunoId} value={s.alunoId}>
                    {s.alunoNome} ({situacaoAlunoLabel[s.situacao]})
                  </option>
                ))}
              </Select>
            </div>
            <div>
              <p className="text-sm font-medium mb-1">Plano</p>
              <Select
                value={vinculo.planoId}
                onChange={(e) => setVinculo({ ...vinculo, planoId: e.target.value })}
              >
                <option value="">Selecione o plano...</option>
                {categorias.map((cat) => (
                  <optgroup key={cat} label={categoriaPlanoLabel[cat]}>
                    {planosPorCategoria[cat].map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.nome} — {formatMoeda(p.valor)}
                      </option>
                    ))}
                  </optgroup>
                ))}
              </Select>
            </div>
            <Button onClick={salvarVinculo} className="w-full">
              Vincular
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
