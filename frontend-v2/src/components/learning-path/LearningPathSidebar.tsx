import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Route,
  Compass,
  Award,
  CheckSquare,
  FolderGit2,
  Sparkles,
  TrendingUp,
  Settings,
  HelpCircle,
  Zap,
} from 'lucide-react';

interface SidebarItem {
  name: string;
  href: string;
  icon: React.ElementType;
}

const NAV_ITEMS: SidebarItem[] = [
  { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
  { name: 'My Learning Path', href: '/learning-path', icon: Route },
  { name: 'Explore Courses', href: '/explore-courses', icon: Compass },
  { name: 'Skills', href: '/skills', icon: Award },
  { name: 'Assessments', href: '/assessments', icon: CheckSquare },
  { name: 'Projects', href: '/projects', icon: FolderGit2 },
  { name: 'AI Mentor', href: '/ai-mentor', icon: Sparkles },
  { name: 'Progress', href: '/progress', icon: TrendingUp },
];

export const LearningPathSidebar: React.FC = () => {
  const location = useLocation();

  return (
    <aside className="hidden lg:flex flex-col w-[250px] shrink-0 h-screen sticky top-0 bg-white/70 backdrop-blur-xl border-r border-gray-200/80 p-5 justify-between z-30 select-none">
      {/* Top Branding & Navigation */}
      <div className="space-y-6">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-3 px-2 group cursor-pointer">
          <div className="w-9 h-9 rounded-xl bg-[#8e4d2b] text-white flex items-center justify-center font-bold text-sm shadow-xs group-hover:scale-105 transition-transform">
            <span>L</span>
          </div>
          <div>
            <span className="font-extrabold text-base tracking-tight text-[#0f1b32] block leading-none">
              LearnAI
            </span>
            <span className="text-[10px] font-semibold text-[#86736b] tracking-wider uppercase block mt-1">
              Personalized Growth
            </span>
          </div>
        </Link>

        {/* Navigation Items */}
        <nav className="space-y-1">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.href || (item.href === '/learning-path' && location.pathname.startsWith('/learning-path'));
            return (
              <Link
                key={item.name}
                to={item.href}
                className={`
                  flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-medium transition-all duration-150
                  ${
                    isActive
                      ? 'bg-[#ffdbcb]/60 text-[#8e4d2b] font-bold shadow-2xs'
                      : 'text-[#53433c] hover:bg-black/[0.03] hover:text-[#0f1b32]'
                  }
                `}
              >
                <Icon
                  className={`w-4 h-4 shrink-0 ${
                    isActive ? 'text-[#8e4d2b]' : 'text-[#86736b]'
                  }`}
                />
                <span>{item.name}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      {/* Bottom Upgrade Card & Secondary Links */}
      <div className="space-y-3 pt-4 border-t border-gray-100">
        {/* Pro Banner */}
        <div className="bg-[#FAF4F0] border border-[#F2DACB] rounded-2xl p-3.5 text-left">
          <div className="flex items-center gap-2 mb-1.5 text-[#8e4d2b]">
            <Zap className="w-4 h-4" />
            <span className="text-xs font-bold uppercase tracking-wider">
              Upgrade to Pro
            </span>
          </div>
          <p className="text-[11px] text-[#53433c] leading-relaxed mb-2.5">
            Unlock real-time AI mentoring and advanced coding labs.
          </p>
          <button
            type="button"
            className="w-full py-1.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-[11px] font-bold transition-colors cursor-pointer"
          >
            Upgrade Plan
          </button>
        </div>

        {/* Footer Nav */}
        <div className="flex items-center justify-between px-2 pt-1 text-xs text-[#86736b]">
          <Link
            to="/settings"
            className="inline-flex items-center gap-1.5 hover:text-[#0f1b32] transition-colors"
          >
            <Settings className="w-3.5 h-3.5" />
            <span>Settings</span>
          </Link>
          <Link
            to="/help"
            className="inline-flex items-center gap-1.5 hover:text-[#0f1b32] transition-colors"
          >
            <HelpCircle className="w-3.5 h-3.5" />
            <span>Help</span>
          </Link>
        </div>
      </div>
    </aside>
  );
};
