const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

let usuarioId: number | null = null;

export function imagemOcorrenciaUrl(imagemId: number) {
  return `${API_URL}/ocorrencias/imagens/${imagemId}/arquivo`;
}

export function setUsuarioId(id: number | null) {
  usuarioId = id;
}

export function getUsuarioId() {
  return usuarioId;
}

export class ApiError extends Error {
  code: string;
  constructor(message: string, code: string) {
    super(message);
    this.code = code;
  }
}

async function request<T>(path: string, options: RequestInit = {}, timeoutMs = 15_000): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };
  if (usuarioId) {
    headers["X-Usuario-Id"] = String(usuarioId);
  }
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const res = await fetch(`${API_URL}${path}`, {
      ...options,
      headers,
      signal: controller.signal,
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText, code: "ERROR" }));
      throw new ApiError(err.message || "Erro na requisição", err.code || "ERROR");
    }
    if (res.status === 204) return undefined as T;
    return res.json();
  } catch (error) {
    if (error instanceof ApiError) throw error;
    if (error instanceof DOMException && error.name === "AbortError") {
      throw new ApiError(
        "Backend não respondeu. Verifique se a API está rodando (docker compose up).",
        "TIMEOUT"
      );
    }
    throw new ApiError(
      "Não foi possível conectar à API em localhost:8080.",
      "NETWORK_ERROR"
    );
  } finally {
    clearTimeout(timeoutId);
  }
}

async function upload<T>(path: string, formData: FormData): Promise<T> {
  const headers: Record<string, string> = {};
  if (usuarioId) {
    headers["X-Usuario-Id"] = String(usuarioId);
  }
  const res = await fetch(`${API_URL}${path}`, { method: "POST", headers, body: formData });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText, code: "ERROR" }));
    throw new ApiError(err.message || "Erro na requisição", err.code || "ERROR");
  }
  return res.json();
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "PUT", body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "PATCH", body: body ? JSON.stringify(body) : undefined }),
  delete: (path: string) => request<void>(path, { method: "DELETE" }),
  upload: <T>(path: string, formData: FormData) => upload<T>(path, formData),
};
