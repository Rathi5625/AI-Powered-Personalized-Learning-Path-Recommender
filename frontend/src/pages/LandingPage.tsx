import { Link } from 'react-router-dom';
import { SceneCanvas } from '../components/three/HeroScene';

export function LandingPage() {
  return (
    <div className="relative min-h-screen bg-[#000000] text-[#FFFFFF] overflow-hidden">
      {/* 3D Background */}
      <SceneCanvas interactive={true} />

      {/* Content Overlay */}
      <div className="relative z-10 mx-auto flex min-h-screen max-w-7xl flex-col justify-center px-4 py-10 pointer-events-none">
        <div className="pointer-events-auto max-w-2xl">
          <p className="mb-4 text-sm font-bold uppercase tracking-[0.3em] text-[#A1A1AA]">
            AI-POWERED LEARNING
          </p>
          <h1 className="font-display text-6xl font-black uppercase leading-[1.1] tracking-tight md:text-7xl lg:text-8xl">
            YOUR PATH <br /> TO ANY <br /> TECH CAREER
          </h1>
          <p className="mt-8 max-w-xl text-lg font-light tracking-wide text-[#A1A1AA] md:text-xl">
            DISCOVER SKILL GAPS. GET ML-POWERED RECOMMENDATIONS. FOLLOW ADAPTIVE PATHS BUILT FOR YOUR GOALS.
          </p>
          <div className="mt-12 flex flex-wrap gap-6">
            <Link
              to="/signup"
              className="rounded-full border border-white/20 px-8 py-4 text-sm font-bold uppercase tracking-widest text-[#FFFFFF] transition-colors hover:bg-white/10"
            >
              Get Started
            </Link>
            <Link
              to="/login"
              className="rounded-full border border-white/20 px-8 py-4 text-sm font-bold uppercase tracking-widest text-[#FFFFFF] transition-colors hover:bg-white/10"
            >
              Sign In
            </Link>
          </div>
          
          <div className="mt-16 grid gap-6 sm:grid-cols-3">
            {[
              ['Skill Gap Analysis', 'Compare your skills vs career requirements'],
              ['Smart Recommendations', 'Rule-based + ML course ranking'],
              ['Adaptive Paths', 'AI-generated phases that evolve with you'],
            ].map(([title, desc]) => (
              <div key={title} className="rounded-2xl border border-white/10 bg-[#000000]/50 p-6 backdrop-blur-md transition-colors hover:bg-[#111111]/80">
                <p className="text-sm font-bold uppercase tracking-wider text-[#FFFFFF]">{title}</p>
                <p className="mt-2 text-xs font-medium tracking-wide text-[#A1A1AA]">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
