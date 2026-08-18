import type {
  AdaptLearningPathResponse,
  Career,
  LearningProgress,
  LearningProgressRequest,
  PageResponse,
  PersonalizedLearningPath,
  RecommendationSummary,
  SkillGapAnalysis,
  User,
  UserCreateRequest,
  UserUpdateRequest,
} from './types';

const API_BASE = import.meta.env.VITE_API_URL ?? '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed (${response.status})`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  health: () => request<{ status: string }>('/health'),

  createUser: (body: UserCreateRequest) =>
    request<User>('/users', { method: 'POST', body: JSON.stringify(body) }),

  getUser: (id: string) => request<User>(`/users/${id}`),

  updateUser: (id: string, body: UserUpdateRequest) =>
    request<User>(`/users/${id}`, { method: 'PUT', body: JSON.stringify(body) }),

  getCareers: (page = 0, size = 50) =>
    request<PageResponse<Career>>(`/careers?page=${page}&size=${size}&sortBy=name&sortDir=ASC`),

  getCareer: (id: string) => request<Career>(`/careers/${id}`),

  searchCareers: (name: string) =>
    request<Career[]>(`/careers/search?name=${encodeURIComponent(name)}`),

  getSkillGaps: (userId: string, careerId: string) =>
    request<SkillGapAnalysis>(`/users/${userId}/skill-gaps?careerId=${careerId}`),

  getRecommendations: (userId: string, careerId: string, limit = 10) =>
    request<RecommendationSummary>(
      `/users/${userId}/recommendations?careerId=${careerId}&limit=${limit}`,
    ),

  generateLearningPath: (userId: string, careerId: string) =>
    request<PersonalizedLearningPath>('/learning-paths/generate', {
      method: 'POST',
      body: JSON.stringify({ userId, careerId }),
    }),

  adaptLearningPath: (userId: string, careerId: string) =>
    request<AdaptLearningPathResponse>(`/learning-paths/users/${userId}/adapt`, {
      method: 'POST',
      body: JSON.stringify({ careerId }),
    }),

  getUserProgress: (userId: string) =>
    request<LearningProgress[]>(`/users/${userId}/learning-progress`),

  upsertProgress: (userId: string, courseId: string, body: LearningProgressRequest) =>
    request<LearningProgress>(`/users/${userId}/learning-progress/${courseId}`, {
      method: 'PUT',
      body: JSON.stringify(body),
    }),
};
