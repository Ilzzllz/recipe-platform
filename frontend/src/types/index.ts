export interface Toast {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  title?: string;
  message: string;
}

export interface Author {
  id: number;
  username: string;
  email?: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface Ingredient {
  id: number;
  name: string;
  caloriesPer100g?: number;
  proteinsPer100g?: number;
  fatsPer100g?: number;
  carbohydratesPer100g?: number;
  gramsPerUnit?: number;
}

export interface RecipeIngredient {
  ingredient: Ingredient;
  quantity: number;
  unit: string;
}

export interface CookingStep {
  id?: number;
  stepOrder: number;
  description: string;
}

export interface Recipe {
  id: number;
  title: string;
  description: string;
  author: Author;
  category: Category;
  ingredients: Ingredient[];
  recipeIngredients?: RecipeIngredient[];
  steps: CookingStep[];
  portions?: number;
  nutrition?: NutritionSummary;
}

export interface NutritionSummary {
  totalWeightGrams: number;
  caloriesKcal: number;
  proteinsGrams: number;
  fatsGrams: number;
  carbohydratesGrams: number;
  caloriesPer100g: number;
  proteinsPer100g: number;
  fatsPer100g: number;
  carbohydratesPer100g: number;
  caloriesPerPortion: number;
  proteinsPerPortion: number;
  fatsPerPortion: number;
  carbohydratesPerPortion: number;
  portions: number;
}

export interface RecipeCreatePayload {
  title: string;
  description: string;
  authorId: number;
  categoryId: number;
  ingredientIds: number[];
  recipeIngredients?: {
    ingredientId: number;
    quantity: number;
    unit: string;
  }[];
  portions?: number;
  steps: {
    stepOrder: number;
    description: string;
  }[];
}

export interface RecipeFilterItem {
  recipeId: number;
  recipeTitle: string;
  recipeDescription: string;
  authorUsername: string;
  categoryName: string;
}

export interface RecipeFilterPage {
  content: RecipeFilterItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CounterStats {
  atomicCount: number;
  synchronizedCount: number;
  unsafeIntCount: number;
  totalViewRecordCalls: number;
}

export interface RaceConditionResult {
  threadCount: number;
  incrementsPerThread: number;
  expectedTotal: number;
  atomicResult: number;
  unsafeIntResult: number;
  lostUpdates: number;
  raceConditionDetected: boolean;
  description: string;
}

export interface IngredientNutrition {
  ingredientName: string;
  caloriesKcal: number;
  proteinsGrams: number;
  fatsGrams?: number;
  fatGrams?: number;
  carbohydratesGrams: number;
  dataSource?: string;
}

export interface NutritionReport {
  recipeId: number;
  recipeTitle: string;
  totalCaloriesKcal: number;
  totalProteinsGrams: number;
  totalFatsGrams?: number;
  totalFatGrams?: number;
  totalCarbohydratesGrams: number;
  ingredients?: IngredientNutrition[];
  ingredientsData?: IngredientNutrition[];
  calculatedAt?: string;
}

export interface NutritionReportTask {
  taskId: string;
  status: 'SUBMITTED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  startedAt?: string;
  submittedAt?: string;
  completedAt?: string | null;
  message?: string | null;
  errorMessage?: string | null;
  result?: NutritionReport | null;
  report?: NutritionReport | null;
}

