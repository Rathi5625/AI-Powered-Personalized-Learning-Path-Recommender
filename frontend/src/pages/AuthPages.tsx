import { type FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ParticleField } from '../components/three/ParticleField';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { useAuth } from '../context/AuthContext';
import type { ExperienceLevel, LearningStyle, PreferredContentType } from '../api/types';

const experienceLevels: ExperienceLevel[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];
const learningStyles: LearningStyle[] = ['PRACTICAL', 'THEORETICAL', 'VISUAL', 'AUDITORY', 'READING_WRITING'];
const contentTypes: PreferredContentType[] = ['VIDEO', 'ARTICLE', 'INTERACTIVE_EXERCISE', 'BOOK', 'PROJECT'];

export function LoginPage() {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      await login(email);
      const stored = localStorage.getItem('learningpath_session');
      const session = stored ? JSON.parse(stored) : null;
      if (session?.onboardingComplete && session?.careerId) {
        navigate('/dashboard');
      } else if (session?.onboardingComplete) {
        navigate('/career-selection');
      } else {
        navigate('/onboarding');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center bg-[#000000] px-4">
      <ParticleField />
      <div className="relative z-10 w-full max-w-md rounded-3xl border border-white/10 bg-[#111111]/90 p-8 backdrop-blur-xl">
        <h1 className="font-display text-3xl font-bold text-[#FFFFFF]">Welcome back</h1>
        <p className="mt-2 text-[#A1A1AA]/70">Sign in with the email you used to register.</p>
        <form onSubmit={handleSubmit} className="mt-8 space-y-4">
          <label className="block">
            <span className="mb-2 block text-sm text-[#A1A1AA]">Email</span>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF] outline-none focus:border-[#38BDF8]"
              placeholder="you@example.com"
            />
          </label>
          {error && <ErrorMessage message={error} />}
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-[#38BDF8] py-3 font-semibold text-[#000000] transition hover:bg-[#7DD3FC] disabled:opacity-60"
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <p className="mt-6 text-center text-sm text-[#A1A1AA]/70">
          New here?{' '}
          <Link to="/signup" className="text-[#FFFFFF] hover:underline">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  );
}

export function SignupPage() {
  const { signup, loading } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [careerGoal, setCareerGoal] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      await signup({ name, email, careerGoal: careerGoal || undefined });
      navigate('/onboarding');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Signup failed');
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center bg-[#000000] px-4">
      <ParticleField />
      <div className="relative z-10 w-full max-w-md rounded-3xl border border-white/10 bg-[#111111]/90 p-8 backdrop-blur-xl">
        <h1 className="font-display text-3xl font-bold text-[#FFFFFF]">Create your account</h1>
        <p className="mt-2 text-[#A1A1AA]/70">Start your personalized learning journey.</p>
        <form onSubmit={handleSubmit} className="mt-8 space-y-4">
          <label className="block">
            <span className="mb-2 block text-sm text-[#A1A1AA]">Full name</span>
            <input
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF] outline-none focus:border-[#38BDF8]"
            />
          </label>
          <label className="block">
            <span className="mb-2 block text-sm text-[#A1A1AA]">Email</span>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF] outline-none focus:border-[#38BDF8]"
            />
          </label>
          <label className="block">
            <span className="mb-2 block text-sm text-[#A1A1AA]">Career goal (optional)</span>
            <input
              value={careerGoal}
              onChange={(e) => setCareerGoal(e.target.value)}
              className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF] outline-none focus:border-[#38BDF8]"
              placeholder="e.g. Backend Developer"
            />
          </label>
          {error && <ErrorMessage message={error} />}
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-[#38BDF8] py-3 font-semibold text-[#000000] transition hover:bg-[#7DD3FC] disabled:opacity-60"
          >
            {loading ? 'Creating account...' : 'Sign Up'}
          </button>
        </form>
        <p className="mt-6 text-center text-sm text-[#A1A1AA]/70">
          Already have an account?{' '}
          <Link to="/login" className="text-[#FFFFFF] hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}

export function OnboardingPage() {
  const { session, completeOnboarding, loading } = useAuth();
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [experienceLevel, setExperienceLevel] = useState<ExperienceLevel>('BEGINNER');
  const [dailyLearningHours, setDailyLearningHours] = useState(2);
  const [learningStyle, setLearningStyle] = useState<LearningStyle>('PRACTICAL');
  const [preferredContentType, setPreferredContentType] = useState<PreferredContentType>('VIDEO');
  const [error, setError] = useState('');

  if (!session) return null;

  async function finish() {
    setError('');
    try {
      await completeOnboarding({
        experienceLevel,
        dailyLearningHours,
        learningStyle,
        preferredContentType,
      });
      navigate('/career-selection');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save preferences');
    }
  }

  const steps = [
    {
      title: 'Experience level',
      body: (
        <div className="grid gap-3 sm:grid-cols-2">
          {experienceLevels.map((level) => (
            <button
              key={level}
              type="button"
              onClick={() => setExperienceLevel(level)}
              className={`rounded-xl border px-4 py-3 text-left transition ${
                experienceLevel === level
                  ? 'border-[#38BDF8] bg-[#38BDF8]/10 text-[#38BDF8]'
                  : 'border-white/10 text-[#A1A1AA] hover:bg-white/5'
              }`}
            >
              {level.replace('_', ' ')}
            </button>
          ))}
        </div>
      ),
    },
    {
      title: 'Daily learning time',
      body: (
        <div>
          <input
            type="range"
            min={1}
            max={8}
            value={dailyLearningHours}
            onChange={(e) => setDailyLearningHours(Number(e.target.value))}
            className="w-full accent-[#38BDF8]"
          />
          <p className="mt-3 text-2xl font-bold text-[#FFFFFF]">{dailyLearningHours} hours / day</p>
        </div>
      ),
    },
    {
      title: 'Learning style & content',
      body: (
        <div className="space-y-6">
          <div>
            <p className="mb-3 text-sm text-[#A1A1AA]/70">Preferred learning style</p>
            <div className="flex flex-wrap gap-2">
              {learningStyles.map((style) => (
                <button
                  key={style}
                  type="button"
                  onClick={() => setLearningStyle(style)}
                  className={`rounded-full border px-4 py-2 text-sm ${
                    learningStyle === style
                      ? 'border-[#38BDF8] bg-[#38BDF8]/10 text-[#38BDF8]'
                      : 'border-white/10 text-[#A1A1AA]'
                  }`}
                >
                  {style.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
          <div>
            <p className="mb-3 text-sm text-[#A1A1AA]/70">Preferred content type</p>
            <div className="flex flex-wrap gap-2">
              {contentTypes.map((type) => (
                <button
                  key={type}
                  type="button"
                  onClick={() => setPreferredContentType(type)}
                  className={`rounded-full border px-4 py-2 text-sm ${
                    preferredContentType === type
                      ? 'border-[#38BDF8] bg-[#38BDF8]/10 text-[#38BDF8]'
                      : 'border-white/10 text-[#A1A1AA]'
                  }`}
                >
                  {type.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="relative min-h-screen bg-[#000000] px-4 py-10">
      <ParticleField className="absolute inset-0 -z-10 opacity-40" />
      <div className="mx-auto max-w-2xl rounded-3xl border border-white/10 bg-[#111111]/90 p-8 backdrop-blur-xl">
        <p className="text-sm uppercase tracking-[0.2em] text-[#FFFFFF]">Onboarding</p>
        <h1 className="mt-2 font-display text-3xl font-bold text-[#FFFFFF]">
          Hi {session.user.name}, let&apos;s personalize your experience
        </h1>
        <div className="mt-8">
          <div className="mb-6 flex gap-2">
            {steps.map((_, index) => (
              <div
                key={index}
                className={`h-1 flex-1 rounded-full ${index <= step ? 'bg-[#38BDF8]' : 'bg-white/10'}`}
              />
            ))}
          </div>
          <h2 className="text-xl font-semibold text-[#FFFFFF]">{steps[step]?.title}</h2>
          <div className="mt-6">{steps[step]?.body}</div>
          {error && <div className="mt-4"><ErrorMessage message={error} /></div>}
          <div className="mt-8 flex justify-between">
            <button
              type="button"
              disabled={step === 0}
              onClick={() => setStep((s) => s - 1)}
              className="rounded-xl border border-white/10 px-5 py-2 text-[#A1A1AA] disabled:opacity-40"
            >
              Back
            </button>
            {step < steps.length - 1 ? (
              <button
                type="button"
                onClick={() => setStep((s) => s + 1)}
                className="rounded-xl bg-[#38BDF8] px-5 py-2 font-semibold text-[#000000] hover:bg-[#7DD3FC] transition"
              >
                Continue
              </button>
            ) : (
              <button
                type="button"
                onClick={finish}
                disabled={loading}
                className="rounded-xl bg-[#38BDF8] px-5 py-2 font-semibold text-[#000000] hover:bg-[#7DD3FC] transition disabled:opacity-60"
              >
                {loading ? 'Saving...' : 'Finish setup'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
