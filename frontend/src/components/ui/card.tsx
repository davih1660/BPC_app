import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const cardVariants = cva("bg-surface text-foreground", {
  variants: {
    variant: {
      elevated: "rounded-xl border border-outline shadow-sm",
      outlined: "rounded-xl border border-outline",
      filled: "rounded-xl bg-surface-variant border border-transparent",
      student: "rounded-2xl border border-outline shadow-sm",
    },
  },
  defaultVariants: { variant: "elevated" },
});

export function Card({
  className,
  variant,
  children,
}: {
  className?: string;
  variant?: VariantProps<typeof cardVariants>["variant"];
  children: React.ReactNode;
}) {
  return <div className={cn(cardVariants({ variant }), className)}>{children}</div>;
}

export function CardHeader({ className, children }: { className?: string; children: React.ReactNode }) {
  return <div className={cn("flex flex-col gap-1.5 p-5 pb-2", className)}>{children}</div>;
}

export function CardTitle({ className, children }: { className?: string; children: React.ReactNode }) {
  return <h3 className={cn("text-lg font-semibold text-foreground", className)}>{children}</h3>;
}

export function CardContent({ className, children }: { className?: string; children: React.ReactNode }) {
  return <div className={cn("p-5 pt-2", className)}>{children}</div>;
}
