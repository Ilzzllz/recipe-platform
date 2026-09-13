import React, { useState } from 'react';
import { Recipe, NutritionReportTask } from '../types';
import { X, ChefHat, Carrot, Sparkles, Loader2, ListOrdered, CheckCircle2, Circle, Edit2 } from 'lucide-react';

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
  const [completedStepIds, setCompletedStepIds] = useState<number[]>([]);

  if (!recipe) return null;

  const sortedSteps = [...(recipe.steps || [])].sort((a, b) => a.stepOrder - b.stepOrder);

  const toggleStepCompleted = (index: number) => {
    setCompletedStepIds((prev) =>
      prev.includes(index) ? prev.filter((i) => i !== index) : [...prev, index]
    );
  };

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
              <span className="text-xs text-slate-400 font-mono">#{recipe.id}</span>
            </div>
            <h2 className="text-2xl font-black text-slate-900">{recipe.title}</h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Scrollable Content */}
        <div className="p-6 overflow-y-auto space-y-6">
          {/* Author & Description */}
          <div className="bg-slate-50/80 rounded-2xl p-4 border border-slate-200/70">
            <div className="flex items-center gap-2 mb-2 text-sm text-slate-700">
              <div className="w-6 h-6 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center">
                <ChefHat className="w-3.5 h-3.5" />
              </div>
              <span className="text-xs text-slate-500">Автор:</span>
              <strong className="text-slate-900">{recipe.author?.username || 'Не указан'}</strong>
            </div>
            <p className="text-sm text-slate-600 leading-relaxed">{recipe.description}</p>
          </div>

          {/* Ingredients List */}
          <div className="rounded-2xl border border-emerald-200/80 bg-emerald-50/30 p-5">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="p-1.5 bg-emerald-100 text-emerald-800 rounded-xl">
                  <Carrot className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-emerald-950">Необходимые ингредиенты</h4>
                  <p className="text-xs text-emerald-700">Продукты, входящие в состав блюда</p>
                </div>
              </div>
              <span className="text-xs font-bold text-emerald-800 bg-emerald-100 px-2.5 py-1 rounded-full">
                {recipe.ingredients?.length || 0} шт.
              </span>
            </div>

            <div className="flex flex-wrap gap-2 pt-1">
              {recipe.ingredients && recipe.ingredients.length > 0 ? (
                recipe.ingredients.map((ingredient) => (
                  <span
                    key={ingredient.id}
                    className="inline-flex items-center gap-1.5 text-xs font-medium px-3 py-1.5 rounded-xl bg-white text-emerald-900 border border-emerald-200 shadow-xs"
                  >
                    <Carrot className="w-3 h-3 text-emerald-600" />
                    {ingredient.name}
                  </span>
                ))
              ) : (
                <p className="text-xs text-slate-500 italic">Ингредиенты не добавлены к рецепту.</p>
              )}
            </div>
          </div>

          {/* Step-by-step Cooking Guide with interactive checkboxes */}
          <div className="rounded-2xl border border-blue-200/80 bg-blue-50/30 p-5">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <div className="p-1.5 bg-blue-100 text-blue-800 rounded-xl">
                  <ListOrdered className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-blue-950">Пошаговый процесс приготовления</h4>
                  <p className="text-xs text-blue-700">
                    Нажимайте на шаги, чтобы отмечать их по мере готовки
                  </p>
                </div>
              </div>
              <span className="text-xs font-bold text-blue-800 bg-blue-100 px-2.5 py-1 rounded-full">
                {completedStepIds.length} из {sortedSteps.length} готово
              </span>
            </div>

            <div className="space-y-2.5">
              {sortedSteps.length > 0 ? (
                sortedSteps.map((step, idx) => {
                  const isDone = completedStepIds.includes(idx);
                  return (
                    <div
                      key={step.id || idx}
                      onClick={() => toggleStepCompleted(idx)}
                      className={`flex items-start gap-3 p-3.5 rounded-xl border transition-all cursor-pointer select-none ${
                        isDone
                          ? 'bg-emerald-50/60 border-emerald-200 text-slate-500'
                          : 'bg-white border-blue-100/80 shadow-xs hover:border-blue-300'
                      }`}
                    >
                      <button
                        type="button"
                        className="mt-0.5 text-slate-400 focus:outline-none"
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleStepCompleted(idx);
                        }}
                      >
                        {isDone ? (
                          <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                        ) : (
                          <Circle className="w-5 h-5 text-slate-300 hover:text-blue-500" />
                        )}
                      </button>

                      <div className="flex-1">
                        <span className="inline-block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-0.5">
                          Шаг {step.stepOrder}
                        </span>
                        <p
                          className={`text-sm leading-relaxed ${
                            isDone ? 'line-through text-slate-400' : 'text-slate-800'
                          }`}
                        >
                          {step.description}
                        </p>
                      </div>
                    </div>
                  );
                })
              ) : (
                <p className="text-xs text-slate-500 italic">Шаги приготовления пока не указаны.</p>
              )}
            </div>
          </div>

          {/* Nutrition Calculation Section */}
          <div className="rounded-2xl border border-purple-200/80 bg-purple-50/40 p-5">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3">
              <div className="flex items-center gap-2">
                <div className="p-1.5 bg-purple-100 text-purple-800 rounded-xl">
                  <Sparkles className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-purple-950">
                    Пищевая ценность и калорийность (КБЖУ)
                  </h4>
                  <p className="text-xs text-purple-700">
                    Асинхронный расчет нутриентов через Open Food Facts
                  </p>
                </div>
              </div>

              <button
                onClick={() => onCalculateNutrition(recipe)}
                disabled={isPollingNutrition}
                className="inline-flex items-center justify-center gap-1.5 px-3.5 py-2 text-xs font-bold text-white bg-purple-600 hover:bg-purple-700 rounded-xl transition-colors shadow-xs disabled:opacity-50"
              >
                {isPollingNutrition ? (
                  <>
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    Расчет данных...
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
              <div className="mt-4 pt-4 border-t border-purple-200/60">
                {nutritionTask.status === 'IN_PROGRESS' && (
                  <div className="p-4 bg-purple-100/60 rounded-2xl text-center text-xs text-purple-900 flex items-center justify-center gap-2">
                    <Loader2 className="w-4 h-4 animate-spin text-purple-600" />
                    <span>Идет анализ пищевой ценности ингредиентов...</span>
                  </div>
                )}

                {nutritionTask.status === 'COMPLETED' && (nutritionTask.result || nutritionTask.report) && (() => {
                  const report = nutritionTask.result || nutritionTask.report;
                  if (!report) return null;
                  const totalFats = report.totalFatsGrams ?? report.totalFatGrams ?? 0;
                  const ingredientsList = report.ingredients || report.ingredientsData || [];

                  return (
                    <div className="space-y-4">
                      {/* Summary Badges */}
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                        <div className="bg-white p-3 rounded-2xl border border-purple-200 text-center shadow-xs">
                          <span className="block text-[11px] text-slate-500 font-medium">Калории</span>
                          <strong className="text-base text-purple-700 font-bold">
                            {report.totalCaloriesKcal} ккал
                          </strong>
                        </div>
                        <div className="bg-white p-3 rounded-2xl border border-purple-200 text-center shadow-xs">
                          <span className="block text-[11px] text-slate-500 font-medium">Белки</span>
                          <strong className="text-base text-purple-700 font-bold">
                            {report.totalProteinsGrams} г
                          </strong>
                        </div>
                        <div className="bg-white p-3 rounded-2xl border border-purple-200 text-center shadow-xs">
                          <span className="block text-[11px] text-slate-500 font-medium">Жиры</span>
                          <strong className="text-base text-purple-700 font-bold">
                            {totalFats} г
                          </strong>
                        </div>
                        <div className="bg-white p-3 rounded-2xl border border-purple-200 text-center shadow-xs">
                          <span className="block text-[11px] text-slate-500 font-medium">Углеводы</span>
                          <strong className="text-base text-purple-700 font-bold">
                            {report.totalCarbohydratesGrams} г
                          </strong>
                        </div>
                      </div>

                      {/* Breakdown by ingredients */}
                      <div className="bg-white rounded-2xl border border-purple-100 p-3.5 overflow-x-auto shadow-xs">
                        <table className="w-full text-left text-xs">
                          <thead>
                            <tr className="border-b border-slate-100 text-slate-400 font-medium">
                              <th className="pb-2">Ингредиент</th>
                              <th className="pb-2 text-right">Ккал</th>
                              <th className="pb-2 text-right">Белки</th>
                              <th className="pb-2 text-right">Жиры</th>
                              <th className="pb-2 text-right">Углеводы</th>
                              <th className="pb-2 text-right">Источник</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100 text-slate-700">
                            {ingredientsList.map((item, i) => {
                              const itemFats = item.fatsGrams ?? item.fatGrams ?? 0;
                              const isOpenFoodFacts =
                                item.dataSource?.includes('Open Food Facts') || item.foundInOpenFoodFacts;

                              return (
                                <tr key={i} className="hover:bg-slate-50/50">
                                  <td className="py-2.5 flex items-center gap-2">
                                    <span
                                      className={`w-2 h-2 rounded-full ${
                                        isOpenFoodFacts ? 'bg-emerald-500' : 'bg-amber-400'
                                      }`}
                                      title={
                                        isOpenFoodFacts
                                          ? 'Найдено в Open Food Facts'
                                          : 'Кулинарная оценка'
                                      }
                                    />
                                    <span className="font-medium text-slate-800">
                                      {item.ingredientName}
                                    </span>
                                  </td>
                                  <td className="py-2.5 text-right font-medium text-slate-900">
                                    {item.caloriesKcal}
                                  </td>
                                  <td className="py-2.5 text-right">{item.proteinsGrams} г</td>
                                  <td className="py-2.5 text-right">{itemFats} г</td>
                                  <td className="py-2.5 text-right">{item.carbohydratesGrams} г</td>
                                  <td className="py-2.5 text-right text-[10px] text-slate-400">
                                    {isOpenFoodFacts ? 'Open Food Facts' : 'Оценка'}
                                  </td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>

                      {/* Explanation Note for Data Sources */}
                      <div className="p-3 bg-white rounded-xl border border-purple-100 text-xs text-slate-600 leading-relaxed space-y-1">
                        <span className="font-bold text-slate-800 text-[11px] block">
                          Что означают источники данных?
                        </span>
                        <p className="text-[11px]">
                          <strong className="text-emerald-700">Open Food Facts</strong> — точные данные найдены в открытой мировой базе продуктов.
                        </p>
                        <p className="text-[11px]">
                          <strong className="text-amber-700">Кулинарная оценка (Fallback)</strong> — средняя кулинарная оценка при опечатках в названии или отсутствии продукта в базе, защищающая приложение от падения.
                        </p>
                      </div>
                    </div>
                  );
                })()}

                {nutritionTask.status === 'FAILED' && (
                  <div className="p-3.5 bg-red-50 text-red-700 rounded-2xl text-xs">
                    Ошибка расчета: {nutritionTask.errorMessage || 'Сервер временно недоступен'}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-slate-50/80 border-t border-slate-200 flex items-center justify-between">
          <button
            onClick={() => {
              onClose();
              onEdit(recipe);
            }}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-semibold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-100 transition-colors"
          >
            <Edit2 className="w-4 h-4" />
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
