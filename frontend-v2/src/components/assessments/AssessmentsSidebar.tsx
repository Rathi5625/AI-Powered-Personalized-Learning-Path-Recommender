import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Route,
  Compass,
  Award,
  ClipboardCheck,
  Rocket,
  Bot,
  BarChart3,
  HelpCircle,
  Settings,
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
  { name: 'Assessments', href: '/assessments', icon: ClipboardCheck },
  { name: 'Projects', href: '/projects', icon: Rocket },
  { name: 'AI Mentor', href: '/ai-mentor', icon: Bot },
  { name: 'Progress', href: '/progress', icon: BarChart3 },
];

interface AssessmentsSidebarProps {
  onUpgrade?: () => void;
}

export const AssessmentsSidebar: React.FC<AssessmentsSidebarProps> = ({ onUpgrade }) => {
  const location = useLocation();

  return (
    <aside className="hidden lg:flex flex-col w-[250px] shrink-0 h-screen sticky top-0 bg-white/70 backdrop-blur-2xl border-r border-gray-200/80 p-5 justify-between z-30 select-none text-left">
      {/* Top Section: Brand & Navigation */}
      <div className="space-y-6">
        {/* Brand */}
        <Link to="/" className="block px-2 group cursor-pointer">
          <span className="font-extrabold text-2xl tracking-tight text-[#8e4d2b] block leading-none">
            LearnAI
          </span>
          <span className="text-xs font-semibold text-gray-400 block mt-1">
            Premium Learning
          </span>
        </Link>

        {/* Navigation Items */}
        <nav className="space-y-1">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive =
              location.pathname === item.href ||
              (item.href === '/assessments' && location.pathname.startsWith('/assessments'));

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

      {/* Bottom Section: Upgrade to Pro, Settings, Help */}
      <div className="space-y-3 pt-3">
        {/* Upgrade to Pro Outline Card */}
        <button
          type="button"
          onClick={onUpgrade}
          className="w-full py-2.5 px-3 rounded-2xl border border-[#d98b63]/60 bg-white/60 hover:bg-[#ffdbcb]/30 text-[#8e4d2b] text-xs font-bold transition-colors cursor-pointer shadow-2xs text-center"
        >
          Upgrade to Pro
        </button>

        {/* Secondary Links */}
        <div className="space-y-1">
          <Link
            to="/settings"
            className="flex items-center gap-3 px-3.5 py-2 rounded-xl text-xs font-medium text-[#53433c] hover:text-[#0f1b32] hover:bg-black/[0.02] transition-colors"
          >
            <Settings className="w-4 h-4 text-[#86736b]" />
            <span>Settings</span>
          </Link>
          <Link
            to="/help"
            className="flex items-center gap-3 px-3.5 py-2 rounded-xl text-xs font-medium text-[#53433c] hover:text-[#0f1b32] hover:bg-black/[0.02] transition-colors"
          >
            <HelpCircle className="w-4 h-4 text-[#86736b]" />
            <span>Help</span>
          </Link>
        </div>
      </div>
    </aside>
  );
};
