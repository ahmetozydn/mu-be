// ============================================================
// mulakatim.com — API Contracts
// Base URL: /api
// Auth: Authorization: Bearer <accessToken>
// ============================================================

// ============================================================
// SHARED
// ============================================================

export interface ErrorResponse {
  code: string;
  message: string;
  timestamp: string; // ISO 8601
  fieldErrors?: Record<string, string>; // sadece VALIDATION_ERROR'da gelir
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ============================================================
// AUTH  —  /api/auth
// ============================================================

// POST /api/auth/register
export interface RegisterRequest {
  name: string;       // max 100
  email: string;
  password: string;   // min 8, max 100
}

// POST /api/auth/login
export interface LoginRequest {
  email: string;
  password: string;
}

// POST /api/auth/google
export interface GoogleAuthRequest {
  idToken: string;    // Google Sign-In'den alınan idToken
}

// POST /api/auth/refresh
export interface RefreshTokenRequest {
  refreshToken: string;
}

// --- Responses ---

export interface UserSummary {
  id: string;         // UUID
  name: string;
  email: string;
  createdAt: string;  // ISO 8601
}

// register → 201, login → 200, google → 200 (mevcut) | 201 (yeni kayıt)
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserSummary;
  isNewUser?: boolean; // sadece /google'da gelir
}

// refresh → 200
export interface RefreshTokenResponse {
  accessToken: string;
}

// logout → 204 No Content

// --- Hata kodları ---
// register  → 409 EMAIL_ALREADY_EXISTS
// login     → 401 INVALID_CREDENTIALS
// google    → 401 INVALID_GOOGLE_TOKEN
// refresh   → 401 REFRESH_TOKEN_EXPIRED

// ============================================================
// KULLANICI  —  /api/users
// ============================================================

// GET /api/users/me  →  200
export interface UserResponse {
  id: string;
  name: string;
  email: string;
  createdAt: string;
}

// PATCH /api/users/me  →  200
export interface UpdateUserRequest {
  name: string; // max 100
}

// DELETE /api/users/me  →  204 No Content

// ============================================================
// KATEGORİLER  —  /api/categories
// ============================================================

export type CategoryType = 'POSITION' | 'TECHNICAL' | 'LANGUAGE';

export interface QuestionCount {
  junior: number;
  mid: number;
  senior: number;
  total: number;
}

export interface CategoryResponse {
  id: string;
  type: CategoryType;
  title: string;
  description: string | null;
  icon: string | null;        // lucide icon name
  questionCount: QuestionCount;
}

// GET /api/categories?lang=tr  →  200  CategoryResponse[]
// GET /api/categories/:id?lang=tr  →  200  CategoryResponse
// lang parametresi: "tr" | "en"  (default: "tr")

// --- Hata kodları ---
// GET /:id  →  404 CATEGORY_NOT_FOUND

// ============================================================
// QUİZ  —  /api/quiz
// ============================================================

export type Difficulty = 'junior' | 'mid' | 'senior';
export type SessionStatus = 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';

// POST /api/quiz/preview/start  (misafir, auth gerekmez)  →  201
export interface PreviewStartRequest {
  categoryId: string;
  language: 'tr' | 'en';
  difficulty: Difficulty | null;  // null → hepsi
}

export interface PreviewStartResponse {
  sessionId: string;
  totalQuestions: number;
  previewLimit: number;        // 3
  startedAt: string;
  expiresAt: string;
}

// POST /api/quiz/start  (Bearer)  →  201
export interface QuizStartRequest {
  categoryId?: string;         // normal mod
  categoryIds?: string[];      // karma mod
  language: 'tr' | 'en';
  difficulty: Difficulty | null;
  isKarma: boolean;
  questionLimit?: number;      // karma modda toplam soru sayısı
}

export interface QuizStartResponse {
  sessionId: string;
  totalQuestions: number;
  startedAt: string;
}

// GET /api/quiz/:sessionId/current  (auth opsiyonel)  →  200
export interface QuestionOption {
  index: number;    // 0-3
  text: string;
}

export interface CurrentQuestion {
  id: string;
  difficulty: Difficulty;
  questionText: string;
  options: QuestionOption[];
  // correctIndex YOK — asla gelmez
}

export interface CurrentQuestionResponse {
  sessionId: string;
  position: number;          // 0-indexed
  totalQuestions: number;
  isLastQuestion: boolean;
  isGuest: boolean;
  remainingPreview: number | null; // misafir → kalan önizleme, login → null
  question: CurrentQuestion;
}

