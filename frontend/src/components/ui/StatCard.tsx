interface StatCardProps {
  label: string;
  value: string | number;
  hint?: string;
  accent?: 'indigo' | 'cyan' | 'emerald' | 'amber' | 'rose';
}

export function StatCard({ label, value, hint }: StatCardProps) {
  return (
    <div className="rounded-2xl border border-[#334155] bg-[#111111] p-5 shadow-sm">
      <p className="text-sm text-[#A1A1AA]">{label}</p>
      <p className="mt-2 text-3xl font-bold text-[#FFFFFF]">{value}</p>
      {hint && <p className="mt-1 text-xs text-[#A1A1AA]">{hint}</p>}
    </div>
  );
}
