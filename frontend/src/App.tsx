import React, { useState, useEffect, useCallback, useRef } from 'react';
import { api } from './api/client';
import { Recipe, Category, Ingredient, User, RecipeCreatePayload, NutritionReportTask } from './types';
import { Navbar } from './components/Navbar';
import { RecipeCard } from './components/RecipeCard';
import { RecipeModal } from './components/RecipeModal';
import { RecipeFormModal } from './components/RecipeFormModal';
import { FilterSection } from './components/FilterSection';
import { ConcurrencySection } from './components/ConcurrencySection';
import { CategoryIngredientManager } from './components/CategoryIngredientManager';
import { Plus, Search, Filter, BookOpen, Layers, ChefHat, Carrot, AlertCircle } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'recipes' | 'filter' | 'concurrency' | 'data'>('recipes');

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [users, setUsers] = useState<User[]>([]);

  const [searchTitle, setSearchTitle] = useState('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('ALL');

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingRecipe, setEditingRecipe] = useState<Recipe | null>(null);
  const [viewingRecipe, setViewingRecipe] = useState<Recipe | null>(null);

  // Nutrition async task
  const [nutritionTask, setNutritionTask] = useState<NutritionReportTask | null>(null);
  const [isPollingNutrition, setIsPollingNutrition] = useState(false);
  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const loadData = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [recList, catList, ingList, userList] = await Promise.all([
        api.getRecipes(),
        api.getCategories().catch(() => []),
        api.getIngredients().catch(() => []),
        api.getUsers().catch(() => []),
      ]);
      setRecipes(recList);
      setCategories(catList);
      setIngredients(ingList);
      setUsers(userList);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Не удалось загрузить данные с сервера';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
    return () => {
      if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
    };
  }, [loadData]);

  // Title search
  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchTitle.trim()) {
      loadData();
      return;
    }
    try {
      setIsLoading(true);
      const found = await api.searchRecipesByTitle(searchTitle.trim());
      setRecipes(found);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Ошибка при поиске по названию');
    } finally {
      setIsLoading(false);
    }
  };

  // View recipe details + increments safe & unsafe view counters
  const handleViewRecipe = async (recipe: Recipe) => {
    try {
      // Calling getById triggers recordView() in RecipeController for concurrency stats!
      const fullRecipe = await api.getRecipeById(recipe.id);
      setViewingRecipe(fullRecipe);
      setNutritionTask(null);
    } catch {
      setViewingRecipe(recipe);
    }
  };

  // Edit recipe
  const handleEditRecipe = (recipe: Recipe) => {
    setEditingRecipe(recipe);
    setIsFormOpen(true);
  };

  // Delete recipe
  const handleDeleteRecipe = async (id: number, title: string) => {
    if (!confirm(`Вы действительно хотите удалить рецепт "${title}"?`)) return;
    try {
      await api.deleteRecipe(id);
      setRecipes((prev) => prev.filter((r) => r.id !== id));
      if (viewingRecipe?.id === id) setViewingRecipe(null);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Ошибка при удалении рецепта');
    }
  };

  // Save (Create or Update)
  const handleSubmitRecipe = async (payload: RecipeCreatePayload, id?: number) => {
    if (id) {
      const updated = await api.updateRecipe(id, payload);
      setRecipes((prev) => prev.map((r) => (r.id === id ? updated : r)));
      if (viewingRecipe?.id === id) setViewingRecipe(updated);
    } else {
      const created = await api.createRecipe(payload);
      setRecipes((prev) => [created, ...prev]);
    }
  };

  // Async Nutrition calculation
  const handleCalculateNutrition = async (recipe: Recipe) => {
    try {
      setIsPollingNutrition(true);
      const task = await api.startNutritionReport(recipe.id);
      setNutritionTask(task);
      setViewingRecipe(recipe);

      if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);

      pollingIntervalRef.current = setInterval(async () => {
        try {
          const currentStatus = await api.getNutritionReportStatus(task.taskId);
          setNutritionTask(currentStatus);
          if (currentStatus.status === 'COMPLETED' || currentStatus.status === 'FAILED') {
            if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
            setIsPollingNutrition(false);
          }
        } catch {
          if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
          setIsPollingNutrition(false);
        }
      }, 1200);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Ошибка запуска расчета КБЖУ');
      setIsPollingNutrition(false);
    }
  };

  // Filtering on client side
  const filteredRecipes = recipes.filter((r) => {
    if (selectedCategoryFilter !== 'ALL') {
      return r.category?.name === selectedCategoryFilter;
    }
    return true;
  });

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onRefresh={loadData}
        isLoading={isLoading}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Error notification */}
        {error && (
          <div className="mb-6 p-4 rounded-2xl bg-red-50 border border-red-200 text-red-700 text-sm flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-500" />
              <span>{error}</span>
            </div>
            <button
              onClick={loadData}
              className="text-xs font-bold px-3 py-1 bg-red-100 hover:bg-red-200 rounded-lg transition-colors"
            >
              Повторить
            </button>
          </div>
        )}

        {/* Tab 1: Recipes Catalog */}
        {activeTab === 'recipes' && (
          <div className="space-y-6">
            {/* Hero & Metric highlights */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-orange-50 text-orange-600 rounded-xl">
                  <BookOpen className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Рецепты</span>
                  <span className="text-xl font-bold text-slate-800">{recipes.length}</span>
                </div>
              </div>

              <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-xl">
                  <Carrot className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Ингредиенты</span>
                  <span className="text-xl font-bold text-slate-800">{ingredients.length}</span>
                </div>
              </div>

              <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-blue-50 text-blue-600 rounded-xl">
                  <Layers className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Категории</span>
                  <span className="text-xl font-bold text-slate-800">{categories.length}</span>
                </div>
              </div>

              <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-purple-50 text-purple-600 rounded-xl">
                  <ChefHat className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Авторы</span>
                  <span className="text-xl font-bold text-slate-800">{users.length}</span>
                </div>
              </div>
            </div>

            {/* Actions Bar: Search, Category Filter, Create Button */}
            <div className="bg-white p-4 rounded-3xl border border-slate-200 shadow-xs flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3">
              <form onSubmit={handleSearchSubmit} className="flex-1 flex items-center gap-2">
                <div className="relative flex-1">
                  <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={searchTitle}
                    onChange={(e) => setSearchTitle(e.target.value)}
                    placeholder="Поиск рецептов по названию (GET /api/recipes/search)..."
                    className="w-full pl-10 pr-4 py-2.5 text-xs sm:text-sm rounded-2xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-orange-500 bg-slate-50/50"
                  />
                </div>
                <button
                  type="submit"
                  className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-2xl transition-colors"
                >
                  Поиск
                </button>
              </form>

              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1.5 px-3 py-2 bg-slate-50 border border-slate-200 rounded-2xl text-xs text-slate-600">
                  <Filter className="w-3.5 h-3.5 text-slate-400" />
                  <select
                    value={selectedCategoryFilter}
                    onChange={(e) => setSelectedCategoryFilter(e.target.value)}
                    className="bg-transparent focus:outline-none font-medium cursor-pointer"
                  >
                    <option value="ALL">Все категории</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.name}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  onClick={() => {
                    setEditingRecipe(null);
                    setIsFormOpen(true);
                  }}
                  className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-orange-600 hover:bg-orange-700 text-white text-xs sm:text-sm font-bold rounded-2xl transition-colors shadow-xs"
                >
                  <Plus className="w-4 h-4" />
                  <span>Создать рецепт</span>
                </button>
              </div>
            </div>

            {/* Recipe Grid */}
            {isLoading && recipes.length === 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-pulse">
                {[1, 2, 3, 4, 5, 6].map((i) => (
                  <div key={i} className="h-64 bg-slate-200 rounded-2xl" />
                ))}
              </div>
            ) : filteredRecipes.length === 0 ? (
              <div className="bg-white rounded-3xl p-12 text-center border border-slate-200">
                <div className="w-12 h-12 bg-orange-50 text-orange-500 rounded-2xl flex items-center justify-center mx-auto mb-3">
                  <BookOpen className="w-6 h-6" />
                </div>
                <h3 className="text-base font-bold text-slate-800 mb-1">Рецепты не найдены</h3>
                <p className="text-xs text-slate-500 mb-4">
                  Попробуйте изменить параметры поиска или создайте первый рецепт
                </p>
                <button
                  onClick={() => {
                    setEditingRecipe(null);
                    setIsFormOpen(true);
                  }}
                  className="inline-flex items-center gap-1.5 px-4 py-2 bg-orange-600 text-white text-xs font-bold rounded-xl"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Создать рецепт
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredRecipes.map((recipe) => (
                  <RecipeCard
                    key={recipe.id}
                    recipe={recipe}
                    onView={handleViewRecipe}
                    onEdit={handleEditRecipe}
                    onDelete={handleDeleteRecipe}
                    onCalculateNutrition={handleCalculateNutrition}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 2: Filter Section (JPQL vs Native SQL) */}
        {activeTab === 'filter' && <FilterSection />}

        {/* Tab 3: Concurrency Section (Lab 6 Demos) */}
        {activeTab === 'concurrency' && <ConcurrencySection />}

        {/* Tab 4: Dictionaries Management */}
        {activeTab === 'data' && (
          <CategoryIngredientManager
            categories={categories}
            ingredients={ingredients}
            users={users}
            onRefresh={loadData}
          />
        )}
      </main>

      {/* Recipe Details Modal */}
      <RecipeModal
        recipe={viewingRecipe}
        onClose={() => setViewingRecipe(null)}
        onEdit={(r) => {
          setViewingRecipe(null);
          handleEditRecipe(r);
        }}
        onCalculateNutrition={handleCalculateNutrition}
        nutritionTask={nutritionTask}
        isPollingNutrition={isPollingNutrition}
      />

      {/* Create / Edit Form Modal */}
      <RecipeFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleSubmitRecipe}
        initialRecipe={editingRecipe}
        categories={categories}
        ingredients={ingredients}
        users={users}
      />
    </div>
  );
};
export default App;
