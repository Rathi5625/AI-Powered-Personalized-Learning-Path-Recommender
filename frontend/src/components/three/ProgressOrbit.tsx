export function ProgressOrbit({ completion, className }: { completion: number; className?: string }) {
  const radius = 85;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (completion / 100) * circumference;

  return (
    <div className={`relative flex items-center justify-center ${className || ''}`}>
      <svg 
        viewBox="0 0 200 200" 
        className="w-full h-full transition-transform duration-300 hover:scale-105"
        style={{ maxWidth: '100%' }}
      >
        <circle
          cx="100"
          cy="100"
          r={radius}
          fill="none"
          stroke="#A1A1AA"
          strokeWidth="6"
          opacity="0.15"
        />
        <circle
          cx="100"
          cy="100"
          r={radius}
          fill="none"
          stroke="#38BDF8"
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          className="transition-all duration-1000 ease-out"
          transform="rotate(-90 100 100)"
        />
      </svg>
      <div className="absolute flex flex-col items-center justify-center pointer-events-none">
        <span className="text-4xl font-display text-[#FFFFFF]">{Math.round(completion)}%</span>
        <span className="text-sm text-[#A1A1AA] uppercase tracking-wider mt-1">complete</span>
      </div>
    </div>
  );
}
