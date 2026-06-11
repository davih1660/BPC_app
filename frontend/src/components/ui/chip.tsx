import { cn } from "@/lib/utils";
import { chipVariantClass, type ChipVariant } from "@/lib/design-tokens";

export function Chip({
  variant = "default",
  className,
  children,
}: {
  variant?: ChipVariant;
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium",
        chipVariantClass[variant],
        className
      )}
    >
      {children}
    </span>
  );
}
