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
  steps: CookingStep[];
}

export interface RecipeCreatePayload {
  title: string;
  description: string;
  authorId: number;
  categoryId: number;
  ingredientIds: number[];
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
  barcodeOrQuery: string;
  foundInOpenFoodFacts: boolean;
  caloriesKcal: number;
  proteinsGrams: number;
  fatGrams: number;
  carbohydratesGrams: number;
}

export interface NutritionReport {
  recipeId: number;
  recipeTitle: string;
  totalIngredientsAnalyzed: number;
  totalCaloriesKcal: number;
  totalProteinsGrams: number;
  totalFatGrams: number;
  totalCarbohydratesGrams: number;
  ingredientsData: IngredientNutrition[];
  generatedAt: string;
  provider: string;
}

export interface NutritionReportTask {
  taskId: string;
  status: 'SUBMITTED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  submittedAt: string;
  completedAt: string | null;
  errorMessage: string | null;
  report: NutritionReport | null;
}
