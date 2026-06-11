"use client";

import * as DialogPrimitive from "@radix-ui/react-dialog";
import { X } from "lucide-react";
import { cn } from "@/lib/utils";

export function Dialog({ open, onOpenChange, children }: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  children: React.ReactNode;
}) {
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      {children}
    </DialogPrimitive.Root>
  );
}

export function DialogContent({ title, children, className, hideTitle }: {
  title?: string;
  children: React.ReactNode;
  className?: string;
  hideTitle?: boolean;
}) {
  const semTitulo = hideTitle || !title;

  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/40" />
      <DialogPrimitive.Content className={cn(
        "fixed left-1/2 top-1/2 z-50 w-full max-w-lg max-h-[90vh] overflow-y-auto -translate-x-1/2 -translate-y-1/2 rounded-2xl border border-outline bg-surface p-6 shadow-md",
        className
      )}>
        {semTitulo ? (
          <>
            <DialogPrimitive.Title className="sr-only">Visualizar</DialogPrimitive.Title>
            <DialogPrimitive.Close className="absolute right-4 top-4 z-20 rounded-lg p-1.5 hover:bg-surface-variant text-muted bg-surface/90">
              <X className="h-4 w-4" />
            </DialogPrimitive.Close>
          </>
        ) : (
          <div className="flex items-center justify-between mb-4">
            <DialogPrimitive.Title className="text-lg font-semibold text-foreground">{title}</DialogPrimitive.Title>
            <DialogPrimitive.Close className="rounded-lg p-1.5 hover:bg-surface-variant text-muted">
              <X className="h-4 w-4" />
            </DialogPrimitive.Close>
          </div>
        )}
        {children}
      </DialogPrimitive.Content>
    </DialogPrimitive.Portal>
  );
}
