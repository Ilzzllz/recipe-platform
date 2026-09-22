import React, { useState, useEffect, useCallback, useRef } from 'react';
import { api, ApiError } from './api/client';
import { Recipe, Category, Ingredient, User, RecipeCreatePayload, NutritionReportTask, Toast, RecipeFilterPage } from './types';
import { Navbar } from './components/Navbar';
import { RecipeCard } from './components/RecipeCard';
import { RecipeModal } from './components/RecipeModal';
import { RecipeFormModal } from './components/RecipeFormModal';
import { CategoryIngredientManager } from './components/CategoryIngredientManager';
import { ConfirmModal } from './components/ConfirmModal';
import { ToastContainer } from './components/ToastContainer';
import { NutritionModal } from './components/NutritionModal';
import { Plus, Search, Filter, BookOpen, Layers, ChefHat, Carrot, X, SlidersHorizontal, ChevronLeft, ChevronRight, Database } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'recipes' | 'data'>('recipes');

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [users, setUsers] = useState<User[]>([]);

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('ALL');
  const [recipePage, setRecipePage] = useState(0);
  const recipePageSize = 6;

  // The backend already contains JPQL and native SQL queries with Pageable.
  // Keep them accessible from the UI so the feature is useful, not hidden in Swagger.
  const [advancedAuthor, setAdvancedAuthor] = useState('');
  const [advancedCategory, setAdvancedCategory] = useState('');
  const [advancedQueryMode, setAdvancedQueryMode] = useState<'jpql' | 'native'>('jpql');
  const [advancedResults, setAdvancedResults] = useState<RecipeFilterPage | null>(null);
  const [advancedPage, setAdvancedPage] = useState(0);
  const [advancedLoading, setAdvancedLoading] = useState(false);

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
      setRecipePage(0);
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

  useEffect(() => {
    if (!advancedAuthor && users.length > 0) setAdvancedAuthor(users[0].username);
    if (!advancedCategory && categories.length > 0) setAdvancedCategory(categories[0].name);
  }, [users, categories, advancedAuthor, advancedCategory]);

  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      setRecipePage(0);
      loadData();
      return;
    }
    try {
      setIsLoading(true);
      const results = await api.searchRecipesByTitle(searchQuery.trim());
      setRecipes(results);
      setRecipePage(0);
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
    setRecipePage(0);
    loadData();
  };

  const runAdvancedQuery = async (page = 0) => {
    if (!advancedAuthor || !advancedCategory) {
      addToast('warning', 'Выберите автора и категорию для расширенного запроса');
      return;
    }
    try {
      setAdvancedLoading(true);
      const result = advancedQueryMode === 'jpql'
        ? await api.filterRecipesJPQL(advancedAuthor, advancedCategory, page, 5)
        : await api.filterRecipesNative(advancedAuthor, advancedCategory, page, 5);
      setAdvancedResults(result);
      setAdvancedPage(page);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Не удалось выполнить расширенный запрос';
      addToast('error', msg, 'Ошибка запроса');
    } finally {
      setAdvancedLoading(false);
    }
  };

  const openAdvancedRecipe = async (recipeId: number) => {
    try {
      const fullRecipe = await api.getRecipeById(recipeId);
      setViewingRecipe(fullRecipe);
      setNutritionTask(null);
    } catch (err: unknown) {
      addToast('error', err instanceof Error ? err.message : 'Не удалось открыть рецепт');
    }
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
  const totalRecipePages = Math.max(1, Math.ceil(filteredRecipes.length / recipePageSize));
  const visibleRecipes = filteredRecipes.slice(recipePage * recipePageSize, (recipePage + 1) * recipePageSize);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-800 antialiased selection:bg-orange-100 selection:text-orange-900">
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

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'recipes' && (
          <div className="space-y-6">
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
                    onChange={(e) => {
                      setSelectedCategoryFilter(e.target.value);
                      setRecipePage(0);
                    }}
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

            <section className="bg-white rounded-3xl border border-slate-200/80 shadow-xs p-5 sm:p-6">
              <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 mb-4">
                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-2xl bg-indigo-50 text-indigo-600">
                    <SlidersHorizontal className="w-5 h-5" />
                  </div>
                  <div>
                    <h2 className="text-lg font-black text-slate-900">Расширенный поиск</h2>
                    <p className="text-sm text-slate-500 mt-0.5">
                      Сложный запрос к связанным таблицам авторов и категорий с серверной пагинацией
                    </p>
                  </div>
                </div>
                <div className="inline-flex rounded-xl bg-slate-100 p-1 self-start lg:self-auto">
                  <button
                    type="button"
                    onClick={() => setAdvancedQueryMode('jpql')}
                    className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${advancedQueryMode === 'jpql' ? 'bg-white text-indigo-700 shadow-sm' : 'text-slate-500'}`}
                  >
                    JPQL
                  </button>
                  <button
                    type="button"
                    onClick={() => setAdvancedQueryMode('native')}
                    className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${advancedQueryMode === 'native' ? 'bg-white text-indigo-700 shadow-sm' : 'text-slate-500'}`}
                  >
                    SQL
                  </button>
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-[1fr_1fr_auto] gap-3">
                <label className="space-y-1.5">
                  <span className="text-sm font-semibold text-slate-700">Автор</span>
                  <select
                    value={advancedAuthor}
                    onChange={(e) => setAdvancedAuthor(e.target.value)}
                    className="w-full px-3.5 py-3 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="">Выберите автора</option>
                    {users.map((u) => <option key={u.id} value={u.username}>{u.username}</option>)}
                  </select>
                </label>
                <label className="space-y-1.5">
                  <span className="text-sm font-semibold text-slate-700">Категория</span>
                  <select
                    value={advancedCategory}
                    onChange={(e) => setAdvancedCategory(e.target.value)}
                    className="w-full px-3.5 py-3 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="">Выберите категорию</option>
                    {categories.map((c) => <option key={c.id} value={c.name}>{c.name}</option>)}
                  </select>
                </label>
                <button
                  type="button"
                  onClick={() => runAdvancedQuery(0)}
                  disabled={advancedLoading}
                  className="self-end inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold transition-colors disabled:opacity-50"
                >
                  <Database className="w-4 h-4" />
                  {advancedLoading ? 'Выполняем…' : 'Выполнить запрос'}
                </button>
              </div>

              {advancedResults && (
                <div className="mt-5 border-t border-slate-100 pt-4">
                  <div className="flex items-center justify-between gap-3 mb-3">
                    <h3 className="text-base font-bold text-slate-900">Результаты запроса</h3>
                    <span className="text-sm text-slate-500">Найдено: {advancedResults.totalElements}</span>
                  </div>
                  {advancedResults.content.length === 0 ? (
                    <p className="text-sm text-slate-500 py-3">Для выбранных условий рецептов нет.</p>
                  ) : (
                    <div className="space-y-2">
                      {advancedResults.content.map((item) => (
                        <button
                          type="button"
                          key={item.recipeId}
                          onClick={() => openAdvancedRecipe(item.recipeId)}
                          className="w-full text-left p-4 rounded-2xl bg-slate-50 hover:bg-indigo-50 border border-slate-100 hover:border-indigo-200 transition-colors"
                        >
                          <span className="block text-base font-bold text-slate-900">{item.recipeTitle}</span>
                          <span className="block text-sm text-slate-600 mt-1 line-clamp-2">{item.recipeDescription}</span>
                          <span className="block text-sm text-indigo-700 mt-2">Автор: {item.authorUsername} · {item.categoryName}</span>
                        </button>
                      ))}
                    </div>
                  )}
                  {advancedResults.totalPages > 1 && (
                    <div className="flex items-center justify-center gap-3 mt-4">
                      <button
                        type="button"
                        onClick={() => runAdvancedQuery(advancedPage - 1)}
                        disabled={advancedPage === 0 || advancedLoading}
                        className="inline-flex items-center gap-1 px-3 py-2 rounded-xl border border-slate-200 text-sm font-semibold disabled:opacity-40 hover:bg-slate-50"
                      >
                        <ChevronLeft className="w-4 h-4" /> Назад
                      </button>
                      <span className="text-sm font-semibold text-slate-600">Страница {advancedPage + 1} из {advancedResults.totalPages}</span>
                      <button
                        type="button"
                        onClick={() => runAdvancedQuery(advancedPage + 1)}
                        disabled={advancedPage >= advancedResults.totalPages - 1 || advancedLoading}
                        className="inline-flex items-center gap-1 px-3 py-2 rounded-xl border border-slate-200 text-sm font-semibold disabled:opacity-40 hover:bg-slate-50"
                      >
                        Вперёд <ChevronRight className="w-4 h-4" />
                      </button>
                    </div>
                  )}
                </div>
              )}
            </section>

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
                {visibleRecipes.map((recipe) => (
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

            {filteredRecipes.length > 0 && totalRecipePages > 1 && (
              <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bg-white border border-slate-200 rounded-2xl px-4 py-3">
                <span className="text-sm text-slate-500">
                  Показаны {recipePage * recipePageSize + 1}–{Math.min((recipePage + 1) * recipePageSize, filteredRecipes.length)} из {filteredRecipes.length}
                </span>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setRecipePage((page) => Math.max(0, page - 1))}
                    disabled={recipePage === 0}
                    className="inline-flex items-center gap-1 px-3 py-2 rounded-xl border border-slate-200 text-sm font-semibold disabled:opacity-40 hover:bg-slate-50"
                  >
                    <ChevronLeft className="w-4 h-4" /> Назад
                  </button>
                  <span className="text-sm font-semibold text-slate-600">Страница {recipePage + 1} из {totalRecipePages}</span>
                  <button
                    type="button"
                    onClick={() => setRecipePage((page) => Math.min(totalRecipePages - 1, page + 1))}
                    disabled={recipePage >= totalRecipePages - 1}
                    className="inline-flex items-center gap-1 px-3 py-2 rounded-xl border border-slate-200 text-sm font-semibold disabled:opacity-40 hover:bg-slate-50"
                  >
                    Вперёд <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

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

      <NutritionModal
        recipe={nutritionRecipe}
        isOpen={!!nutritionRecipe}
        onClose={() => setNutritionRecipe(null)}
        onRecalculate={handleCalculateNutrition}
        nutritionTask={nutritionTask}
        isPolling={isPollingNutrition}
      />

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

      <RecipeFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleSubmitRecipe}
        initialRecipe={editingRecipe}
        categories={categories}
        ingredients={ingredients}
        users={users}
      />

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

      <ToastContainer toasts={toasts} onDismiss={removeToast} />
    </div>
  );
};

export default App;
