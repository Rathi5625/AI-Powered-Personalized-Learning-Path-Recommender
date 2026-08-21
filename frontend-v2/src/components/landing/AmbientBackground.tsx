import React from 'react';

export const AmbientBackground: React.FC = () => {
  return (
    <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10 bg-[#FAF8F5]">
      {/* Top Left Peach / Orange Ambient Glow */}
      <div
        className="absolute -top-32 -left-32 w-[550px] h-[550px] rounded-full blur-[130px] opacity-45"
        style={{
          background: 'radial-gradient(circle, #FFD3C2 0%, rgba(255, 237, 229, 0.4) 60%, transparent 80%)',
        }}
      />

      {/* Top Right / Center Behind Dashboard Lavender Glow */}
      <div
        className="absolute top-10 right-0 w-[650px] h-[650px] rounded-full blur-[140px] opacity-45"
        style={{
          background: 'radial-gradient(circle, #DDD8FE 0%, rgba(235, 230, 255, 0.35) 60%, transparent 80%)',
        }}
      />

      {/* Center Bottom Soft Warm Glow */}
      <div
        className="absolute bottom-0 left-1/3 w-[600px] h-[500px] rounded-full blur-[160px] opacity-35"
        style={{
          background: 'radial-gradient(circle, #FFEADF 0%, rgba(250, 245, 238, 0.2) 65%, transparent 80%)',
        }}
      />

      {/* Very subtle noise/vignette overlay */}
      <div className="absolute inset-0 bg-gradient-to-b from-transparent via-[#FAF8F5]/40 to-[#FAF8F5]" />
    </div>
  );
};
