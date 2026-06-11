"use client";

import { Toaster } from "sonner";
import { AuthProvider } from "@/contexts/auth-context";

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      {children}
      <Toaster
        position="top-center"
        richColors
        toastOptions={{
          className: "md:!top-4",
        }}
        mobileOffset={{ bottom: "5rem" }}
      />
    </AuthProvider>
  );
}
