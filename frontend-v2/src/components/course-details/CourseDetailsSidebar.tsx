import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Route,
  Compass,
  Award,
  CheckSquare,
  Rocket,
  Bot,
  BarChart3,
  HelpCircle,
  LogOut,
  Sparkles,
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
  { name: 'Projects', href: '/projects', icon: Rocket },
  { name: 'AI Mentor', href: '/ai-mentor', icon: Bot },
  { name: 'Progress', href: '/progress', icon: BarChart3 },
];

interface CourseDetailsSidebarProps {
  onUpgrade?: () => void;
}

export const CourseDetailsSidebar: React.FC<CourseDetailsSidebarProps> = ({ onUpgrade }) => {
  const location = useLocation();

  return (
    <aside className="hidden lg:flex flex-col w-[250px] shrink-0 h-screen sticky top-0 bg-white/70 backdrop-blur-2xl border-r border-gray-200/80 p-5 justify-between z-30 select-none text-left">
      {/* Top Brand Header & Navigation */}
      <div className="space-y-6">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-3 px-2 group cursor-pointer">
          <div className="w-10 h-10 rounded-2xl bg-[#8e4d2b] text-white flex items-center justify-center font-extrabold text-lg shadow-sm group-hover:scale-105 transition-transform">
            L
          </div>
          <div>
            <span className="font-extrabold text-xl tracking-tight text-[#0f1b32] block leading-none">
              LearnAI
            </span>
            <span className="text-[11px] font-semibold text-emerald-600 flex items-center gap-1 mt-1">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
              AI Mentor Active
            </span>
          </div>
        </Link>

        {/* Navigation Items */}
        <nav className="space-y-1">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            // Highlight Explore Courses as active on course details
            const isActive =
              item.href === '/explore-courses' ||
              location.pathname === item.href ||
              location.pathname.startsWith('/course-details');

            return (
              <Link
                key={item.name}
                to={item.href}
                className={`
                  flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs sm:text-sm font-medium transition-all duration-150
                  ${isActive
                    ? 'bg-[#ffdbcb]/60 text-[#8e4d2b] font-bold shadow-2xs'
                    : 'text-[#53433c] hover:bg-black/[0.03] hover:text-[#0f1b32]'
                  }
                `}
              >
                <Icon
                  className={`w-4 h-4 shrink-0 ${isActive ? 'text-[#8e4d2b]' : 'text-[#86736b]'
                    }`}
                />
                <span>{item.name}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      {/* Bottom Upgrade Card & Links */}
      <div className="space-y-3 pt-3">
        {/* Upgrade Card */}
        <div className="bg-white/80 border border-gray-100/90 rounded-2xl p-4 text-center shadow-xs">
          <div className="w-7 h-7 mx-auto rounded-full bg-[#FAF4F0] text-[#8e4d2b] flex items-center justify-center mb-1.5 shadow-2xs">
            <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          </div>
          <h4 className="text-xs font-bold text-[#0f1b32] mb-2.5">
            Upgrade to Pro
          </h4>
          <button
            type="button"
            onClick={onUpgrade}
            className="w-full py-1.5 px-3 rounded-xl bg-[#FAF4F0] hover:bg-[#F2DACB]/60 text-[#8e4d2b] text-xs font-bold transition-colors cursor-pointer border border-[#F2DACB]"
          >
            Upgrade
          </button>
        </div>

        {/* Secondary Links */}
        <div className="space-y-1">
          <Link
            to="/help"
            className="flex items-center gap-3 px-3.5 py-2 rounded-xl text-xs font-medium text-[#53433c] hover:text-[#0f1b32] hover:bg-black/[0.02] transition-colors"
          >
            <HelpCircle className="w-4 h-4 text-[#86736b]" />
            <span>Help</span>
          </Link>
          <Link
            to="/login"
            className="flex items-center gap-3 px-3.5 py-2 rounded-xl text-xs font-medium text-[#53433c] hover:text-[#0f1b32] hover:bg-black/[0.02] transition-colors"
          >
            <LogOut className="w-4 h-4 text-[#86736b]" />
            <span>Logout</span>
          </Link>
        </div>
      </div>
    </aside>
  );
};
