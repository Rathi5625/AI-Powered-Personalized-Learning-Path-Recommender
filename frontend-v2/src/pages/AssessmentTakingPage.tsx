import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle2, XCircle, Sparkles, ArrowRight, Brain, ShieldCheck, Clock } from 'lucide-react';
import { AssessmentHeader } from '../components/assessment/AssessmentHeader';

import { ExitAssessmentModal } from '../components/assessment/ExitAssessmentModal';
import api from '../api/client';
import {
  AdaptiveSessionStartResponse,
  AdaptiveNextQuestionResponse,
  AdaptiveAnswerSubmissionResult,
  AdaptiveSessionResultResponse,
} from '../api/types';

export const AssessmentTakingPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const assessmentId = searchParams.get('id');

  // Session state
  const [session, setSession] = useState<AdaptiveSessionStartResponse | null>(null);
  const [currentQuestion, setCurrentQuestion] = useState<AdaptiveNextQuestionResponse | null>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [questionStartTime, setQuestionStartTime] = useState<number>(Date.now());
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [lastResult, setLastResult] = useState<AdaptiveAnswerSubmissionResult | null>(null);
  const [sessionResult, setSessionResult] = useState<AdaptiveSessionResultResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [showExitModal, setShowExitModal] = useState<boolean>(false);

  // Initialize Adaptive Session
  useEffect(() => {
    const initSession = async () => {
      if (!assessmentId) {
        navigate('/assessments');
        return;
      }
      try {
        setLoading(true);
        const startResp = await api.startAdaptiveSession(assessmentId);
        setSession(startResp);

        const firstQ = await api.getAdaptiveNextQuestion(startResp.sessionId);
        setCurrentQuestion(firstQ);
        setQuestionStartTime(Date.now());
      } catch (err) {
        console.error('Failed to start adaptive session:', err);
      } finally {
        setLoading(false);
      }
    };
    initSession();
  }, [assessmentId, navigate]);

  const handleSubmitAnswer = async () => {
    if (!session || !currentQuestion || !currentQuestion.questionId || !selectedOption || isSubmitting) {
      return;
    }

    try {
      setIsSubmitting(true);
      const elapsedSeconds = Math.max(1, Math.round((Date.now() - questionStartTime) / 1000));

      const answerResult = await api.submitAdaptiveSessionAnswer(
        session.sessionId,
        currentQuestion.questionId,
        selectedOption,
        elapsedSeconds
      );

      setLastResult(answerResult);

      if (answerResult.sessionComplete) {
        // Load final rich result
        const finalResult = await api.getAdaptiveSessionResult(session.sessionId);
        setSessionResult(finalResult);
      } else {
        // Load next CAT question after brief feedback
        setTimeout(async () => {
          setLastResult(null);
          setSelectedOption(null);
          const nextQ = await api.getAdaptiveNextQuestion(session.sessionId);
          if (nextQ.isTerminated) {
            const finalResult = await api.getAdaptiveSessionResult(session.sessionId);
            setSessionResult(finalResult);
          } else {
            setCurrentQuestion(nextQ);
            setQuestionStartTime(Date.now());
          }
          setIsSubmitting(false);
        }, 1200);
      }
    } catch (err) {
      console.error('Failed to submit answer:', err);
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#f9f9ff] flex flex-col items-center justify-center p-6 text-center">
        <Sparkles className="w-8 h-8 animate-pulse text-[#8e4d2b] mb-3" />
        <h2 className="text-base font-bold text-[#0f1b32]">Calibrating Adaptive Assessment Engine...</h2>
        <p className="text-xs text-gray-400 mt-1">Evaluating current Bayesian Knowledge Tracing priors.</p>
      </div>
    );
  }

  // Render Post-Assessment Rich Results
  if (sessionResult) {
    return (
      <div className="min-h-screen bg-[#f9f9ff] text-[#0f1b32] pt-20 pb-16 px-4 sm:px-8 flex flex-col items-center justify-start">
        <div className="w-full max-w-2xl bg-white rounded-3xl p-6 sm:p-8 shadow-xl border border-gray-100 space-y-6 text-left">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-gray-100 pb-4">
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-xl font-extrabold text-[#0f1b32]">{sessionResult.assessmentTitle}</h1>
                <span className="px-2.5 py-0.5 rounded-full text-[10px] font-extrabold bg-[#FAF4F0] text-[#8e4d2b] border border-[#F2DACB]">
                  Adaptive Assessment
                </span>
              </div>
              <p className="text-xs text-gray-400 mt-1">Computerized Adaptive Testing Complete</p>
            </div>
            <div className="text-right">
              <span className="text-3xl font-black text-[#8e4d2b]">{Math.round(sessionResult.overallScore)}%</span>
              <span className="text-[10px] text-gray-400 block font-bold">Accuracy</span>
            </div>
          </div>

          {/* Metrics Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 text-left">
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">Mastery</span>
              <span className="text-sm font-extrabold text-[#0f1b32]">{sessionResult.masteryEstimate}%</span>
            </div>
            <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 text-left">
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">Confidence</span>
              <span className="text-sm font-extrabold text-emerald-600 flex items-center gap-1">
                <ShieldCheck className="w-3.5 h-3.5" />
                {sessionResult.confidenceLevel}
              </span>
            </div>
            <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 text-left">
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">Avg Time</span>
              <span className="text-sm font-extrabold text-[#0f1b32]">{sessionResult.averageResponseTimeSeconds}s</span>
            </div>
            <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 text-left">
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">Tier Reached</span>
              <span className="text-sm font-extrabold text-[#8e4d2b]">{sessionResult.difficultyReached}</span>
            </div>
          </div>

          {/* Skill Breakdown */}
          <div className="space-y-3">
            <h3 className="text-xs font-extrabold text-gray-400 uppercase tracking-wider">Concept Diagnostic</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="p-3.5 rounded-2xl bg-emerald-50/60 border border-emerald-100">
                <span className="text-xs font-bold text-emerald-800 block mb-1">Strong Concepts</span>
                <div className="flex flex-wrap gap-1.5">
                  {sessionResult.strongSkills.length > 0 ? (
                    sessionResult.strongSkills.map((s) => (
                      <span key={s} className="px-2 py-0.5 bg-emerald-100/80 text-emerald-900 rounded-lg text-[10px] font-bold">
                        {s}
                      </span>
                    ))
                  ) : (
                    <span className="text-[10px] text-gray-400">Continue practice to build strong concepts</span>
                  )}
                </div>
              </div>
              <div className="p-3.5 rounded-2xl bg-amber-50/60 border border-amber-100">
                <span className="text-xs font-bold text-amber-800 block mb-1">Developing / Weak</span>
                <div className="flex flex-wrap gap-1.5">
                  {sessionResult.weakSkills.length > 0 ? (
                    sessionResult.weakSkills.map((s) => (
                      <span key={s} className="px-2 py-0.5 bg-amber-100/80 text-amber-900 rounded-lg text-[10px] font-bold">
                        {s}
                      </span>
                    ))
                  ) : (
                    <span className="text-[10px] text-emerald-700 font-bold">No critical knowledge gaps detected!</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Behavioral Insight Card */}
          {sessionResult.behaviorCategory && (
            <div className="p-4 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB] space-y-1.5">
              <div className="flex items-center gap-1.5 text-xs font-bold text-[#8e4d2b]">
                <Brain className="w-3.5 h-3.5" />
                <span>Learner Profile: {sessionResult.behaviorCategory.replace(/_/g, ' ')}</span>
              </div>
              <p className="text-xs text-gray-600 leading-relaxed">
                {sessionResult.recommendedNextAction}
              </p>
            </div>
          )}

          {/* CTAs */}
          <div className="flex flex-col sm:flex-row items-center gap-3 pt-2">
            <button
              type="button"
              onClick={() => navigate('/learning-path')}
              className="w-full sm:flex-1 py-3 rounded-2xl bg-[#8e4d2b] text-white text-xs font-extrabold hover:bg-[#783e21] shadow-sm flex items-center justify-center gap-2 transition-colors cursor-pointer"
            >
              <span>Continue Learning Path</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
            <button
              type="button"
              onClick={() => navigate('/assessments')}
              className="w-full sm:w-auto px-5 py-3 rounded-2xl bg-gray-100 text-gray-700 text-xs font-bold hover:bg-gray-200 transition-colors cursor-pointer"
            >
              All Assessments
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Active Adaptive Question View
  return (
    <div className="min-h-screen bg-[#f9f9ff] text-[#0f1b32] relative pt-24 pb-16 px-4 sm:px-8 flex flex-col items-center justify-start">
      <AssessmentHeader
        title={session?.assessmentTitle || 'Adaptive Skill Assessment'}
        onExitAssessment={() => setShowExitModal(true)}
      />

      <main className="relative z-10 w-full max-w-2xl mx-auto space-y-6">
        {/* Dynamic Meta Bar */}
        <div className="bg-white/80 backdrop-blur-xl rounded-2xl p-4 border border-gray-100 shadow-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-1 rounded-xl text-xs font-extrabold bg-[#FAF4F0] text-[#8e4d2b] border border-[#F2DACB]">
              Question {currentQuestion?.questionNumber || 1} of ~{currentQuestion?.totalQuestionsEstimated || 10}
            </span>
            <span
              className={`px-2 py-0.5 rounded-lg text-[10px] font-extrabold uppercase ${
                currentQuestion?.difficulty === 'ADVANCED'
                  ? 'bg-purple-50 text-purple-700 border border-purple-200'
                  : currentQuestion?.difficulty === 'INTERMEDIATE'
                  ? 'bg-blue-50 text-blue-700 border border-blue-200'
                  : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
              }`}
            >
              {currentQuestion?.difficulty || 'BEGINNER'}
            </span>
          </div>

          <div className="flex items-center gap-1 text-xs text-gray-400 font-mono">
            <Clock className="w-3.5 h-3.5 text-[#8e4d2b]" />
            <span>CAT Engine Active</span>
          </div>
        </div>

        {/* Question Card */}
        <div className="bg-white rounded-3xl p-6 sm:p-8 shadow-sm border border-gray-100 text-left space-y-5">
          <h2 className="text-base sm:text-lg font-bold text-[#0f1b32] leading-snug">
            {currentQuestion?.questionText}
          </h2>

          {/* Option Choices */}
          <div className="space-y-2.5">
            {currentQuestion?.options && currentQuestion.options.length > 0 ? (
              currentQuestion.options.map((opt, idx) => {
                const letter = String.fromCharCode(65 + idx);
                const isSelected = selectedOption === opt;
                return (
                  <button
                    key={opt}
                    type="button"
                    onClick={() => setSelectedOption(opt)}
                    className={`w-full p-4 rounded-2xl border text-xs text-left font-medium transition-all flex items-center gap-3 cursor-pointer ${
                      isSelected
                        ? 'border-[#8e4d2b] bg-[#FAF4F0] text-[#8e4d2b] shadow-xs'
                        : 'border-gray-100 hover:border-gray-200 bg-white text-gray-700'
                    }`}
                  >
                    <span
                      className={`w-6 h-6 rounded-lg flex items-center justify-center font-bold text-[10px] ${
                        isSelected ? 'bg-[#8e4d2b] text-white' : 'bg-gray-100 text-gray-500'
                      }`}
                    >
                      {letter}
                    </span>
                    <span>{opt}</span>
                  </button>
                );
              })
            ) : (
              <div className="p-4 rounded-2xl bg-gray-50 text-xs text-gray-400">Loading options...</div>
            )}
          </div>

          {/* Feedback Toast */}
          <AnimatePresence>
            {lastResult && (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 10 }}
                className={`p-3.5 rounded-2xl text-xs font-bold flex items-center gap-2 ${
                  lastResult.correct
                    ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                    : 'bg-rose-50 text-rose-800 border border-rose-200'
                }`}
              >
                {lastResult.correct ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                ) : (
                  <XCircle className="w-4 h-4 text-rose-600 shrink-0" />
                )}
                <span>{lastResult.feedback}</span>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Submit Action */}
          <div className="pt-2 flex justify-end">
            <button
              type="button"
              disabled={!selectedOption || isSubmitting}
              onClick={handleSubmitAnswer}
              className="px-6 py-3 rounded-2xl bg-[#8e4d2b] text-white text-xs font-extrabold hover:bg-[#783e21] shadow-xs flex items-center gap-2 transition-colors cursor-pointer disabled:opacity-50"
            >
              <span>{isSubmitting ? 'Evaluating...' : 'Confirm Answer'}</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </main>

      <ExitAssessmentModal
        isOpen={showExitModal}
        onContinue={() => setShowExitModal(false)}
        onConfirmExit={() => navigate('/assessments')}
      />
    </div>
  );
};
