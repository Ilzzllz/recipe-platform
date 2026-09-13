import React from 'react';
import { Recipe, NutritionReportTask } from '../types';
import { X, ChefHat, Carrot, Sparkles, Loader2, Database, Network } from 'lucide-react';

interface RecipeModalProps {
  recipe: Recipe | null;
  onClose: () => void;
  onEdit: (recipe: Recipe) => void;
  onCalculateNutrition: (recipe: Recipe) => void;
  nutritionTask: NutritionReportTask | null;
  isPollingNutrition: boolean;
}

export const RecipeModal: React.FC<RecipeModalProps> = ({
  recipe,
  onClose,
  onEdit,
  onCalculateNutrition,
  nutritionTask,
  isPollingNutrition,
}) => {
  if (!recipe) return null;

  const sortedSteps = [...(recipe.steps || [])].sort((a, b) => a.stepOrder - b.stepOrder);

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 sm:p-6 animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl shadow-2xl max-w-3xl w-full max-h-[90vh] flex flex-col overflow-hidden border border-slate-200">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-orange-100 text-orange-800">
                {recipe.category?.name || 'Без категории'}
              </span>
              <span className="text-xs text-slate-400 font-mono">ID: #{recipe.id}</span>
            </div>
            <h2 className="text-2xl font-bold text-slate-900">{recipe.title}</h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-6">
          {/* Author & Description */}
          <div className="bg-slate-50 rounded-2xl p-4 border border-slate-200/60">
            <div className="flex items-center gap-2 mb-2 text-sm text-slate-700">
              <ChefHat className="w-4 h-4 text-orange-500" />
              <span>Автор рецепта:</span>
              <strong className="text-slate-900">{recipe.author?.username}</strong>
            </div>
            <p className="text-sm text-slate-600 leading-relaxed">{recipe.description}</p>
          </div>

          {/* ManyToMany Relationship: Ingredients */}
          <div className="rounded-2xl border-2 border-emerald-100 bg-emerald-50/30 p-5">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="p-1.5 bg-emerald-100 text-emerald-800 rounded-lg">
                  <Network className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-emerald-950">
                    Ингредиенты (Связь ManyToMany)
                  </h4>
                  <p className="text-xs text-emerald-700">
                    Таблица связей <code className="bg-emerald-100/80 px-1 rounded">recipe_ingredients</code> (Recipe ⟷ Ingredient)
                  </p>
                </div>
              </div>
              <span className="text-xs font-bold text-emerald-800 bg-emerald-100 px-2.5 py-1 rounded-full">
                Всего: {recipe.ingredients?.length || 0}
              </span>
            </div>

            <div className="flex flex-wrap gap-2 pt-1">
              {recipe.ingredients && recipe.ingredients.length > 0 ? (
                recipe.ingredients.map((ingredient) => (
                  <span
                    key={ingredient.id}
                    className="inline-flex items-center gap-1.5 text-xs font-medium px-3 py-1.5 rounded-xl bg-white text-emerald-900 border border-emerald-200 shadow-xs"
                  >
                    <Carrot className="w-3.5 h-3.5 text-emerald-600" />
                    {ingredient.name}
                    <span className="text-[10px] text-emerald-400 font-mono">#{ingredient.id}</span>
                  </span>
                ))
              ) : (
                <p className="text-xs text-slate-500 italic">Ингредиенты не добавлены.</p>
              )}
            </div>
          </div>

          {/* OneToMany Relationship: Cooking Steps */}
          <div className="rounded-2xl border-2 border-blue-100 bg-blue-50/30 p-5">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <div className="p-1.5 bg-blue-100 text-blue-800 rounded-lg">
                  <Database className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-blue-950">
                    Шаги приготовления (Связь OneToMany)
                  </h4>
                  <p className="text-xs text-blue-700">
                    Внешний ключ <code className="bg-blue-100/80 px-1 rounded">cooking_steps.recipe_id</code> с каскадным обновлением/удалением
                  </p>
                </div>
              </div>
              <span className="text-xs font-bold text-blue-800 bg-blue-100 px-2.5 py-1 rounded-full">
                {sortedSteps.length} {sortedSteps.length === 1 ? 'шаг' : 'шагов'}
              </span>
            </div>

            <div className="space-y-3">
              {sortedSteps.length > 0 ? (
                sortedSteps.map((step, idx) => (
                  <div
                    key={step.id || idx}
                    className="flex items-start gap-3 bg-white p-3.5 rounded-xl border border-blue-100 shadow-xs"
                  >
                    <span className="flex-shrink-0 w-7 h-7 rounded-lg bg-blue-600 text-white font-bold text-xs flex items-center justify-center shadow-xs">
                      {step.stepOrder}
                    </span>
                    <p className="text-sm text-slate-700 leading-relaxed pt-0.5">{step.description}</p>
                  </div>
                ))
              ) : (
                <p className="text-xs text-slate-500 italic">Шаги приготовления пока не указаны.</p>
              )}
            </div>
          </div>

          {/* Async Nutrition Calculation (Lab 6 Feature) */}
          <div className="rounded-2xl border-2 border-purple-100 bg-purple-50/40 p-5">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="p-1.5 bg-purple-100 text-purple-800 rounded-lg">
                  <Sparkles className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-purple-950">
                    Асинхронный расчет КБЖУ (Лабораторная 6)
                  </h4>
                  <p className="text-xs text-purple-700">
                    @Async + CompletableFuture с запросом к живому API Open Food Facts
                  </p>
                </div>
              </div>

              <button
                onClick={() => onCalculateNutrition(recipe)}
                disabled={isPollingNutrition}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-purple-600 hover:bg-purple-700 rounded-xl transition-colors shadow-xs disabled:opacity-50"
              >
                {isPollingNutrition ? (
                  <>
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    Выполняется расчет...
                  </>
                ) : (
                  <>
                    <Sparkles className="w-3.5 h-3.5" />
                    Рассчитать КБЖУ
                  </>
                )}
              </button>
            </div>

            {/* Task Status & Results */}
            {nutritionTask && (
              <div className="mt-4 pt-4 border-t border-purple-200">
                <div className="flex items-center justify-between text-xs text-purple-900 mb-3">
                  <span>Статус задачи: <strong>{nutritionTask.status}</strong></span>
                  <span className="font-mono text-purple-600">ID: {nutritionTask.taskId.slice(0, 8)}...</span>
                </div>

                {nutritionTask.status === 'IN_PROGRESS' && (
                  <div className="p-4 bg-purple-100/60 rounded-xl text-center text-xs text-purple-900 flex items-center justify-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin text-purple-600" />
                    <span>Фоновый поток запрашивает калорийность ингредиентов через Open Food Facts API...</span>
                  </div>
                )}

                {nutritionTask.status === 'COMPLETED' && nutritionTask.report && (
                  <div className="space-y-4">
                    {/* Summary Badges */}
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                      <div className="bg-white p-2.5 rounded-xl border border-purple-200 text-center">
                        <span className="block text-[11px] text-slate-500">Калории</span>
                        <strong className="text-sm text-purple-700">
                          {nutritionTask.report.totalCaloriesKcal} ккал
                        </strong>
                      </div>
                      <div className="bg-white p-2.5 rounded-xl border border-purple-200 text-center">
                        <span className="block text-[11px] text-slate-500">Белки</span>
                        <strong className="text-sm text-purple-700">
                          {nutritionTask.report.totalProteinsGrams} г
                        </strong>
                      </div>
                      <div className="bg-white p-2.5 rounded-xl border border-purple-200 text-center">
                        <span className="block text-[11px] text-slate-500">Жиры</span>
                        <strong className="text-sm text-purple-700">
                          {nutritionTask.report.totalFatGrams} г
                        </strong>
                      </div>
                      <div className="bg-white p-2.5 rounded-xl border border-purple-200 text-center">
                        <span className="block text-[11px] text-slate-500">Углеводы</span>
                        <strong className="text-sm text-purple-700">
                          {nutritionTask.report.totalCarbohydratesGrams} г
                        </strong>
                      </div>
                    </div>

                    {/* Breakdown by ingredients */}
                    <div className="bg-white rounded-xl border border-purple-100 p-3 overflow-x-auto">
                      <table className="w-full text-left text-xs">
                        <thead>
                          <tr className="border-b border-slate-100 text-slate-400">
                            <th className="pb-2 font-medium">Ингредиент</th>
                            <th className="pb-2 font-medium text-right">Ккал</th>
                            <th className="pb-2 font-medium text-right">Белки</th>
                            <th className="pb-2 font-medium text-right">Жиры</th>
                            <th className="pb-2 font-medium text-right">Углеводы</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 text-slate-700">
                          {nutritionTask.report.ingredientsData.map((item, i) => (
                            <tr key={i}>
                              <td className="py-2 flex items-center gap-1.5">
                                <span className={`w-1.5 h-1.5 rounded-full ${item.foundInOpenFoodFacts ? 'bg-emerald-500' : 'bg-amber-400'}`} />
                                {item.ingredientName}
                              </td>
                              <td className="py-2 text-right font-medium">{item.caloriesKcal}</td>
                              <td className="py-2 text-right">{item.proteinsGrams}</td>
                              <td className="py-2 text-right">{item.fatGrams}</td>
                              <td className="py-2 text-right">{item.carbohydratesGrams}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}

                {nutritionTask.status === 'FAILED' && (
                  <div className="p-3 bg-red-50 text-red-700 rounded-xl text-xs">
                    Ошибка расчета: {nutritionTask.errorMessage || 'Не удалось получить данные от внешнего сервиса'}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-slate-50 border-t border-slate-200 flex items-center justify-between">
          <button
            onClick={() => {
              onClose();
              onEdit(recipe);
            }}
            className="px-4 py-2 text-sm font-semibold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-100 transition-colors"
          >
            Редактировать
          </button>
          <button
            onClick={onClose}
            className="px-5 py-2 text-sm font-semibold text-white bg-slate-900 hover:bg-slate-800 rounded-xl transition-colors"
          >
            Закрыть
          </button>
        </div>
      </div>
    </div>
  );
};
