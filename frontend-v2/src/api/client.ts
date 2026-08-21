import {
  AuthResponse,
  AuthenticatedUserResponse,
  ForgotPasswordRequest,
  GenericApiResponse,
  LoginRequest,
  ResendOtpRequest,
  ResetPasswordRequest,
  SignupRequest,
  SignupResponse,
  VerifyEmailOtpRequest,
  VerifyResetOtpRequest,
  VerifyResetOtpResponse,
  UserProfile,
  UpdateProfileRequest,
  UserSkill,
  UserSkillRequest,
  Course,
  CourseEnrollment,
  CourseProgressUpdate,
  Assessment,
  AssessmentSubmissionRequest,
  AssessmentResult,
  Project,
  UpdateUserProjectRequest,
  Notification,
  NotificationCategory,
  SupportTicket,
  CreateSupportTicketRequest,
  DashboardAggregated,
  ProgressAnalytics,
  Settings,
  UpdateSettingsRequest,
  OnboardingCompleteRequest,
  PageResponse,
  ActiveLearningPathResponse,
  PersonalizedLearningPath,
  AdaptLearningPathResponse
} from './types';

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api';
const TOKEN_KEY = 'learnai_v2_jwt_token';

class ApiClient {
  private getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  public setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  public clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  public isAuthenticated(): boolean {
    return Boolean(this.getToken());
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const token = this.getToken();
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...((options.headers as Record<string, string>) || {}),
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      this.clearToken();
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    }

    if (!response.ok) {
      let errorMessage = this.friendlyError(response.status, null);
      try {
        const errorData = await response.json();
        // Use server message only if it is a clean user-facing string (not a stack trace)
        const serverMsg = errorData.message || errorData.error || null;
        if (serverMsg && !serverMsg.includes('Exception') && !serverMsg.includes('at com.') && serverMsg.length < 300) {
          errorMessage = serverMsg;
        } else if (serverMsg) {
          // Server returned a technical message — map to friendly version
          errorMessage = this.friendlyError(response.status, null);
        }
      } catch {
        errorMessage = this.friendlyError(response.status, null);
      }
      throw new Error(errorMessage);
    }

    if (response.status === 204) {
      return {} as T;
    }

