// Core Domain Enums
export type Role = 'USER' | 'ADMIN';
export type ExperienceLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type LearningStyle = 'VISUAL' | 'AUDITORY' | 'PRACTICAL' | 'THEORETICAL';
export type PreferredContentType = 'VIDEO' | 'ARTICLE' | 'INTERACTIVE_EXERCISE' | 'PROJECT' | 'BOOK';
export type CourseDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type CourseType = 'VIDEO' | 'INTERACTIVE' | 'TEXT' | 'PROJECT_BASED' | 'SPECIALIZATION';
export type GapType = 'NO_GAP' | 'PARTIAL_GAP' | 'FULL_GAP';
export type PriorityLevel = 'CRITICAL' | 'IMPORTANT' | 'OPTIONAL';
export type LearningPathStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
export type ProgressStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'PAUSED';
export type ProjectStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED';
export type NotificationCategory = 'LEARNING' | 'AI' | 'ASSESSMENTS' | 'PROJECTS' | 'SYSTEM';
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type ProficiencyLevel = 'NOVICE' | 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

// Authentication DTOs
export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  targetCareer?: string;
  experienceLevel?: ExperienceLevel;
  dailyLearningHours?: number;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
}

export interface UserSummary {
  id: string;
  name: string;
  email: string;
  role: Role;
  emailVerified?: boolean;
  onboardingCompleted?: boolean;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

export interface SignupResponse {
  userId: string;
  name: string;
  email: string;
  message: string;
}

export type OtpPurpose = 'EMAIL_VERIFICATION' | 'PASSWORD_RESET';

export interface VerifyEmailOtpRequest {
  email: string;
  otp: string;
}

export interface ResendOtpRequest {
  email: string;
  purpose: OtpPurpose;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface VerifyResetOtpRequest {
  email: string;
  otp: string;
}

export interface VerifyResetOtpResponse {
  resetToken: string;
  email: string;
  message: string;
}

export interface ResetPasswordRequest {
  resetToken: string;
  newPassword: string;
  confirmPassword: string;
}

export interface GenericApiResponse {
  success: boolean;
  message: string;
}

export interface AuthenticatedUserResponse {
  id: string;
  name: string;
  email: string;
  role: Role;
  targetCareer: string | null;
  experienceLevel: ExperienceLevel | null;
  dailyLearningHours: number | null;
  learningStyle: LearningStyle | null;
  preferredContentType: PreferredContentType | null;
  emailVerified?: boolean;
  onboardingCompleted?: boolean;
}

// User Profile DTOs
export interface UserSkill {
  id: string;
  userId: string;
  skillId: string;
  skillName: string;
  skillCategory?: string;
  proficiencyLevel: ProficiencyLevel;
  confidence?: number;
  source?: string;
  isVerified: boolean;
  lastAssessedDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserProfile {
  id: string;
  fullName: string;
  email: string;
  role: Role;
  emailVerified: boolean;
  location?: string;
  education?: string;
  graduationYear?: number;
  currentGoal?: string;
  personalObjective?: string;
  bio?: string;
  avatarUrl?: string;
  githubUrl?: string;
  linkedinUrl?: string;
  portfolioUrl?: string;
  targetCareer?: string;
  experienceLevel?: ExperienceLevel;
  dailyLearningHours?: number;
  weeklyCommitmentHours?: number;
  preferredLearningPace?: string;
  availableDays?: string;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
  profileCompletionPercentage: number;
  skills: UserSkill[];
  createdAt: string;
  updatedAt: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  location?: string;
  education?: string;
  graduationYear?: number;
  currentGoal?: string;
  personalObjective?: string;
  bio?: string;
  avatarUrl?: string;
  githubUrl?: string;
  linkedinUrl?: string;
  portfolioUrl?: string;
  targetCareer?: string;
  experienceLevel?: ExperienceLevel;
  dailyLearningHours?: number;
  weeklyCommitmentHours?: number;
  preferredLearningPace?: string;
  availableDays?: string;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
}

export interface UserSkillRequest {
  skillId: string;
  proficiencyLevel: ProficiencyLevel;
  confidence?: number;
  source?: string;
  isVerified?: boolean;
}

// Course DTOs
export interface Course {
  id: string;
  title: string;
  description: string;
  provider: string;
  url: string;
  difficulty: CourseDifficulty;
  type: CourseType;
  durationMinutes: number | null;
  rating: number | null;
  price: number | null;
  isFree: boolean;
  language: string;
  createdAt: string;
  updatedAt: string;
}

export interface CourseEnrollment {
  id: string;
  userId: string;
  courseId: string;
  courseTitle: string;
  status: ProgressStatus;
  progressPercentage: number;
  lastLessonCompleted?: number;
  totalLessons?: number;
  lastAccessedAt?: string;
  enrolledAt: string;
}

export interface CourseProgressUpdate {
  status: ProgressStatus;
  progressPercentage: number;
  lastLessonCompleted?: number;
  timeSpentMinutes?: number;
}

// Assessment DTOs
export interface AssessmentQuestion {
  id: string;
  questionText: string;
  questionType: string;
  difficulty: CourseDifficulty;
  options: string[];
  points: number;
}

export interface Assessment {
  id: string;
  title: string;
  description: string;
  skillId: string;
  skillName: string;
  passingScore: number;
  questionCount: number;
  estimatedMinutes: number;
  questions?: AssessmentQuestion[];
}

export interface AssessmentSubmissionRequest {
  answers: Record<string, string>; // questionId -> selectedOption
  timeSpentSeconds?: number;
}

export interface AssessmentResult {
  id: string;
  assessmentId: string;
  assessmentTitle: string;
  skillName: string;
  score: number;
  passed: boolean;
  evaluatedProficiency: ProficiencyLevel;
  totalQuestions: number;
  correctAnswers: number;
  timeSpentSeconds?: number;
  completedAt: string;
}

// Project DTOs
export interface Project {
  id: string;
  title: string;
  description: string;
  technologies: string[];
  difficulty: CourseDifficulty;
  estimatedHours: number;
  milestonesCount: number;
  repositoryTemplateUrl?: string;
  userStatus: ProjectStatus;
  userProgressPercentage: number;
  userStartedAt?: string;
  userCompletedAt?: string;
}

export interface UpdateUserProjectRequest {
  status?: ProjectStatus;
  progressPercentage?: number;
  completedMilestones?: number;
  submissionUrl?: string;
  notes?: string;
}

// Notification DTOs
export interface Notification {
  id: string;
  title: string;
  message: string;
  category: NotificationCategory;
  read: boolean;
  actionUrl?: string;
  createdAt: string;
}

// Support Ticket DTOs
export interface SupportTicket {
  id: string;
  category: string;
  subject: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSupportTicketRequest {
  category: string;
  subject: string;
  description: string;
  priority?: TicketPriority;
}

// Dashboard Aggregated DTO
export interface DashboardAggregated {
  userId: string;
  userName: string;
  targetCareer?: string;
  experienceLevel?: ExperienceLevel;
  profileCompletionPercentage: number;
  activeStreakDays: number;
  totalLearningHours: number;
  completedCoursesCount: number;
  inProgressCoursesCount: number;
  totalSkillsCount: number;
  unreadNotificationsCount: number;
  activeLearningPath: ActiveLearningPathResponse | null;
  enrolledCourses: CourseEnrollment[];
  topSkills: UserSkill[];
  recentAssessments: AssessmentResult[];
  activeProjects: Project[];
  recentNotifications: Notification[];
}

// Analytics DTOs
export interface WeeklyActivityPoint {
  day: string;
  hours: number;
  lessonsCompleted: number;
}

export interface SkillProgressItem {
  skillId: string;
  skillName: string;
  category: string;
  proficiencyScore: number;
  level: string;
}

export interface ProgressAnalytics {
  userId: string;
  totalLearningHours: number;
  streakDays: number;
  completedCoursesCount: number;
  totalEnrolledCourses: number;
  skillsMasteredCount: number;
  totalAssessmentsTaken: number;
  averageAssessmentScore: number;
  weeklyActivity: WeeklyActivityPoint[];
  skillProgressBreakdown: SkillProgressItem[];
  recentCourses: CourseEnrollment[];
  assessmentHistory: AssessmentResult[];
}

// Settings DTOs
export interface Settings {
  fullName: string;
  email: string;
  location?: string;
  themePreference: string;
  emailNotifications: boolean;
  pushNotifications: boolean;
  dailyLearningHours?: number;
  weeklyCommitmentHours?: number;
  preferredLearningPace?: string;
  availableDays?: string;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
}

export interface UpdateSettingsRequest {
  fullName?: string;
  location?: string;
  themePreference?: string;
  emailNotifications?: boolean;
  pushNotifications?: boolean;
  dailyLearningHours?: number;
  weeklyCommitmentHours?: number;
  preferredLearningPace?: string;
  availableDays?: string;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
}

// Onboarding DTO
export interface OnboardingCompleteRequest {
  targetCareer?: string;
  experienceLevel?: ExperienceLevel;
  selectedSkills?: string[];
  interests?: string[];
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
  preferredLearningPace?: string;
  weeklyCommitmentHours?: number;
  availableDays?: string;
  currentGoal?: string;
  personalObjective?: string;
}

// Career DTOs
export interface Career {
  id: string;
  name: string;
  description: string;
  category: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// Skill Gap DTOs
export interface SkillGapItem {
  skillId: string;
  skillName: string;
  userProficiency: string;
  requiredProficiency: string;
  gapType: GapType;
  priority: PriorityLevel;
  gapWeight: number;
}

export interface SkillGapAnalysis {
  userId: string;
  userName: string;
  careerId: string;
  careerTitle: string;
  totalRequiredSkills: number;
  acquiredSkills: number;
  partialGapSkills: number;
  fullGapSkills: number;
  gapScore: number;
  gaps: SkillGapItem[];
}

// Recommendation DTOs
export interface CourseRecommendation {
  rank: number;
  courseId: string;
  courseTitle: string;
  provider: string;
  url: string;
  difficulty: CourseDifficulty;
  type: CourseType;
  rating: number;
  price: number;
  isFree: boolean;
  ruleBasedScore: number;
  mlScore: number | null;
  finalScore: number;
  matchedSkills: string[];
  coveredGapSkills: string[];
  explanation: string;
}

export interface RecommendationSummary {
  userId: string;
  userName: string;
  careerId: string;
  careerTitle: string;
  isFallback: boolean;
  totalRecommendations: number;
  recommendations: CourseRecommendation[];
}

// Learning Path DTOs
export interface LearningPathCourseItem {
  courseId: string;
  courseTitle: string;
  provider: string;
  url: string;
  difficulty: CourseDifficulty;
  durationMinutes: number | null;
  rating: number | null;
  price: number | null;
  isFree: boolean;
  skills: string[];
  order: number;
  estimatedDuration: string;
  explanation: string;
  completed?: boolean;
}

export interface LearningPathPhase {
  phaseNumber: number;
  phaseTitle: string;
  phaseDescription: string;
  estimatedDuration: string;
  courses: LearningPathCourseItem[];
}

export interface PersonalizedLearningPath {
  success: boolean;
  userId: string;
  targetCareer: string;
  summary: string;
  phases: LearningPathPhase[];
  provider: string;
  model: string;
  error?: string | null;
}

export interface ActiveLearningPathResponse {
  id: string;
  userId: string;
  targetCareer: string;
  title: string;
  description: string;
  status: LearningPathStatus;
  currentPhase: number;
  totalPhases: number;
  totalCourses: number;
  phases: LearningPathPhase[];
  createdAt: string;
  updatedAt: string;
}

// Learning Progress DTOs
export interface LearningProgress {
  id: string;
  userId: string;
  courseId: string;
  status: ProgressStatus;
  completionPercentage: number;
  lastAccessedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface LearningProgressRequest {
  status: ProgressStatus;
  completionPercentage: number;
}

export interface UserProgressSummary {
  totalCoursesTracked: number;
  completedCourses: number;
  inProgressCourses: number;
  notStartedCourses: number;
  pausedCourses: number;
  overallCompletionRate: number;
}

export interface AdaptLearningPathResponse {
  adapted: boolean;
  changeReason: string;
  completedSkills: string[];
  remainingSkills: string[];
  path: PersonalizedLearningPath;
}

// Dashboard DTO
export interface DashboardResponse {
  user: UserProfile;
  activeLearningPath: ActiveLearningPathResponse | null;
  progressSummary: UserProgressSummary;
  skillGapSummary: SkillGapAnalysis | null;
  topRecommendations: CourseRecommendation[];
}

// AI Mentor DTOs
export interface AIMentorChatRequest {
  message: string;
  conversationId?: string;
}

export interface AIMentorChatResponse {
  messageId: string;
  conversationId: string;
  role: 'mentor';
  reply: string;
  topic: string;
  confidenceScore: number;
  recommendedAction?: string;
  suggestedFollowUps?: string[];
  recommendedResources?: {
    title: string;
    type: string;
    difficulty: string;
    url: string;
    matchScore: number;
  }[];
}

export interface AIMessageEntity {
  id: string;
  role: 'user' | 'mentor';
  content: string;
  topic?: string;
  recommendedAction?: string;
  createdAt?: string;
}

export interface RecommendationSummaryResponse {
  userId: string;
  careerId?: string;
  careerName?: string;
  generatedAt: string;
  totalRecommendations: number;
  recommendations: CourseRecommendation[];
}

// Phase 4 — Adaptive Learner Intelligence DTOs
export type MasteryLevel = 'NOT_STARTED' | 'DEVELOPING' | 'BASIC' | 'PROFICIENT' | 'MASTERED';

export interface SkillMasteryConceptItem {
  id: string;
  conceptName: string;
  skillId?: string;
  knowledgeProbability: number;
  masteryLevel: MasteryLevel;
  attempts: number;
  correctAttempts: number;
  confidenceScore: number;
  revisionRequired: boolean;
  lastAttemptAt?: string;
}

export interface LearnerMasterySummary {
  totalConceptsTracked: number;
  overallMasteryPercentage: number;
  masteredSkills: string[];
  developingSkills: string[];
  weakSkills: string[];
  revisionRequiredSkills: string[];
  conceptStates: SkillMasteryConceptItem[];
}

export interface LearnerBehaviorProfile {
  preferredDifficulty: string;
  learningVelocity: number;
  consistency: number;
  assessmentAccuracy: number;
  revisionNeed: number;
  preferredSessionLengthMinutes: number;
  strongestLearningFormat: string;
  activeStreakDays: number;
  totalSessionsRecorded: number;
  insufficientData: boolean;
  dataQualityStatus: 'COMPLETE' | 'PARTIAL' | 'INSUFFICIENT_DATA';
}

export interface DailyLearningPlanItem {
  id: string;
  title: string;
  type: 'LEARN' | 'PRACTICE' | 'ASSESSMENT' | 'REVISION';
  durationMinutes: number;
  difficulty: string;
  reason: string;
  actionUrl: string;
  priority: number;
}

export interface DailyLearningPlan {
  title: string;
  targetCareer: string;
  estimatedTotalMinutes: number;
  focusTopic: string;
  currentMasteryProbability: number;
  items: DailyLearningPlanItem[];
  generatedAt: string;
}

export interface AdaptiveQuestionResponse {
  questionId: string;
  assessmentId: string;
  assessmentTitle: string;
  skillName: string;
  questionText: string;
  questionType: string;
  options: string[];
  difficulty: CourseDifficulty;
  questionNumber: number;
  totalQuestions: number;
}

export interface AdaptiveAnswerResult {
  correct: boolean;
  feedback: string;
  updatedKnowledgeProbability: number;
  updatedMasteryLevel: MasteryLevel;
  nextRecommendedDifficulty: CourseDifficulty;
  revisionSuggested: boolean;
  explanation: string;
}

// Phase 5 — Real Personalized Learning Path Engine DTOs
export type LearningPathNodeType = 'COURSE' | 'TOPIC' | 'PRACTICE' | 'ASSESSMENT' | 'REVISION' | 'PROJECT' | 'MILESTONE';
export type LearningPathNodeStatus = 'LOCKED' | 'UNLOCKED' | 'IN_PROGRESS' | 'COMPLETED' | 'REVISION_REQUIRED';

export interface LearningPathNodeDto {
  id: string;
  nodeType: LearningPathNodeType;
  title: string;
  description?: string;
  skillName?: string;
  courseId?: string;
  courseTitle?: string;
  status: LearningPathNodeStatus;
  difficulty: CourseDifficulty;
  estimatedMinutes: number;
  masteryRequirement: number;
  currentMastery: number;
  recommendationScore: number;
  order: number;
  phaseNumber: number;
  phaseTitle: string;
  actionUrl: string;
  reason: string;
  unlockReason: string;
  prerequisites: string[];
  completed: boolean;
  completedAt?: string;
}

export interface LearningPathMilestoneDto {
  id: string;
  title: string;
  description: string;
  targetSkill: string;
  requiredMastery: number;
  currentMastery: number;
  completed: boolean;
  targetPhase: number;
}

export interface SkillGapDetailDto {
  skill: string;
  requiredLevel: number;
  currentMastery: number;
  gap: number;
  priority: number;
  status: string;
}

export interface WeeklyDayScheduleDto {
  dayName: string;
  dayIndex: number;
  allocatedMinutes: number;
  activities: LearningPathNodeDto[];
}

export interface WeeklyLearningPlanDto {
  weekNumber: number;
  weeklyTargetMinutes: number;
  scheduledMinutes: number;
  focusTopic: string;
  days: WeeklyDayScheduleDto[];
}

export interface PathChangeHistoryDto {
  version: number;
  timestamp: string;
  reason: string;
  explanation: string;
  overallProgress: number;
}

export interface LearningPathFullResponse {
  id: string;
  userId: string;
  title: string;
  description: string;
  targetCareer: string;
  targetRole: string;
  status: string;
  version: number;
  overallProgress: number;
  estimatedTotalHours: number;
  completedHours: number;
  qualityScore: number;
  qualityBreakdown: Record<string, number>;
  currentNodeId?: string;
  nodes: LearningPathNodeDto[];
  milestones: LearningPathMilestoneDto[];
  skillGaps: SkillGapDetailDto[];
  weeklyHours: number;
  generatedAt: string;
  lastRecalculatedAt?: string;
  recalculationReason?: string;
}

// Phase 6 — Adaptive Assessment Session DTOs
export type ConfidenceLevel = 'LOW' | 'MEDIUM' | 'HIGH';
export type AdaptiveSessionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';

export interface AdaptiveSessionStartResponse {
  sessionId: string;
  assessmentId: string;
  assessmentTitle: string;
  skillName: string;
  currentDifficulty: CourseDifficulty;
  status: AdaptiveSessionStatus;
  startedAt: string;
  totalAvailableQuestions: number;
}

export interface AdaptiveNextQuestionResponse {
  sessionId: string;
  questionId?: string;
  questionNumber?: number;
  totalQuestionsEstimated?: number;
  questionText?: string;
  questionType?: string;
  options?: string[];
  difficulty?: CourseDifficulty;
  skillName?: string;
  conceptFocus?: string;
  isTerminated: boolean;
  terminationReason?: string;
}

export interface AdaptiveAnswerSubmissionResult {
  correct: boolean;
  feedback: string;
  explanation: string;
  updatedKnowledgeProbability: number;
  updatedMasteryLevel: string;
  nextDifficulty: CourseDifficulty;
  possibleGuess: boolean;
  possibleCarelessError: boolean;
  sessionComplete: boolean;
  terminationReason?: string;
}

export interface AdaptiveSessionResultResponse {
  sessionId: string;
  assessmentId: string;
  assessmentTitle: string;
  overallScore: number;
  masteryEstimate: number;
  confidenceScore: number;
  confidenceLevel: ConfidenceLevel;
  difficultyReached: CourseDifficulty;
  questionsAnswered: number;
  correctAnswers: number;
  incorrectAnswers: number;
  averageResponseTimeSeconds: number;
  strongSkills: string[];
  developingSkills: string[];
  weakSkills: string[];
  revisionRequired: string[];
  behaviorCategory: string;
  behaviorInsights: string[];
  recommendedNextAction: string;
  conceptCoverage: Record<string, number>;
}

export interface AdaptiveSessionAnalyticsResponse {
  sessionId: string;
  accuracyTrend: boolean[];
  difficultyProgression: string[];
  responseTimeTrend: number[];
  conceptMasteryDeltas: Record<string, number>;
  totalTimeSeconds: number;
  consistencyRating: string;
}




