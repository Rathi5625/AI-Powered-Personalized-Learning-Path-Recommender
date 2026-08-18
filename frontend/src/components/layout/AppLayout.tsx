import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/career-selection', label: 'Career' },
  { to: '/skill-gap', label: 'Skill Gap' },
  { to: '/recommendations', label: 'Recommendations' },
  { to: '/learning-path', label: 'Learning Path' },
  { to: '/progress', label: 'Progress' },
  { to: '/adaptive-learning', label: 'Adaptive' },
  { to: '/profile', label: 'Profile' },
];

export function AppLayout() {
  const { session, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[#000000] text-[#FFFFFF]">
      <header className="sticky top-0 z-40 border-b border-[#111111] bg-[#000000]/80 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4">
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="font-display text-xl font-bold text-[#38BDF8]"
          >
            LearnPath <span className="text-[#FFFFFF]">AI</span>
          </button>
          <nav className="hidden flex-wrap items-center gap-1 lg:flex">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `rounded-lg px-3 py-2 text-sm transition ${
                    isActive ? 'bg-[#38BDF8] text-[#000000]' : 'text-[#A1A1AA] hover:bg-[#111111] hover:text-[#FFFFFF]'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-[#A1A1AA] md:inline">{session?.user.name}</span>
            <button
              type="button"
              onClick={logout}
              className="rounded-lg border border-[#111111] px-3 py-2 text-sm text-[#A1A1AA] hover:bg-[#111111] hover:text-[#FFFFFF]"
            >
              Logout
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
