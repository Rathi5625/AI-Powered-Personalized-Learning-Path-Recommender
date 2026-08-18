export function LoadingSpinner({ label = 'Loading...' }: { label?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-[#A1A1AA]">
      <div className="h-10 w-10 animate-spin rounded-full border-2 border-[#38BDF8] border-t-transparent" />
      <p>{label}</p>
    </div>
  );
}
