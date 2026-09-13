import {
  Category,
  CounterStats,
  Ingredient,
  NutritionReportTask,
  RaceConditionResult,
  Recipe,
  RecipeCreatePayload,
  RecipeFilterPage,
  User,
} from '../types';

export interface ApiErrorResponse {
  statusCode: number;
  message: string;
  timestamp: string;
  fieldErrors?: Record<string, string>;
}

class ApiError extends Error {
  statusCode: number;
  fieldErrors?: Record<string, string>;

  constructor(statusCode: number, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.name = 'ApiError';
    this.statusCode = statusCode;
    this.fieldErrors = fieldErrors;
  }
}

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  const headers = new Headers(options?.headers || {});
  if (!headers.has('Content-Type') && options?.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 204) {
    return {} as T;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || `Ошибка сервера: HTTP ${response.status}`;
    throw new ApiError(response.status, errorMsg, data?.fieldErrors);
  }

  return data as T;
}

export const api = {
  // Recipes
  getRecipes: () => request<Recipe[]>('/api/recipes'),
  getRecipeById: (id: number) => request<Recipe>(`/api/recipes/${id}`),
  searchRecipesByTitle: (title: string) => request<Recipe[]>(`/api/recipes/search?title=${encodeURIComponent(title)}`),
  createRecipe: (payload: RecipeCreatePayload) =>
    request<Recipe>('/api/recipes', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  updateRecipe: (id: number, payload: RecipeCreatePayload) =>
    request<Recipe>(`/api/recipes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  deleteRecipe: (id: number) =>
    request<void>(`/api/recipes/${id}`, {
      method: 'DELETE',
    }),

  // Filtering (JPQL / Native)
  filterRecipesJPQL: (authorUsername: string, categoryName: string, page = 0, size = 10) =>
    request<RecipeFilterPage>(
      `/api/recipes/filter/jpql?authorUsername=${encodeURIComponent(authorUsername)}&categoryName=${encodeURIComponent(
        categoryName
      )}&page=${page}&size=${size}`
    ),
  filterRecipesNative: (authorUsername: string, categoryName: string, page = 0, size = 10) =>
    request<RecipeFilterPage>(
      `/api/recipes/filter/native?authorUsername=${encodeURIComponent(authorUsername)}&categoryName=${encodeURIComponent(
        categoryName
      )}&page=${page}&size=${size}`
    ),

  // Categories
  getCategories: () => request<Category[]>('/api/categories'),
  createCategory: (name: string, description?: string) =>
    request<Category>('/api/categories', {
      method: 'POST',
      body: JSON.stringify({ name, description }),
    }),
  deleteCategory: (id: number) =>
    request<void>(`/api/categories/${id}`, {
      method: 'DELETE',
    }),

  // Ingredients
  getIngredients: () => request<Ingredient[]>('/api/ingredients'),
  createIngredient: (name: string) =>
    request<Ingredient>('/api/ingredients', {
      method: 'POST',
      body: JSON.stringify({ name }),
    }),
  deleteIngredient: (id: number) =>
    request<void>(`/api/ingredients/${id}`, {
      method: 'DELETE',
    }),

  // Users
  getUsers: () => request<User[]>('/api/users'),
  createUser: (username: string, email: string) =>
    request<User>('/api/users', {
      method: 'POST',
      body: JSON.stringify({ username, email }),
    }),

  // Concurrency & Demos (Lab 6)
  getViewStats: () => request<CounterStats>('/api/recipes/views/stats'),
  resetViewCounters: () =>
    request<void>('/api/recipes/views/reset', {
      method: 'POST',
    }),
  demoRaceCondition: (threads = 50, increments = 100) =>
    request<RaceConditionResult>(`/api/recipes/demo/race-condition?threadCount=${threads}&incrementsPerThread=${increments}`, {
      method: 'POST',
    }),

  // Asynchronous Nutrition calculation (Lab 6)
  startNutritionReport: (recipeId: number) =>
    request<NutritionReportTask>(`/api/recipes/${recipeId}/nutrition-report`, {
      method: 'POST',
    }),
  getNutritionReportStatus: (taskId: string) =>
    request<NutritionReportTask>(`/api/recipes/nutrition-report/${taskId}`),
};
