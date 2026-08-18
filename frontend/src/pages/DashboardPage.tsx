import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { ProgressOrbit } from '../components/three/ProgressOrbit';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { PageHeader } from '../components/ui/PageHeader';
import { StatCard } from '../components/ui/StatCard';
import { useAuth } from '../context/AuthContext';

export function DashboardPage() {
  const { session } = useAuth();
  const [gapScore, setGapScore] = useState<number | null>(null);
  const [recommendationCount, setRecommendationCount] = useState(0);
  const [progressAvg, setProgressAvg] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const userId = session?.user.id;
  const careerId = session?.careerId;

  useEffect(() => {
    if (!userId || !careerId) return;
    setLoading(true);
    Promise.all([
      api.getSkillGaps(userId, careerId),
      api.getRecommendations(userId, careerId, 5),
      api.getUserProgress(userId),
    ])
      .then(([gaps, recs, progress]) => {
        setGapScore(Math.round(gaps.overallGapScore * 100));
        setRecommendationCount(recs.recommendations.length);
        const avg =
          progress.length > 0
            ? progress.reduce((sum, item) => sum + Number(item.completionPercentage), 0) / progress.length
            : 0;
        setProgressAvg(Math.round(avg));
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load dashboard'))
      .finally(() => setLoading(false));
  }, [userId, careerId]);

  const quickLinks = useMemo(
    () => [
      { to: '/skill-gap', label: 'Analyze skill gaps', color: 'text-[#FFFFFF]' },
      { to: '/recommendations', label: 'View recommendations', color: 'text-[#FFFFFF]' },
      { to: '/learning-path', label: 'Generate learning path', color: 'text-[#FFFFFF]' },
      { to: '/adaptive-learning', label: 'Adapt your path', color: 'text-[#FFFFFF]' },
    ],
    [],
  );

  if (loading) return <LoadingSpinner label="Loading dashboard..." />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div>
      <PageHeader
        eyebrow="Dashboard"
        title={`Welcome back, ${session?.user.name}`}
        description={`Target career: ${session?.careerName ?? 'Not selected'}`}
      />
      <div className="grid gap-6 lg:grid-cols-[1.2fr_1fr]">
        <div className="grid gap-4 sm:grid-cols-2">
          <StatCard label="Gap score" value={gapScore !== null ? `${gapScore}%` : '—'} accent="rose" />
          <StatCard label="Top recommendations" value={recommendationCount} accent="indigo" />
          <StatCard label="Avg. progress" value={`${progressAvg}%`} accent="cyan" />
          <StatCard
            label="Daily learning goal"
            value={`${session?.user.dailyLearningHours ?? 2}h`}
            accent="emerald"
          />
        </div>
        <div className="overflow-hidden rounded-3xl border border-white/10 bg-[#111111]">
          <ProgressOrbit completion={progressAvg} />
        </div>
      </div>
      <div className="mt-8 grid gap-4 md:grid-cols-2">
        {quickLinks.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className="rounded-2xl border border-white/10 bg-[#111111] p-5 transition hover:border-white/10 hover:bg-white/5"
          >
            <span className={`font-semibold ${link.color}`}>{link.label}</span>
            <p className="mt-1 text-sm text-[#A1A1AA]/70">Open page →</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
