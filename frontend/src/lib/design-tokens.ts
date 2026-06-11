/** Semantic chip/badge variants mapped from domain status keys */
export type ChipVariant = "default" | "success" | "warning" | "error" | "info" | "neutral";

export const chipVariantClass: Record<ChipVariant, string> = {
  default: "bg-primary-container text-on-primary-container border-transparent",
  success: "bg-success-bg text-success border-transparent",
  warning: "bg-warning-bg text-warning border-transparent",
  error: "bg-error-bg text-error border-transparent",
  info: "bg-info-bg text-info border-transparent",
  neutral: "bg-surface-variant text-muted border-outline",
};
