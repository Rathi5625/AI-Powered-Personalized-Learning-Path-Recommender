import React from 'react';

export interface SupportRequestItem {
  id: string;
  title: string;
  status: 'In Progress' | 'Resolved';
}

interface SupportStatusCardProps {
  recentRequests: SupportRequestItem[];
}

export const SupportStatusCard: React.FC<SupportStatusCardProps> = ({
  recentRequests,
}) => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4 select-none">
      {/* Top Status & Response Time */}
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="relative flex h-2.5 w-2.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500" />
          </span>
          <span className="text-xs font-bold text-[#0f1b32]">
            All systems operational
          </span>
        </div>

        <div className="text-right">
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">
            AVG RESPONSE
          </span>
          <span className="text-xs font-bold text-[#0f1b32] block mt-0.5">
            ~ 2 hrs
          </span>
        </div>
      </div>

      {/* Divider */}
      <div className="pt-2 border-t border-gray-100/80 space-y-3">
        <span className="text-[10px] font-extrabold text-gray-400 uppercase tracking-wider block">
          RECENT REQUESTS
        </span>

        {/* Requests List */}
        <div className="space-y-2.5">
          {recentRequests.map((req) => (
            <div
              key={req.id}
              className="flex items-center justify-between gap-3 p-3 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs text-xs"
            >
              <span className="font-semibold text-[#0f1b32] truncate max-w-[170px]">
                {req.title}
              </span>

              <span
                className={`
                  px-2.5 py-0.5 rounded-full text-[10px] font-bold shrink-0
                  ${
                    req.status === 'In Progress'
                      ? 'bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b]'
                      : 'bg-emerald-50 border border-emerald-200 text-emerald-700'
                  }
                `}
              >
                {req.status}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
