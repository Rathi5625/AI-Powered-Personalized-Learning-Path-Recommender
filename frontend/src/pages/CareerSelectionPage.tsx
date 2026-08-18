import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import type { Career } from '../api/types';
import { ParticleField } from '../components/three/ParticleField';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { PageHeader } from '../components/ui/PageHeader';
import { useAuth } from '../context/AuthContext';

export function CareerSelectionPage() {
  const { session, setCareer } = useAuth();
  const navigate = useNavigate();
  const [careers, setCareers] = useState<Career[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedId, setSelectedId] = useState(session?.careerId ?? '');

  useEffect(() => {
    api
      .getCareers()
      .then((page) => setCareers(page.content))
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load careers'))
      .finally(() => setLoading(false));
  }, []);

  function handleContinue() {
    const career = careers.find((item) => item.id === selectedId);
    if (!career) return;
    setCareer(career);
    navigate('/dashboard');
  }

  return (
    <div className="relative">
      <ParticleField className="pointer-events-none absolute inset-x-0 top-0 h-64 opacity-30" />
      <PageHeader
        eyebrow="Career Selection"
        title="Choose your target career"
        description="Pick the role you want to pursue. We'll analyze skill gaps and build your learning path around it."
      />
      {loading && <LoadingSpinner label="Loading careers..." />}
      {error && <ErrorMessage message={error} />}
      {!loading && !error && (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {careers.map((career) => (
              <button
                key={career.id}
                type="button"
                onClick={() => setSelectedId(career.id)}
                className={`rounded-2xl border p-5 text-left transition ${
                  selectedId === career.id
                    ? 'border-[#38BDF8] bg-[#38BDF8]/10 shadow-md shadow-white/5'
                    : 'border-white/10 bg-[#111111] hover:border-white/20'
                }`}
              >
                <p className="text-xs uppercase tracking-wider text-[#FFFFFF]">{career.category}</p>
                <h3 className="mt-2 text-xl font-semibold text-[#FFFFFF]">{career.name}</h3>
                <p className="mt-2 text-sm text-[#A1A1AA]/70">{career.description}</p>
              </button>
            ))}
          </div>
          <div className="mt-8">
            <button
               type="button"
               disabled={!selectedId}
               onClick={handleContinue}
               className="rounded-xl bg-[#38BDF8] px-6 py-3 font-semibold text-[#000000] disabled:opacity-50 hover:bg-[#7DD3FC] transition"
             >
               Continue to Dashboard
             </button>
          </div>
        </>
      )}
    </div>
  );
}
