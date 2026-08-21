import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  ClipboardList,
  Calendar,
  Settings,
  HelpCircle,
  LogOut,
  Sparkles,
} from 'lucide-react';

interface SkillsSidebarProps {
  onNewCourse?: () => void;
  onLogout?: () => void;
}

export const SkillsSidebar: React.FC<SkillsSidebarProps> = ({
  onNewCourse,
  onLogout,
}) => {
  const location = useLocation();

  const NAV_ITEMS = [
    { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
    { name: 'Skills', href: '/skills', icon: ClipboardList },
    { name: 'Calendar', href: '/calendar', icon: Calendar },
    { name: 'Settings', href: '/settings', icon: Settings },
    { name: 'Support', href: '/help', icon: HelpCircle },
  ];

  return (
    <aside className="hidden lg:flex flex-col w-[250px] shrink-0 h-screen sticky top-0 bg-white/70 backdrop-blur-2xl border-r border-gray-200/80 p-5 justify-between z-30 select-none text-left">
      {/* Top Section: Brand & Navigation */}
      <div className="space-y-7">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-3 px-2 group cursor-pointer">
          <div className="w-10 h-10 rounded-full bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b] shadow-2xs group-hover:scale-105 transition-transform">
            <Sparkles className="w-5 h-5 text-[#8e4d2b]" />
          </div>
          <div>
            <span className="font-extrabold text-xl tracking-tight text-[#0f1b32] block leading-none">
              LearnAI
            </span>
            <span className="text-[11px] font-semibold text-gray-400 block mt-1">
              Premium Learning
            </span>
          </div>
        </Link>

        {/* Navigation Items */}
        <nav className="space-y-1.5 -mr-5">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive =
              location.pathname === item.href ||
              (item.href === '/skills' && location.pathname.startsWith('/skills'));

            return (
              <Link
                key={item.name}
                to={item.href}
                className={`
                  flex items-center gap-3.5 px-4 py-3 text-xs sm:text-sm font-medium transition-all duration-150
                  ${
                    isActive
                      ? 'bg-[#FAF4F0] text-[#8e4d2b] font-bold border-l-4 border-[#8e4d2b] rounded-r-2xl shadow-2xs'
                      : 'text-[#53433c] hover:bg-black/[0.03] hover:text-[#0f1b32] rounded-xl mr-5'
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

      {/* Bottom Section: New Course Button & Logout */}
      <div className="space-y-3 pt-4">
        {/* Large Terracotta New Course Button */}
        <button
          type="button"
          onClick={onNewCourse}
          className="w-full py-3.5 px-4 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs sm:text-sm font-bold transition-all shadow-sm hover:shadow-md cursor-pointer text-center active:scale-[0.98]"
        >
          New Course
        </button>

        {/* Logout Link */}
        <button
          type="button"
          onClick={onLogout}
          className="w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-medium text-[#53433c] hover:text-[#ba1a1a] hover:bg-red-50/50 transition-colors cursor-pointer text-left"
        >
          <LogOut className="w-4 h-4 text-[#86736b]" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};
