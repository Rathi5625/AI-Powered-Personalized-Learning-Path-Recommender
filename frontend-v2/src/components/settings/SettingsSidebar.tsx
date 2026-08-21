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
  GraduationCap,
} from 'lucide-react';

interface SidebarItem {
  name: string;
  href: string;
  icon: React.ElementType;
  hasDot?: boolean;
}

const MAIN_NAV_ITEMS: SidebarItem[] = [
  { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
  { name: 'My Learning Path', href: '/learning-path', icon: Route },
  { name: 'Explore Courses', href: '/explore-courses', icon: Compass },
  { name: 'Skills', href: '/skills', icon: Award },
  { name: 'Assessments', href: '/assessments', icon: ClipboardCheck },
  { name: 'Projects', href: '/project-details', icon: Rocket },
  { name: 'AI Mentor', href: '/ai-mentor', icon: Bot, hasDot: true },
  { name: 'Progress', href: '/progress', icon: BarChart3 },
];

export const SettingsSidebar: React.FC = () => {
  const location = useLocation();

  return (
    <aside className="hidden lg:flex flex-col w-[250px] shrink-0 h-screen sticky top-0 bg-white/70 backdrop-blur-2xl border-r border-gray-200/80 p-5 justify-between z-30 select-none text-left">
      {/* Brand & Main Navigation */}
      <div className="space-y-6">
        {/* Brand */}
        <Link to="/" className="block px-2 group cursor-pointer">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-[#8e4d2b] flex items-center justify-center text-white shadow-2xs">
              <GraduationCap className="w-5 h-5" />
            </div>
            <div>
              <span className="font-extrabold text-xl tracking-tight text-[#8e4d2b] block leading-none">
                LearnAI
              </span>
              <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block mt-1">
                PREMIUM LEARNING
              </span>
            </div>
          </div>
        </Link>

        {/* Main Menu Label */}
        <div className="space-y-2">
          <span className="text-[10px] font-extrabold text-gray-400 uppercase tracking-wider px-3.5 block">
            MAIN MENU
          </span>

          <nav className="space-y-0.5">
            {MAIN_NAV_ITEMS.map((item) => {
              const Icon = item.icon;
              const isActive =
                item.href === '/settings' ||
                location.pathname === item.href;

              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`
                    flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-medium transition-all duration-150
                    ${
                      isActive
                        ? 'bg-[#ffdbcb]/60 text-[#8e4d2b] font-bold shadow-2xs'
                        : 'text-[#53433c] hover:bg-black/[0.03] hover:text-[#0f1b32]'
                    }
                  `}
                >
                  <div className="flex items-center gap-3">
                    <Icon
                      className={`w-4 h-4 shrink-0 ${
                        isActive ? 'text-[#8e4d2b]' : 'text-[#86736b]'
                      }`}
                    />
                    <span>{item.name}</span>
                  </div>

                  {item.hasDot && (
                    <span className="w-2 h-2 rounded-full bg-[#8e4d2b]" />
                  )}
                </Link>
              );
            })}
          </nav>
        </div>

        {/* Preferences Label */}
        <div className="space-y-2">
          <span className="text-[10px] font-extrabold text-gray-400 uppercase tracking-wider px-3.5 block">
            PREFERENCES
          </span>

          <nav className="space-y-0.5">
            {/* Settings (Active) */}
            <Link
              to="/settings"
              className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-bold bg-[#ffdbcb]/60 text-[#8e4d2b] shadow-2xs border-l-4 border-[#8e4d2b]"
            >
              <Settings className="w-4 h-4 text-[#8e4d2b]" />
              <span>Settings</span>
            </Link>

            {/* Help */}
            <Link
              to="/help"
              className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-medium text-[#53433c] hover:bg-black/[0.03] hover:text-[#0f1b32] transition-colors"
            >
              <HelpCircle className="w-4 h-4 text-[#86736b]" />
              <span>Help</span>
            </Link>
          </nav>
        </div>
      </div>

      {/* Footer User Profile */}
      <div className="pt-3 border-t border-gray-100/80">
        <Link
          to="/profile"
          className="flex items-center gap-3 px-2 py-2 rounded-2xl hover:bg-black/[0.03] transition-colors cursor-pointer"
        >
          <div className="w-8 h-8 rounded-full bg-[#d98b63] text-white font-extrabold text-xs flex items-center justify-center shrink-0 shadow-2xs">
            PR
          </div>
          <div className="text-left overflow-hidden">
            <span className="text-xs font-bold text-[#0f1b32] block truncate">
              Parth Rathi
            </span>
            <span className="text-[10px] font-medium text-gray-400 block truncate">
              Software Engineer
            </span>
          </div>
        </Link>
      </div>
    </aside>
  );
};
