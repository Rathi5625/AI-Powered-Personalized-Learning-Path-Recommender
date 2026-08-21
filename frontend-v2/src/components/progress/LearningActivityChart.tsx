import React, { useState } from 'react';
import { motion } from 'framer-motion';

type TimeRange = '7 Days' | '30 Days' | '3 Months';

interface ChartPathData {
  path: string;
  fillPath: string;
}

const RANGES: TimeRange[] = ['7 Days', '30 Days', '3 Months'];

const CHART_DATA: Record<TimeRange, ChartPathData> = {
  '7 Days': {
    // Matches screenshot curve: gentle wave, dip, huge smooth peak near end
    path: 'M 0 170 C 50 160, 80 185, 120 185 C 160 185, 180 140, 210 130 C 240 120, 260 185, 290 185 C 320 185, 340 30, 370 30 C 390 30, 400 130, 420 190',
    fillPath:
      'M 0 170 C 50 160, 80 185, 120 185 C 160 185, 180 140, 210 130 C 240 120, 260 185, 290 185 C 320 185, 340 30, 370 30 C 390 30, 400 130, 420 190 L 420 200 L 0 200 Z',
  },
  '30 Days': {
    path: 'M 0 150 C 60 120, 100 160, 150 140 C 200 120, 240 80, 280 110 C 320 140, 360 40, 400 60 C 410 65, 415 150, 420 190',
    fillPath:
      'M 0 150 C 60 120, 100 160, 150 140 C 200 120, 240 80, 280 110 C 320 140, 360 40, 400 60 C 410 65, 415 150, 420 190 L 420 200 L 0 200 Z',
  },
  '3 Months': {
    path: 'M 0 180 C 70 140, 130 110, 190 130 C 250 150, 290 60, 340 50 C 370 45, 395 100, 420 190',
    fillPath:
      'M 0 180 C 70 140, 130 110, 190 130 C 250 150, 290 60, 340 50 C 370 45, 395 100, 420 190 L 420 200 L 0 200 Z',
  },
};

export const LearningActivityChart: React.FC = () => {
  const [selectedRange, setSelectedRange] = useState<TimeRange>('7 Days');

  const currentData = CHART_DATA[selectedRange];

  return (
    <section
      aria-label="Learning Activity Graph"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-6"
    >
      {/* Header & Range Switcher */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
          Learning Activity
        </h3>

        {/* Range Switcher Pills */}
        <div className="inline-flex items-center p-1 rounded-xl bg-[#e9edff]/60 border border-gray-200/60 select-none">
          {RANGES.map((range) => {
            const isActive = selectedRange === range;

            return (
              <button
                key={range}
                type="button"
                onClick={() => setSelectedRange(range)}
                className={`
                  px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer
                  ${
                    isActive
                      ? 'bg-white text-[#0f1b32] shadow-2xs font-bold'
                      : 'text-gray-500 hover:text-[#0f1b32]'
                  }
                `}
              >
                {range}
              </button>
            );
          })}
        </div>
      </div>

      {/* SVG Chart Area */}
      <div className="relative w-full h-[220px] sm:h-[260px] flex items-stretch">
        {/* Y-Axis Labels */}
        <div className="flex flex-col justify-between text-[11px] text-gray-400 font-medium pr-3 pb-2 select-none">
          <span>4h</span>
          <span>3h</span>
          <span>2h</span>
          <span>1h</span>
          <span>0</span>
        </div>

        {/* SVG Curve Container */}
        <div className="relative flex-1 h-full overflow-hidden">
          <svg
            viewBox="0 0 420 200"
            preserveAspectRatio="none"
            className="w-full h-full"
          >
            <defs>
              <linearGradient id="terracottaGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#d98b63" stopOpacity="0.28" />
                <stop offset="100%" stopColor="#FAF4F0" stopOpacity="0.02" />
              </linearGradient>
            </defs>

            {/* Gradient Fill */}
            <motion.path
              key={`fill-${selectedRange}`}
              d={currentData.fillPath}
              fill="url(#terracottaGradient)"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.5 }}
            />

            {/* Main Smooth Line Stroke */}
            <motion.path
              key={`stroke-${selectedRange}`}
              d={currentData.path}
              fill="none"
              stroke="#d98b63"
              strokeWidth="4"
              strokeLinecap="round"
              initial={{ pathLength: 0 }}
              animate={{ pathLength: 1 }}
              transition={{ duration: 0.8, ease: 'easeOut' }}
            />

            {/* Bottom Base Line */}
            <line
              x1="0"
              y1="198"
              x2="420"
              y2="198"
              stroke="#F2DACB"
              strokeWidth="2"
            />
          </svg>
        </div>
      </div>
    </section>
  );
};
