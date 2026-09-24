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
import { Plus, Search, Filter, BookOpen, Layers, ChefHat, Carrot, X, ChevronLeft, ChevronRight, ListPlus } from 'lucide-react';
import { BulkRecipeModal } from './components/BulkRecipeModal';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'recipes' | 'data'>('recipes');

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [users, setUsers] = useState<User[]>([]);

  const [searchQuery, setSearchQuery] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');
  const [selectedAuthors, setSelectedAuthors] = useState<string[]>([]);
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [caloriesMin, setCaloriesMin] = useState('');
  const [caloriesMax, setCaloriesMax] = useState('');
  const [proteinsMin, setProteinsMin] = useState('');
  const [proteinsMax, setProteinsMax] = useState('');
  const [fatsMin, setFatsMin] = useState('');
  const [fatsMax, setFatsMax] = useState('');
  const [carbsMin, setCarbsMin] = useState('');
  const [carbsMax, setCarbsMax] = useState('');
  const [recipePage, setRecipePage] = useState(0);
  const recipePageSize = 6;

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
  const [isBulkOpen, setIsBulkOpen] = useState(false);
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

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setAppliedSearch(searchQuery.trim());
    setRecipePage(0);
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    setAppliedSearch('');
    setSelectedAuthors([]);
    setSelectedCategories([]);
    setCaloriesMin('');
    setCaloriesMax('');
    setProteinsMin('');
    setProteinsMax('');
    setFatsMin('');
    setFatsMax('');
    setCarbsMin('');
    setCarbsMax('');
    setRecipePage(0);
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

  const handleSubmitBulk = async (payload: RecipeCreatePayload[]) => {
    try {
      const created = await api.createRecipesBulk(payload);
      setRecipes((prev) => [...created, ...prev]);
      setIsBulkOpen(false);
      addToast('success', `Добавлено рецептов: ${created.length}`);
    } catch (error: unknown) {
      addToast('error', error instanceof Error ? error.message : 'Не удалось добавить рецепты', 'Ошибка массового добавления');
      throw error;
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

  const inRange = (value: number | undefined, min: string, max: string) => {
    const actual = value ?? 0;
    if (min !== '' && actual < Number(min)) return false;
    if (max !== '' && actual > Number(max)) return false;
    return true;
  };

  const filteredRecipes = recipes.filter((r) => {
    const titleMatches = !appliedSearch || r.title.toLocaleLowerCase().includes(appliedSearch.toLocaleLowerCase());
    const authorMatches = selectedAuthors.length === 0 || selectedAuthors.includes(r.author?.username || '');
    const categoryMatches = selectedCategories.length === 0 || selectedCategories.includes(r.category?.name || '');
    const caloriesMatch = inRange(r.nutrition?.caloriesPerPortion, caloriesMin, caloriesMax);
    const proteinsMatch = inRange(r.nutrition?.proteinsPerPortion, proteinsMin, proteinsMax);
    const fatsMatch = inRange(r.nutrition?.fatsPerPortion, fatsMin, fatsMax);
    const carbsMatch = inRange(r.nutrition?.carbohydratesPerPortion, carbsMin, carbsMax);
    return (
      titleMatches &&
      authorMatches &&
      categoryMatches &&
      caloriesMatch &&
      proteinsMatch &&
      fatsMatch &&
      carbsMatch
    );
  });
  const totalRecipePages = Math.max(1, Math.ceil(filteredRecipes.length / recipePageSize));
  const visibleRecipes = filteredRecipes.slice(recipePage * recipePageSize, (recipePage + 1) * recipePageSize);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-800 antialiased selection:bg-orange-100 selection:text-orange-900">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onRefresh={loadData}
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

            <div className="bg-white p-5 rounded-3xl border border-slate-200/80 shadow-xs space-y-4">
              <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row gap-3">
                <div className="relative flex-1">
                  <Search className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input type="text" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} placeholder="Найти блюдо по названию" className="w-full pl-11 pr-10 py-3 text-base rounded-2xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-orange-500 bg-slate-50/50" />
                  {searchQuery && <button type="button" onClick={() => setSearchQuery('')} className="p-1 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2"><X className="w-5 h-5" /></button>}
                </div>
                <button type="submit" className="px-6 py-3 bg-orange-600 hover:bg-orange-700 text-white text-base font-bold rounded-2xl">Найти</button>
                <button type="button" onClick={handleClearSearch} className="px-5 py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 text-base font-semibold rounded-2xl">Сбросить</button>
              </form>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label className="space-y-1.5"><span className="flex items-center gap-2 text-base font-semibold text-slate-700"><Filter className="w-4 h-4" />Авторы</span><select multiple value={selectedAuthors} onChange={(e) => { setSelectedAuthors(Array.from(e.target.selectedOptions, (option) => option.value)); setRecipePage(0); }} className="w-full min-h-24 px-3 py-2 rounded-2xl border border-slate-200 bg-slate-50/70 text-base focus:outline-none focus:ring-2 focus:ring-orange-500">{users.map((user) => <option key={user.id} value={user.username}>{user.username}</option>)}</select><span className="text-sm text-slate-500">Можно выбрать несколько авторов</span></label>
                <label className="space-y-1.5"><span className="flex items-center gap-2 text-base font-semibold text-slate-700"><Filter className="w-4 h-4" />Категории</span><select multiple value={selectedCategories} onChange={(e) => { setSelectedCategories(Array.from(e.target.selectedOptions, (option) => option.value)); setRecipePage(0); }} className="w-full min-h-24 px-3 py-2 rounded-2xl border border-slate-200 bg-slate-50/70 text-base focus:outline-none focus:ring-2 focus:ring-orange-500">{categories.map((category) => <option key={category.id} value={category.name}>{category.name}</option>)}</select><span className="text-sm text-slate-500">Можно выбрать несколько категорий</span></label>
              </div>
              <div className="space-y-2">
                <span className="flex items-center gap-2 text-base font-semibold text-slate-700"><Filter className="w-4 h-4" />КБЖУ на порцию</span>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  <div className="space-y-1">
                    <span className="text-xs font-medium text-slate-500">Калории, ккал</span>
                    <div className="flex items-center gap-1.5">
                      <input type="number" min={0} value={caloriesMin} onChange={(e) => { setCaloriesMin(e.target.value); setRecipePage(0); }} placeholder="от" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                      <input type="number" min={0} value={caloriesMax} onChange={(e) => { setCaloriesMax(e.target.value); setRecipePage(0); }} placeholder="до" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                    </div>
                  </div>
                  <div className="space-y-1">
                    <span className="text-xs font-medium text-slate-500">Белки, г</span>
                    <div className="flex items-center gap-1.5">
                      <input type="number" min={0} value={proteinsMin} onChange={(e) => { setProteinsMin(e.target.value); setRecipePage(0); }} placeholder="от" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                      <input type="number" min={0} value={proteinsMax} onChange={(e) => { setProteinsMax(e.target.value); setRecipePage(0); }} placeholder="до" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                    </div>
                  </div>
                  <div className="space-y-1">
                    <span className="text-xs font-medium text-slate-500">Жиры, г</span>
                    <div className="flex items-center gap-1.5">
                      <input type="number" min={0} value={fatsMin} onChange={(e) => { setFatsMin(e.target.value); setRecipePage(0); }} placeholder="от" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                      <input type="number" min={0} value={fatsMax} onChange={(e) => { setFatsMax(e.target.value); setRecipePage(0); }} placeholder="до" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                    </div>
                  </div>
                  <div className="space-y-1">
                    <span className="text-xs font-medium text-slate-500">Углеводы, г</span>
                    <div className="flex items-center gap-1.5">
                      <input type="number" min={0} value={carbsMin} onChange={(e) => { setCarbsMin(e.target.value); setRecipePage(0); }} placeholder="от" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                      <input type="number" min={0} value={carbsMax} onChange={(e) => { setCarbsMax(e.target.value); setRecipePage(0); }} placeholder="до" className="w-full px-2.5 py-2 rounded-xl border border-slate-200 bg-slate-50/70 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500" />
                    </div>
                  </div>
                </div>
              </div>
              <div className="flex flex-wrap gap-3"><button type="button" onClick={() => { setEditingRecipe(null); setIsFormOpen(true); }} className="inline-flex items-center gap-2 px-5 py-3 bg-orange-600 hover:bg-orange-700 text-white text-base font-bold rounded-2xl"><Plus className="w-5 h-5" />Создать рецепт</button><button type="button" onClick={() => setIsBulkOpen(true)} className="inline-flex items-center gap-2 px-5 py-3 bg-indigo-600 hover:bg-indigo-700 text-white text-base font-bold rounded-2xl"><ListPlus className="w-5 h-5" />Добавить несколько</button></div>
            </div>


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

      <BulkRecipeModal
        isOpen={isBulkOpen}
        onClose={() => setIsBulkOpen(false)}
        onSubmit={handleSubmitBulk}
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