// POST /api/quiz/:sessionId/answer  (auth opsiyonel)  →  200
export interface AnswerRequest {
  selectedIndex: number;     // 0-3
  durationSec?: number;      // bu soruya harcanan süre (opsiyonel)
}

export interface AnswerResponse {
  accepted: boolean;
  hasNext: boolean;
  nextPosition?: number;     // hasNext=true ise
  completed?: boolean;       // hasNext=false ise true
  sessionId?: string;        // completed=true ise → /result endpoint'ine yönlendir
}

// GET /api/quiz/:sessionId/result  (Bearer)  →  200
export interface QuizResultItem {
  position: number;
  questionText: string;
  options: string[];          // sadece text array
  selectedIndex: number;
  correctIndex: number;       // artık açıklanır
  isCorrect: boolean;
  explanation: string | null;
}

export interface QuizResultResponse {
  sessionId: string;
  categoryTitle: string;
  language: 'tr' | 'en';
  score: number;
  total: number;
  percentage: number;
  totalDurationSec: number | null;
  completedAt: string;
  results: QuizResultItem[];
}

// GET /api/quiz/history?page=0&size=10  (Bearer)  →  200
export interface QuizHistoryItem {
  sessionId: string;
  categoryId: string;
  categoryTitle: string;
  language: 'tr' | 'en';
  difficulty: Difficulty | null;
  isKarma: boolean;
  score: number;
  total: number;
  percentage: number;
  durationSec: number | null;
  completedAt: string;
}

// PageResponse<QuizHistoryItem>

// GET /api/quiz/history/:sessionId  (Bearer)  →  200  QuizResultResponse

// --- Hata kodları ---
// GET  current   →  404 SESSION_NOT_FOUND
// GET  current   →  409 SESSION_ALREADY_COMPLETED
// POST answer    →  401 LOGIN_REQUIRED  { code, message, previewLimit }
// POST answer    →  409 WRONG_QUESTION_POSITION

export interface LoginRequiredError extends ErrorResponse {
  code: 'LOGIN_REQUIRED';
  previewLimit: number;
}

// ============================================================
// FEEDBACk  —  /api/feedback
// ============================================================

export type FeedbackType = 'question' | 'session' | 'general';
export type FeedbackStatus = 'PENDING' | 'REVIEWED' | 'RESOLVED';

// POST /api/feedback  (Bearer)  →  201
export interface FeedbackRequest {
  type: FeedbackType;
  questionId?: string;   // type="question" ise
  sessionId?: string;    // type="session" ise
  message: string;
}

export interface FeedbackResponse {
  id: string;
  status: FeedbackStatus;
}

// ============================================================
// ADMIN  —  /api/admin  (ROLE_ADMIN)
// ============================================================

// GET  /api/admin/questions?page=0&size=20  →  200  PageResponse<AdminQuestionResponse>
export interface AdminQuestionResponse {
  id: string;
  categoryId: string;
  language: 'tr' | 'en';
  difficulty: Difficulty;
  questionText: string;
  explanation: string | null;
  correctIndex: number;         // admin'e açıklanır
  options: QuestionOption[];
  active: boolean;
  createdAt: string;
}

// POST /api/admin/questions  →  201  AdminQuestionResponse
export interface CreateQuestionRequest {
  categoryId: string;
  language: 'tr' | 'en';
  difficulty: Difficulty;
  questionText: string;
  explanation?: string;
  correctIndex: number;
  options: { index: number; text: string }[];  // 4 seçenek (0-3)
}

// PUT /api/admin/questions/:id  →  200  AdminQuestionResponse
export type UpdateQuestionRequest = Partial<CreateQuestionRequest>;

// DELETE /api/admin/questions/:id  →  204  (active=false yapar, fiziksel silmez)

// GET  /api/admin/feedback?status=PENDING&page=0&size=20  →  200  PageResponse<AdminFeedbackResponse>
export interface AdminFeedbackResponse {
  id: string;
  userId: string | null;
  type: FeedbackType;
  questionId: string | null;
  sessionId: string | null;
  message: string;
  status: FeedbackStatus;
  createdAt: string;
}

// PATCH /api/admin/feedback/:id/status  →  200
export interface UpdateFeedbackStatusRequest {
  status: FeedbackStatus;
}

// GET /api/admin/stats  →  200
export interface AdminStats {
  totalUsers: number;
  totalSessions: number;
  completedSessions: number;
  totalQuestions: number;
  categoryBreakdown: { categoryId: string; sessionCount: number }[];
}
