import React, { useMemo } from 'react';

export function ParticleField({ className }: { className?: string }) {
  const particles = useMemo(() => {
    const items = [];
    for (let i = 0; i < 30; i++) {
      items.push({
        id: i,
        size: Math.random() * 4 + 3, // 3-7px
        left: `${Math.random() * 100}%`,
        opacity: Math.random() * 0.3 + 0.1,
        animationDuration: `${Math.random() * 25 + 15}s`, // 15-40s
        animationDelay: `-${Math.random() * 20}s`, // -20s to 0s
        tx: `${(Math.random() - 0.5) * 100}px`
      });
    }
    return items;
  }, []);

  return (
    <div className={`absolute inset-0 overflow-hidden pointer-events-none ${className || ''}`}>
      <style>
        {`
          @keyframes float-up {
            0% {
              transform: translateY(100vh) translateX(0);
            }
            100% {
              transform: translateY(-20vh) translateX(var(--tx));
            }
          }
        `}
      </style>
      {particles.map((p) => (
        <span
          key={p.id}
          className="absolute rounded-full bg-[#38BDF8]"
          style={{
            width: p.size,
            height: p.size,
            left: p.left,
            bottom: '-20px',
            opacity: p.opacity,
            animation: `float-up ${p.animationDuration} linear infinite`,
            animationDelay: p.animationDelay,
            '--tx': p.tx
          } as React.CSSProperties}
        />
      ))}
    </div>
  );
}