    return response.json();
  }

  private friendlyError(status: number, _detail: string | null): string {
    switch (status) {
      case 400: return 'The request could not be processed. Please check your input and try again.';
      case 401: return 'Your session has expired. Please log in again.';
      case 403: return 'You are not authorised to perform this action.';
      case 404: return 'The requested resource was not found.';
      case 409: return 'A conflict occurred. This item may already exist.';
      case 422: return 'The provided data is invalid. Please review and try again.';
      case 429: return 'Too many requests. Please slow down and try again in a moment.';
      case 500: return 'Something went wrong on our end. Please try again later.';
      case 502: return 'The server is temporarily unavailable. Please try again shortly.';
      case 503: return 'AI recommendations are temporarily unavailable. The system is still functional.';
      case 504: return 'The request timed out. Please check your connection and try again.';
      default:  return 'An unexpected error occurred. Please try again.';
    }
  }

  // ==========================================
  // Auth Endpoints
  // ==========================================
  async signup(data: SignupRequest): Promise<SignupResponse> {
    return this.request<SignupResponse>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async login(data: LoginRequest): Promise<AuthResponse> {
    const res = await this.request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    if (res.accessToken) {
      this.setToken(res.accessToken);
    }
    return res;
  }

  async verifyEmailOtp(data: VerifyEmailOtpRequest): Promise<AuthResponse> {
    const res = await this.request<AuthResponse>('/auth/verify-email-otp', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    if (res.accessToken) {
      this.setToken(res.accessToken);
    }
    return res;
  }

  async resendOtp(data: ResendOtpRequest): Promise<GenericApiResponse> {
    return this.request<GenericApiResponse>('/auth/resend-otp', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async forgotPassword(data: ForgotPasswordRequest): Promise<GenericApiResponse> {
    return this.request<GenericApiResponse>('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async verifyResetOtp(data: VerifyResetOtpRequest): Promise<VerifyResetOtpResponse> {
    return this.request<VerifyResetOtpResponse>('/auth/verify-reset-otp', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async resetPassword(data: ResetPasswordRequest): Promise<GenericApiResponse> {
    return this.request<GenericApiResponse>('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getCurrentUser(): Promise<AuthenticatedUserResponse> {
    return this.request<AuthenticatedUserResponse>('/auth/me');
  }

  async getMe(): Promise<AuthenticatedUserResponse> {
    return this.getCurrentUser();
  }

  logout(): void {
    this.clearToken();
  }

  // ==========================================
  // Profile Endpoints
  // ==========================================
  async getProfile(): Promise<UserProfile> {
    return this.request<UserProfile>('/profile');
  }

  async updateProfile(data: UpdateProfileRequest): Promise<UserProfile> {
    return this.request<UserProfile>('/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async getProfileSkills(): Promise<UserSkill[]> {
    return this.request<UserSkill[]>('/profile/skills');
  }

  async addProfileSkill(data: UserSkillRequest): Promise<UserSkill> {
    return this.request<UserSkill>('/profile/skills', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async deleteProfileSkill(skillId: string): Promise<void> {
    return this.request<void>(`/profile/skills/${skillId}`, {
      method: 'DELETE',
    });
  }

  // ==========================================
  // Courses Endpoints
  // ==========================================
  async getCourses(params?: { page?: number; size?: number; sortBy?: string; sortDir?: string }): Promise<PageResponse<Course>> {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.set('page', params.page.toString());
    if (params?.size !== undefined) query.set('size', params.size.toString());
    if (params?.sortBy) query.set('sortBy', params.sortBy);
    if (params?.sortDir) query.set('sortDir', params.sortDir);
    return this.request<PageResponse<Course>>(`/courses?${query.toString()}`);
  }

  async getCourseById(id: string): Promise<Course> {
    return this.request<Course>(`/courses/${id}`);
  }

  async getCategories(): Promise<string[]> {
    return this.request<string[]>('/courses/categories');
  }

  async searchCourses(title: string): Promise<Course[]> {
    return this.request<Course[]>(`/courses/search?title=${encodeURIComponent(title)}`);
  }

  async filterCourses(params: {
    difficulty?: string;
    courseType?: string;
    provider?: string;
    isFree?: boolean;
    language?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<Course>> {
    const query = new URLSearchParams();
    if (params.difficulty) query.set('difficulty', params.difficulty);
    if (params.courseType) query.set('courseType', params.courseType);
    if (params.provider) query.set('provider', params.provider);
    if (params.isFree !== undefined) query.set('isFree', String(params.isFree));
    if (params.language) query.set('language', params.language);
    if (params.page !== undefined) query.set('page', String(params.page));
    if (params.size !== undefined) query.set('size', String(params.size));
    return this.request<PageResponse<Course>>(`/courses/filter?${query.toString()}`);
  }

  async enrollCourse(courseId: string): Promise<CourseEnrollment> {
    return this.request<CourseEnrollment>(`/courses/${courseId}/enroll`, {
      method: 'POST',
    });
  }

  async getCourseProgress(courseId: string): Promise<CourseEnrollment> {
    return this.request<CourseEnrollment>(`/courses/${courseId}/progress`);
  }

  async updateCourseProgress(courseId: string, data: CourseProgressUpdate): Promise<CourseEnrollment> {
    return this.request<CourseEnrollment>(`/courses/${courseId}/progress`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async getMyCourses(): Promise<CourseEnrollment[]> {
    return this.request<CourseEnrollment[]>('/courses/my-courses');
  }

  // ==========================================
  // Learning Path Endpoints (Legacy)
  // ==========================================
  async getActiveLearningPath(): Promise<ActiveLearningPathResponse> {
    return this.request<ActiveLearningPathResponse>('/learning-paths/active');
  }

  async generateLegacyLearningPath(data: { userId: string; careerId?: string }): Promise<PersonalizedLearningPath> {
    return this.request<PersonalizedLearningPath>('/learning-paths/generate', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async adaptLearningPath(data: { userId: string; reason?: string }): Promise<AdaptLearningPathResponse> {
    return this.request<AdaptLearningPathResponse>('/learning-paths/adapt', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }


  // ==========================================
  // Assessment Endpoints
  // ==========================================
  async getAssessments(): Promise<Assessment[]> {
    return this.request<Assessment[]>('/assessments');
  }

  async getAssessmentById(id: string): Promise<Assessment> {
    return this.request<Assessment>(`/assessments/${id}`);
  }

  async submitAssessment(id: string, data: AssessmentSubmissionRequest): Promise<AssessmentResult> {
    return this.request<AssessmentResult>(`/assessments/${id}/submit`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getMyAssessmentResults(): Promise<AssessmentResult[]> {
    return this.request<AssessmentResult[]>('/assessments/my-results');
  }

  // ==========================================
  // Projects Endpoints
  // ==========================================
  async getProjects(): Promise<Project[]> {
    return this.request<Project[]>('/projects');
  }

  async getProjectById(id: string): Promise<Project> {
    return this.request<Project>(`/projects/${id}`);
  }

  async updateProjectProgress(id: string, data?: UpdateUserProjectRequest): Promise<Project> {
    return this.request<Project>(`/projects/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data || {}),
    });
  }

  // ==========================================
  // Notifications Endpoints
  // ==========================================
  async getNotifications(category?: NotificationCategory): Promise<Notification[]> {
    const query = category ? `?category=${category}` : '';
    return this.request<Notification[]>(`/notifications${query}`);
  }

  async getUnreadNotificationCount(): Promise<{ unreadCount: number }> {
    return this.request<{ unreadCount: number }>('/notifications/unread-count');
  }

  async markNotificationAsRead(id: string): Promise<Notification> {
    return this.request<Notification>(`/notifications/${id}/read`, {
      method: 'PUT',
    });
  }

  async markAllNotificationsAsRead(): Promise<GenericApiResponse> {
    return this.request<GenericApiResponse>('/notifications/read-all', {
      method: 'PUT',
    });
  }

  // ==========================================
  // Support Tickets Endpoints
  // ==========================================
  async getSupportTickets(): Promise<SupportTicket[]> {
    return this.request<SupportTicket[]>('/support/tickets');
  }

  async getSupportTicketById(id: string): Promise<SupportTicket> {
    return this.request<SupportTicket>(`/support/tickets/${id}`);
  }

  async createSupportTicket(data: CreateSupportTicketRequest): Promise<SupportTicket> {
    return this.request<SupportTicket>('/support/tickets', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // ==========================================
  // Dashboard & Analytics Endpoints
  // ==========================================
  async getDashboardData(): Promise<DashboardAggregated> {
    return this.request<DashboardAggregated>('/dashboard');
  }

  async getProgressAnalytics(): Promise<ProgressAnalytics> {
    return this.request<ProgressAnalytics>('/analytics/progress');
  }

  // ==========================================
  // Settings Endpoints
  // ==========================================
  async getSettings(): Promise<Settings> {
    return this.request<Settings>('/settings');
  }

  async updateSettings(data: UpdateSettingsRequest): Promise<Settings> {
    return this.request<Settings>('/settings', {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  // ==========================================
  // Onboarding Endpoints
  // ==========================================
  async completeOnboarding(data: OnboardingCompleteRequest): Promise<UserProfile> {
    return this.request<UserProfile>('/onboarding/complete', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // ==========================================
  // AI Mentor Endpoints
  // ==========================================
  async sendMentorMessage(message: string, conversationId?: string): Promise<import('./types').AIMentorChatResponse> {
    return this.request<import('./types').AIMentorChatResponse>('/ai/mentor/chat', {
      method: 'POST',
      body: JSON.stringify({ message, conversationId }),
    });
  }

  async getMentorHistory(): Promise<import('./types').AIMessageEntity[]> {
    return this.request<import('./types').AIMessageEntity[]>('/ai/mentor/history');
  }

  async clearMentorHistory(): Promise<{ message: string }> {
    return this.request<{ message: string }>('/ai/mentor/history', {
      method: 'DELETE',
    });
  }

  // ==========================================
  // Recommendation Endpoints
  // ==========================================
  async getRecommendations(): Promise<import('./types').RecommendationSummaryResponse> {
    return this.request<import('./types').RecommendationSummaryResponse>('/recommendations');
  }

  // ==========================================
  // Phase 4 — Adaptive Learner Intelligence Endpoints
  // ==========================================
  async getMasterySummary(): Promise<import('./types').LearnerMasterySummary> {
    return this.request<import('./types').LearnerMasterySummary>('/learner/mastery');
  }

  async getWeakSkills(): Promise<import('./types').SkillMasteryConceptItem[]> {
    return this.request<import('./types').SkillMasteryConceptItem[]>('/learner/weak-skills');
  }

  async getRevisionRequired(): Promise<import('./types').SkillMasteryConceptItem[]> {
    return this.request<import('./types').SkillMasteryConceptItem[]>('/learner/revision');
  }

  async getLearnerBehavior(): Promise<import('./types').LearnerBehaviorProfile> {
    return this.request<import('./types').LearnerBehaviorProfile>('/learner/behavior');
  }

  async getLearningPlan(): Promise<import('./types').DailyLearningPlan> {
    return this.request<import('./types').DailyLearningPlan>('/learning-plan');
  }

  async generateLearningPlan(): Promise<import('./types').DailyLearningPlan> {
    return this.request<import('./types').DailyLearningPlan>('/learning-plan/generate', {
      method: 'POST',
    });
  }

  async getAdaptiveQuestion(assessmentId: string): Promise<import('./types').AdaptiveQuestionResponse> {
    return this.request<import('./types').AdaptiveQuestionResponse>(`/assessments/${assessmentId}/adaptive-question`);
  }

  async submitAdaptiveAnswer(
    assessmentId: string,
    questionId: string,
    answer: string,
    responseTimeSeconds: number
  ): Promise<import('./types').AdaptiveAnswerResult> {
    return this.request<import('./types').AdaptiveAnswerResult>(`/assessments/${assessmentId}/adaptive-answer`, {
      method: 'POST',
      body: JSON.stringify({ questionId, answer, responseTimeSeconds }),
    });
  }

  // ==========================================
  // Phase 5 — Real Personalized Learning Path Engine Endpoints
  // ==========================================
  async getLearningPath(): Promise<import('./types').LearningPathFullResponse> {
    return this.request<import('./types').LearningPathFullResponse>('/learning-path');
  }

  async generateLearningPath(careerId?: string): Promise<import('./types').LearningPathFullResponse> {
    const url = careerId ? `/learning-path/generate?careerId=${careerId}` : '/learning-path/generate';
    return this.request<import('./types').LearningPathFullResponse>(url, {
      method: 'POST',
    });
  }

  async recalculateLearningPath(reason?: string): Promise<import('./types').LearningPathFullResponse> {
    const url = reason ? `/learning-path/recalculate?reason=${encodeURIComponent(reason)}` : '/learning-path/recalculate';
    return this.request<import('./types').LearningPathFullResponse>(url, {
      method: 'POST',
    });
  }

  async getLearningPathWeeklyPlan(): Promise<import('./types').WeeklyLearningPlanDto> {
    return this.request<import('./types').WeeklyLearningPlanDto>('/learning-path/weekly-plan');
  }

  async getLearningPathSkillGaps(careerId?: string): Promise<import('./types').SkillGapDetailDto[]> {
    const url = careerId ? `/learning-path/skill-gaps?careerId=${careerId}` : '/learning-path/skill-gaps';
    return this.request<import('./types').SkillGapDetailDto[]>(url);
  }

  async getLearningPathNodes(): Promise<import('./types').LearningPathNodeDto[]> {
    return this.request<import('./types').LearningPathNodeDto[]>('/learning-path/nodes');
  }

  async getLearningPathChanges(): Promise<import('./types').PathChangeHistoryDto[]> {
    return this.request<import('./types').PathChangeHistoryDto[]>('/learning-path/changes');
  }

  async getLearningPathMilestones(): Promise<import('./types').LearningPathMilestoneDto[]> {
    return this.request<import('./types').LearningPathMilestoneDto[]>('/learning-path/milestones');
  }

  // ==========================================
  // Phase 6 — Advanced CAT Session Endpoints
  // ==========================================
  async startAdaptiveSession(assessmentId: string): Promise<import('./types').AdaptiveSessionStartResponse> {
    return this.request<import('./types').AdaptiveSessionStartResponse>(`/assessments/${assessmentId}/adaptive/start`, {
      method: 'POST',
    });
  }

  async getAdaptiveNextQuestion(sessionId: string): Promise<import('./types').AdaptiveNextQuestionResponse> {
    return this.request<import('./types').AdaptiveNextQuestionResponse>(`/assessments/adaptive/${sessionId}/next-question`);
  }

  async submitAdaptiveSessionAnswer(
    sessionId: string,
    questionId: string,
    answer: string,
    responseTimeSeconds: number
  ): Promise<import('./types').AdaptiveAnswerSubmissionResult> {
    return this.request<import('./types').AdaptiveAnswerSubmissionResult>(`/assessments/adaptive/${sessionId}/answer`, {
      method: 'POST',
      body: JSON.stringify({ questionId, answer, responseTimeSeconds }),
    });
  }

  async getAdaptiveSessionResult(sessionId: string): Promise<import('./types').AdaptiveSessionResultResponse> {
    return this.request<import('./types').AdaptiveSessionResultResponse>(`/assessments/adaptive/${sessionId}/result`);
  }

  async getAdaptiveSessionAnalytics(sessionId: string): Promise<import('./types').AdaptiveSessionAnalyticsResponse> {
    return this.request<import('./types').AdaptiveSessionAnalyticsResponse>(`/assessments/adaptive/${sessionId}/analytics`);
  }
}

export const api = new ApiClient();
export const getStoredToken = () => localStorage.getItem(TOKEN_KEY);
export const setStoredToken = (token: string) => localStorage.setItem(TOKEN_KEY, token);
export const clearStoredToken = () => localStorage.removeItem(TOKEN_KEY);
export default api;




