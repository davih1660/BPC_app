"use client";

import { useRef } from "react";
import { Button } from "@/components/ui/button";

type FileInputProps = {
  accept?: string;
  multiple?: boolean;
  files: File[];
  onChange: (files: FileList | null) => void;
  disabled?: boolean;
  className?: string;
};

export function FileInput({
  accept,
  multiple,
  files,
  onChange,
  disabled,
  className,
}: FileInputProps) {
  const inputRef = useRef<HTMLInputElement>(null);

  const rotulo =
    files.length === 0
      ? "Arquivo(s) não selecionado(s)."
      : files.map((f) => f.name).join(", ");

  return (
    <div className={`flex flex-wrap items-center gap-2 ${className ?? ""}`}>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        multiple={multiple}
        className="hidden"
        disabled={disabled}
        onChange={(e) => onChange(e.target.files)}
      />
      <Button
        type="button"
        variant="outline"
        size="sm"
        disabled={disabled}
        onClick={() => inputRef.current?.click()}
      >
        Selecionar arquivo
      </Button>
      <span className="text-sm text-slate-500">{rotulo}</span>
    </div>
  );
}
