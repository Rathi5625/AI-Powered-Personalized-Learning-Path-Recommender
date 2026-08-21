import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Route,
  Compass,
  Award,
  CheckSquare,
  FolderGit2,
  Bot,
  TrendingUp,
  Settings,
  HelpCircle,
  Brain,
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
  { name: 'AI Mentor', href: '/ai-mentor', icon: Bot },
  { name: 'Progress', href: '/progress', icon: TrendingUp },
];

interface DashboardSidebarProps {
  onUpgrade?: () => void;
}

export const DashboardSidebar: React.FC<DashboardSidebarProps> = ({ onUpgrade }) => {
  const location = useLocation();

  return (
    <aside className="hidden lg:flex flex-col w-[250px] shrink-0 h-screen sticky top-0 bg-white/75 backdrop-blur-2xl border-r border-gray-200/80 p-5 justify-between z-30 select-none">
      {/* Top Branding & Navigation */}
      <div className="space-y-6">
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-3 px-2 group cursor-pointer">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#d98b63] to-[#8e4d2b] text-white flex items-center justify-center shadow-xs group-hover:scale-105 transition-transform">
            <Brain className="w-5 h-5 text-white" />
          </div>
          <span className="font-extrabold text-lg tracking-tight text-[#0f1b32]">
            LearnAI
          </span>
        </Link>

        {/* Navigation Items */}
        <nav className="space-y-1">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive =
              location.pathname === item.href ||
              (item.href === '/dashboard' && (location.pathname === '/' || location.pathname === '/dashboard'));

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

      {/* Bottom Section */}
      <div className="space-y-3 pt-4 border-t border-gray-100/90">
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
          <span>Help & Support</span>
        </Link>

        {/* Upgrade Button */}
        <button
          type="button"
          onClick={onUpgrade}
          className="w-full py-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] active:bg-[#623219] text-white text-xs font-bold transition-all shadow-xs cursor-pointer"
        >
          Upgrade to Pro
        </button>
      </div>
    </aside>
  );
};
