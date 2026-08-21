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
  Settings,
  HelpCircle,
  User,
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
  { name: 'Projects', href: '/project-details', icon: Rocket },
  { name: 'AI Mentor', href: '/ai-mentor', icon: Bot },
  { name: 'Progress', href: '/progress', icon: BarChart3 },
];

interface HelpSidebarProps {
  onUpgrade?: () => void;
}

export const HelpSidebar: React.FC<HelpSidebarProps> = ({ onUpgrade }) => {
  const location = useLocation();

  return (
    <aside className="hidden lg:flex flex-col w-[250px] shrink-0 h-screen sticky top-0 bg-white/70 backdrop-blur-2xl border-r border-gray-200/80 p-5 justify-between z-30 select-none text-left">
      {/* Brand & Navigation */}
      <div className="space-y-6">
        {/* Brand */}
        <Link to="/" className="block px-2 group cursor-pointer">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-[#d98b63] to-[#8e4d2b] flex items-center justify-center text-white font-extrabold shadow-2xs">
              L
            </div>
            <div>
              <span className="font-extrabold text-xl tracking-tight text-[#8e4d2b] block leading-none">
                LearnAI
              </span>
              <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block mt-1">
                PERSONALIZED GROWTH
              </span>
            </div>
          </div>
        </Link>

        {/* Navigation Items */}
        <nav className="space-y-1">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.href;

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

      {/* Footer Section */}
      <div className="space-y-3 pt-3">
        {/* Upgrade to Pro Button */}
        <button
          type="button"
          onClick={onUpgrade}
          className="w-full py-3 px-4 rounded-2xl bg-[#FAF4F0] hover:bg-[#F2DACB]/60 border border-[#F2DACB] text-[#8e4d2b] text-xs font-bold transition-all shadow-2xs hover:shadow-xs cursor-pointer text-center"
        >
          Upgrade to Pro
        </button>

        {/* Bottom Nav: Settings, Help & Support (ACTIVE), Profile */}
        <div className="space-y-1 pt-1 border-t border-gray-100/80">
          <Link
            to="/settings"
            className="flex items-center gap-3 px-3.5 py-2 rounded-xl text-xs font-medium text-[#53433c] hover:text-[#0f1b32] hover:bg-black/[0.02] transition-colors"
          >
            <Settings className="w-4 h-4 text-[#86736b]" />
            <span>Settings</span>
          </Link>

          <Link
            to="/help-support"
            className="flex items-center gap-3 px-3.5 py-2 rounded-xl text-xs font-bold bg-[#ffdbcb]/60 text-[#8e4d2b] shadow-2xs"
          >
            <HelpCircle className="w-4 h-4 text-[#8e4d2b]" />
            <span>Help &amp; Support</span>
          </Link>

          <Link
            to="/profile"
            className="flex items-center gap-3 px-3.5 py-2 rounded-xl text-xs font-medium text-[#53433c] hover:text-[#0f1b32] hover:bg-black/[0.02] transition-colors"
          >
            <User className="w-4 h-4 text-[#86736b]" />
            <span>Profile</span>
          </Link>
        </div>
      </div>
    </aside>
  );
};
