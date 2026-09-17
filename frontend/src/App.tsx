import React, { useState, useEffect, useCallback, useRef } from 'react';
import { api, ApiError } from './api/client';
import { Recipe, Category, Ingredient, User, RecipeCreatePayload, NutritionReportTask, Toast } from './types';
import { Navbar } from './components/Navbar';
import { RecipeCard } from './components/RecipeCard';
import { RecipeModal } from './components/RecipeModal';
import { RecipeFormModal } from './components/RecipeFormModal';
import { CategoryIngredientManager } from './components/CategoryIngredientManager';
import { ConfirmModal } from './components/ConfirmModal';
import { ToastContainer } from './components/ToastContainer';
import { NutritionModal } from './components/NutritionModal';
import { Plus, Search, Filter, BookOpen, Layers, ChefHat, Carrot, X } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'recipes' | 'data'>('recipes');

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [users, setUsers] = useState<User[]>([]);

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('ALL');

  const [isLoading, setIsLoading] = useState(true);

  const [toasts, setToasts] = useState<Toast[]>([]);

  const addToast = useCallback(
    (type: 'success' | 'error' | 'info' | 'warning', message: string, title?: string) => {
      const id = Date.now().toString() + Math.random().toString(36).substring(2, 6);
      setToasts((prev) => [...prev, { id, type, message, title }]);
    },
    []
  );

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingRecipe, setEditingRecipe] = useState<Recipe | null>(null);
  const [viewingRecipe, setViewingRecipe] = useState<Recipe | null>(null);
  const [nutritionRecipe, setNutritionRecipe] = useState<Recipe | null>(null);
  const [recipeToDelete, setRecipeToDelete] = useState<{ id: number; title: string } | null>(null);

  const [nutritionTask, setNutritionTask] = useState<NutritionReportTask | null>(null);
  const [isPollingNutrition, setIsPollingNutrition] = useState(false);
  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const loadData = useCallback(async () => {
    try {
      setIsLoading(true);
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
      const msg = err instanceof Error ? err.message : 'Не удалось подключиться к серверу';
      addToast('error', msg, 'Ошибка загрузки данных');
    } finally {
      setIsLoading(false);
    }
  }, [addToast]);

  useEffect(() => {
    loadData();
    return () => {
      if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
    };
  }, [loadData]);

  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      loadData();
      return;
    }
    try {
      setIsLoading(true);
      const results = await api.searchRecipesByTitle(searchQuery.trim());
      setRecipes(results);
      if (results.length === 0) {
        addToast('info', `По запросу "${searchQuery}" ничего не найдено`);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Ошибка при выполнении поиска';
      addToast('error', msg, 'Поиск не удался');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    loadData();
  };

  const handleViewRecipe = async (recipe: Recipe) => {
    try {
      const fullRecipe = await api.getRecipeById(recipe.id);
      setViewingRecipe(fullRecipe);
      setNutritionTask(null);
    } catch {
      setViewingRecipe(recipe);
    }
  };

  const handleEditRecipe = (recipe: Recipe) => {
    setEditingRecipe(recipe);
    setIsFormOpen(true);
  };

  const handleDeleteRecipePrompt = (id: number, title: string) => {
    setRecipeToDelete({ id, title });
  };

  const handleConfirmDeleteRecipe = async () => {
    if (!recipeToDelete) return;
    try {
      await api.deleteRecipe(recipeToDelete.id);
      setRecipes((prev) => prev.filter((r) => r.id !== recipeToDelete.id));
      if (viewingRecipe?.id === recipeToDelete.id) {
        setViewingRecipe(null);
      }
      addToast('success', `Рецепт "${recipeToDelete.title}" успешно удален`);
    } catch (err: unknown) {
      const msg = err instanceof ApiError ? err.message : 'Не удалось удалить рецепт';
      addToast('error', msg, 'Ошибка удаления');
    } finally {
      setRecipeToDelete(null);
    }
  };

  const handleSubmitRecipe = async (payload: RecipeCreatePayload, id?: number) => {
    if (id) {
      const updated = await api.updateRecipe(id, payload);
      setRecipes((prev) => prev.map((r) => (r.id === id ? updated : r)));
      if (viewingRecipe?.id === id) {
        setViewingRecipe(updated);
      }
      addToast('success', `Рецепт "${updated.title}" успешно обновлен`);
    } else {
      const created = await api.createRecipe(payload);
      setRecipes((prev) => [created, ...prev]);
      addToast('success', `Рецепт "${created.title}" успешно создан`);
    }
  };

  const handleCalculateNutrition = async (recipe: Recipe) => {
    try {
      setIsPollingNutrition(true);
      const task = await api.startNutritionReport(recipe.id);
      setNutritionTask(task);
      addToast('info', `Запущен расчет КБЖУ для "${recipe.title}"...`);

      if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);

      pollingIntervalRef.current = setInterval(async () => {
        try {
          const current = await api.getNutritionReportStatus(task.taskId);
          setNutritionTask(current);
          if (current.status === 'COMPLETED') {
            if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
            setIsPollingNutrition(false);
            addToast('success', `Расчет КБЖУ для "${recipe.title}" готов!`);
          } else if (current.status === 'FAILED') {
            if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
            setIsPollingNutrition(false);
            addToast('warning', current.errorMessage || 'Расчет завершился с предупреждением');
          }
        } catch {
          if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
          setIsPollingNutrition(false);
        }
      }, 1000);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Ошибка при запуске расчета';
      addToast('error', msg, 'Ошибка сервиса');
      setIsPollingNutrition(false);
    }
  };

  const handleCardCalculateNutrition = (recipe: Recipe) => {
    setNutritionRecipe(recipe);
    handleCalculateNutrition(recipe);
  };

  const filteredRecipes = recipes.filter((r) => {
    if (selectedCategoryFilter !== 'ALL') {
      return r.category?.name === selectedCategoryFilter;
    }
    return true;
  });

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-800 antialiased selection:bg-orange-100 selection:text-orange-900">
      {/* Top Navbar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onRefresh={loadData}
        onOpenCreateModal={() => {
          setEditingRecipe(null);
          setIsFormOpen(true);
        }}
        isLoading={isLoading}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'recipes' && (
          <div className="space-y-6">
            {/* Metric counters summary */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-white p-4 rounded-3xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-orange-50 text-orange-600 rounded-2xl">
                  <BookOpen className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Рецептов</span>
                  <span className="text-xl font-black text-slate-900">{recipes.length}</span>
                </div>
              </div>

              <div className="bg-white p-4 rounded-3xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-2xl">
                  <Carrot className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Ингредиентов</span>
                  <span className="text-xl font-black text-slate-900">{ingredients.length}</span>
                </div>
              </div>

              <div className="bg-white p-4 rounded-3xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-blue-50 text-blue-600 rounded-2xl">
                  <Layers className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Категорий</span>
                  <span className="text-xl font-black text-slate-900">{categories.length}</span>
                </div>
              </div>

              <div className="bg-white p-4 rounded-3xl border border-slate-200/80 shadow-xs flex items-center gap-3">
                <div className="p-2.5 bg-purple-50 text-purple-600 rounded-2xl">
                  <ChefHat className="w-5 h-5" />
                </div>
                <div>
                  <span className="block text-xs text-slate-400 font-medium">Авторов</span>
                  <span className="text-xl font-black text-slate-900">{users.length}</span>
                </div>
              </div>
            </div>

            {/* Actions Bar: Search, Category Filter, Create Button */}
            <div className="bg-white p-4 rounded-3xl border border-slate-200/80 shadow-xs flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3">
              <form onSubmit={handleSearchSubmit} className="flex-1 flex items-center gap-2">
                <div className="relative flex-1">
                  <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Поиск рецептов по названию блюда..."
                    className="w-full pl-10 pr-9 py-2.5 text-xs sm:text-sm rounded-2xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-orange-500 bg-slate-50/50"
                  />
                  {searchQuery && (
                    <button
                      type="button"
                      onClick={handleClearSearch}
                      className="p-1 text-slate-400 hover:text-slate-600 absolute right-2.5 top-1/2 -translate-y-1/2"
                      title="Очистить поиск"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  )}
                </div>
                <button
                  type="submit"
                  className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs sm:text-sm font-semibold rounded-2xl transition-colors"
                >
                  Найти
                </button>
              </form>

              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1.5 px-3.5 py-2.5 bg-slate-50/80 border border-slate-200 rounded-2xl text-xs text-slate-600">
                  <Filter className="w-3.5 h-3.5 text-slate-400" />
                  <select
                    value={selectedCategoryFilter}
                    onChange={(e) => setSelectedCategoryFilter(e.target.value)}
                    className="bg-transparent focus:outline-none font-semibold text-slate-700 cursor-pointer"
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
                  <span>Создать</span>
                </button>
              </div>
            </div>

            {/* Recipes Grid */}
            {isLoading && recipes.length === 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-pulse">
                {[1, 2, 3, 4, 5, 6].map((i) => (
                  <div key={i} className="h-64 bg-slate-200/80 rounded-3xl" />
                ))}
              </div>
            ) : filteredRecipes.length === 0 ? (
              <div className="bg-white rounded-3xl p-12 text-center border border-slate-200 shadow-xs">
                <div className="w-14 h-14 bg-orange-50 text-orange-500 rounded-2xl flex items-center justify-center mx-auto mb-3 shadow-xs">
                  <BookOpen className="w-7 h-7" />
                </div>
                <h3 className="text-base font-bold text-slate-900 mb-1">Рецепты не найдены</h3>
                <p className="text-xs text-slate-500 mb-5 max-w-sm mx-auto">
                  Ничего не найдено по текущим фильтрам. Измените параметры поиска или создайте свой первый рецепт!
                </p>
                <button
                  onClick={() => {
                    setEditingRecipe(null);
                    setIsFormOpen(true);
                  }}
                  className="inline-flex items-center gap-1.5 px-5 py-2.5 bg-orange-600 hover:bg-orange-700 text-white text-xs sm:text-sm font-bold rounded-2xl shadow-xs transition-colors"
                >
                  <Plus className="w-4 h-4" />
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
                    onDelete={handleDeleteRecipePrompt}
                    onCalculateNutrition={handleCardCalculateNutrition}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 2: Dictionaries Management */}
        {activeTab === 'data' && (
          <CategoryIngredientManager
            categories={categories}
            ingredients={ingredients}
            users={users}
            onRefresh={loadData}
            onToast={addToast}
          />
        )}
      </main>

      {/* Dedicated Nutrition Modal for Direct Card Action */}
      <NutritionModal
        recipe={nutritionRecipe}
        isOpen={!!nutritionRecipe}
        onClose={() => setNutritionRecipe(null)}
        onRecalculate={handleCalculateNutrition}
        nutritionTask={nutritionTask}
        isPolling={isPollingNutrition}
      />

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

      {/* Modern Confirm Recipe Deletion Modal */}
      <ConfirmModal
        isOpen={!!recipeToDelete}
        title="Удалить рецепт?"
        message={`Вы уверены, что хотите удалить рецепт "${recipeToDelete?.title}"? Все связанные шаги приготовления также будут безвозвратно удалены.`}
        confirmText="Да, удалить"
        cancelText="Отмена"
        isDangerous={true}
        onConfirm={handleConfirmDeleteRecipe}
        onCancel={() => setRecipeToDelete(null)}
      />

      {/* Floating Modern Toast Alerts */}
      <ToastContainer toasts={toasts} onDismiss={removeToast} />
    </div>
  );
};

export default App;
