export type ExperienceLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
export type LearningStyle = 'PRACTICAL' | 'THEORETICAL' | 'VISUAL' | 'AUDITORY' | 'READING_WRITING';
export type PreferredContentType = 'VIDEO' | 'ARTICLE' | 'INTERACTIVE_EXERCISE' | 'BOOK' | 'PROJECT';
export type ProgressStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'PAUSED';
export type GapType = 'NONE' | 'PARTIAL' | 'FULL';
export type GapSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type CourseDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export interface User {
  id: string;
  name: string;
  email: string;
  careerGoal?: string;
  experienceLevel?: ExperienceLevel;
  dailyLearningHours?: number;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserCreateRequest {
  name: string;
  email: string;
  careerGoal?: string;
  experienceLevel?: ExperienceLevel;
  dailyLearningHours?: number;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
}

export interface UserUpdateRequest {
  name: string;
  careerGoal?: string;
  experienceLevel?: ExperienceLevel;
  dailyLearningHours?: number;
  learningStyle?: LearningStyle;
  preferredContentType?: PreferredContentType;
}

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
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface SkillGapItem {
  skillId: string;
  skillName: string;
  skillCategory: string;
  currentProficiency: string;
  requiredProficiency: string;
  gapType: GapType;
  severity: GapSeverity;
  priority: string;
  mandatory: boolean;
  explanation: string;
}

export interface SkillGapAnalysis {
  userId: string;
  userName: string;
  careerId: string;
  careerName: string;
  totalRequiredSkills: number;
  skillsWithNoGap: number;
  partialGaps: number;
  fullGaps: number;
  overallGapScore: number;
  gaps: SkillGapItem[];
}

export interface CourseRecommendation {
  rank: number;
  courseId: string;
  courseTitle: string;
  provider: string;
  url: string;
  difficulty: CourseDifficulty;
  courseType: string;
  rating: number;
  price: number;
  isFree: boolean;
  ruleBasedScore: number;
  mlScore: number;
  finalScore: number;
  matchedSkills: string[];
  gapSkillsAddressed: string[];
  explanation: string;
}

export interface RecommendationSummary {
  userId: string;
  userName: string;
  careerId: string;
  careerName: string;
  hasGaps: boolean;
  totalCandidateCourses: number;
  recommendations: CourseRecommendation[];
}

export interface RecommendedCourseItem {
  courseId: string;
  courseTitle: string;
  provider: string;
  score: number;
  difficulty: string;
  skillsCovered: string[];
}

export interface LearningPathPhase {
  phaseNumber: number;
  phaseTitle: string;
  targetSkills: string[];
  courses: RecommendedCourseItem[];
  estimatedDuration: string;
  explanation: string;
}

export interface PersonalizedLearningPath {
  success: boolean;
  userId: string;
  targetCareer: string;
  summary: string;
  phases: LearningPathPhase[];
  provider: string;
  model: string;
  error?: string;
}

export interface AdaptLearningPathResponse {
  adapted: boolean;
  changeReason: string;
  completedSkills: string[];
  remainingSkills: string[];
  path: PersonalizedLearningPath;
}

export interface LearningProgress {
  progressId: string;
  userId: string;
  courseId: string;
  courseTitle: string;
  status: ProgressStatus;
  completionPercentage: number;
  lastAccessedAt: string;
  updatedAt: string;
}

export interface LearningProgressRequest {
  status: ProgressStatus;
  completionPercentage: number;
}

export interface OnboardingData {
  experienceLevel: ExperienceLevel;
  dailyLearningHours: number;
  learningStyle: LearningStyle;
  preferredContentType: PreferredContentType;
}

export interface AppSession {
  user: User;
  careerId?: string;
  careerName?: string;
  onboardingComplete: boolean;
}
