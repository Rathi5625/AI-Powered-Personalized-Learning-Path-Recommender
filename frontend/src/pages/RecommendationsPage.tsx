import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { PageHeader } from '../components/ui/PageHeader';
import { useAuth } from '../context/AuthContext';

export function RecommendationsPage() {
  const { session } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [data, setData] = useState<Awaited<ReturnType<typeof api.getRecommendations>> | null>(null);

  useEffect(() => {
    if (!session?.user.id || !session.careerId) return;
    api
      .getRecommendations(session.user.id, session.careerId, 12)
      .then(setData)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load recommendations'))
      .finally(() => setLoading(false));
  }, [session?.user.id, session?.careerId]);

  if (loading) return <LoadingSpinner label="Fetching recommendations..." />;
  if (error) return <ErrorMessage message={error} />;
  if (!data) return null;

  return (
    <div>
      <PageHeader
        eyebrow="Recommendations"
        title="Courses ranked for you"
        description={`${data.totalCandidateCourses} candidates evaluated for ${data.careerName}.`}
      />
      <div className="grid gap-4">
        {data.recommendations.map((course) => (
          <article
            key={course.courseId}
            className="rounded-2xl border border-white/10 bg-[#111111] p-5"
          >
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="text-sm text-[#FFFFFF]">#{course.rank} · {course.provider}</p>
                <h3 className="mt-1 text-xl font-semibold text-[#FFFFFF]">{course.courseTitle}</h3>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold text-[#FFFFFF]">{course.finalScore.toFixed(2)}</p>
                <p className="text-xs text-[#A1A1AA]/70">final score</p>
              </div>
            </div>
            <p className="mt-3 text-sm text-[#A1A1AA]">{course.explanation}</p>
            <div className="mt-4 flex flex-wrap gap-2">
              {course.gapSkillsAddressed.map((skill) => (
                <span key={skill} className="rounded-full bg-white/10 px-3 py-1 text-xs text-[#A1A1AA]">
                  {skill}
                </span>
              ))}
            </div>
            <div className="mt-4 flex flex-wrap items-center gap-4 text-sm text-[#A1A1AA]/70">
              <span>{course.difficulty}</span>
              <span>{course.isFree ? 'Free' : `$${course.price}`}</span>
              <span>Rating {course.rating}</span>
              {course.url && (
                <a href={course.url} target="_blank" rel="noreferrer" className="text-[#38BDF8] hover:underline">
                  Open course
                </a>
              )}
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
