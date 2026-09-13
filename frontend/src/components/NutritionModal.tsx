import React from 'react';
import { Recipe, NutritionReportTask } from '../types';
import { X, Sparkles, Loader2, RefreshCw, AlertCircle } from 'lucide-react';

interface NutritionModalProps {
  recipe: Recipe | null;
  isOpen: boolean;
  onClose: () => void;
  onRecalculate: (recipe: Recipe) => void;
  nutritionTask: NutritionReportTask | null;
  isPolling: boolean;
}

export const NutritionModal: React.FC<NutritionModalProps> = ({
  recipe,
  isOpen,
  onClose,
  onRecalculate,
  nutritionTask,
  isPolling,
}) => {
  if (!isOpen || !recipe) return null;

  const report = nutritionTask?.result || nutritionTask?.report;
  const isCompleted = nutritionTask?.status === 'COMPLETED' && !!report;
  const isFailed = nutritionTask?.status === 'FAILED';
  const inProgress = isPolling || nutritionTask?.status === 'IN_PROGRESS' || nutritionTask?.status === 'SUBMITTED';

  const totalFats = report?.totalFatsGrams ?? report?.totalFatGrams ?? 0;
  const ingredientsList = report?.ingredients || report?.ingredientsData || [];

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 sm:p-6 animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl shadow-2xl max-w-2xl w-full max-h-[90vh] flex flex-col overflow-hidden border border-slate-200">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-200 flex items-center justify-between bg-gradient-to-r from-purple-50 to-orange-50/40">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-purple-600 text-white rounded-2xl shadow-sm">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-900">Пищевая ценность (КБЖУ)</h2>
              <p className="text-xs font-semibold text-purple-700">{recipe.title}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <div className="p-6 overflow-y-auto space-y-6">
          {/* Loading State */}
          {inProgress && (
            <div className="py-12 px-4 text-center space-y-4">
              <div className="w-16 h-16 bg-purple-50 text-purple-600 rounded-3xl flex items-center justify-center mx-auto shadow-inner">
                <Loader2 className="w-8 h-8 animate-spin" />
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900">Идет расчет нутриентов...</h3>
                <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
                  Асинхронный фоновый поток выполняет запросы к базе данных Open Food Facts API для сопоставления калорийности каждого ингредиента.
                </p>
              </div>
            </div>
          )}

          {/* Failed State */}
          {isFailed && !inProgress && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-2xl text-red-700 text-xs flex items-start gap-3">
              <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-500 mt-0.5" />
              <div className="flex-1">
                <strong className="block font-bold mb-0.5">Ошибка при расчете</strong>
                <span>{nutritionTask?.errorMessage || 'Не удалось связаться с внешним сервисом'}</span>
              </div>
              <button
                onClick={() => onRecalculate(recipe)}
                className="px-3 py-1.5 bg-red-100 hover:bg-red-200 text-red-800 font-bold rounded-xl transition-colors"
              >
                Повторить
              </button>
            </div>
          )}

          {/* Completed State */}
          {isCompleted && !inProgress && report && (
            <div className="space-y-6">
              {/* 4 Summary Metric Cards */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div className="bg-gradient-to-br from-orange-50 to-amber-50 p-4 rounded-2xl border border-orange-200 text-center shadow-xs">
                  <span className="block text-xs font-semibold text-orange-700 uppercase tracking-wider mb-1">
                    Калории
                  </span>
                  <span className="text-2xl font-black text-slate-900">{report.totalCaloriesKcal}</span>
                  <span className="block text-[11px] text-slate-500 mt-0.5">ккал</span>
                </div>

                <div className="bg-gradient-to-br from-blue-50 to-sky-50 p-4 rounded-2xl border border-blue-200 text-center shadow-xs">
                  <span className="block text-xs font-semibold text-blue-700 uppercase tracking-wider mb-1">
                    Белки
                  </span>
                  <span className="text-2xl font-black text-slate-900">{report.totalProteinsGrams}</span>
                  <span className="block text-[11px] text-slate-500 mt-0.5">грамм</span>
                </div>

                <div className="bg-gradient-to-br from-purple-50 to-fuchsia-50 p-4 rounded-2xl border border-purple-200 text-center shadow-xs">
                  <span className="block text-xs font-semibold text-purple-700 uppercase tracking-wider mb-1">
                    Жиры
                  </span>
                  <span className="text-2xl font-black text-slate-900">{totalFats}</span>
                  <span className="block text-[11px] text-slate-500 mt-0.5">грамм</span>
                </div>

                <div className="bg-gradient-to-br from-emerald-50 to-teal-50 p-4 rounded-2xl border border-emerald-200 text-center shadow-xs">
                  <span className="block text-xs font-semibold text-emerald-700 uppercase tracking-wider mb-1">
                    Углеводы
                  </span>
                  <span className="text-2xl font-black text-slate-900">{report.totalCarbohydratesGrams}</span>
                  <span className="block text-[11px] text-slate-500 mt-0.5">грамм</span>
                </div>
              </div>

              {/* Detailed Breakdown per Ingredient */}
              <div className="bg-slate-50/70 rounded-2xl border border-slate-200 p-4">
                <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-3">
                  Детализация по ингредиентам блюда
                </h4>

                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead>
                      <tr className="border-b border-slate-200 text-slate-400 font-medium">
                        <th className="pb-2.5">Ингредиент</th>
                        <th className="pb-2.5 text-right">Ккал</th>
                        <th className="pb-2.5 text-right">Белки</th>
                        <th className="pb-2.5 text-right">Жиры</th>
                        <th className="pb-2.5 text-right">Углеводы</th>
                        <th className="pb-2.5 text-right">Источник</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200/60 text-slate-700">
                      {ingredientsList.map((item, idx) => {
                        const isOpenFoodFacts =
                          item.dataSource?.includes('Open Food Facts') || item.foundInOpenFoodFacts;
                        const itemFats = item.fatsGrams ?? item.fatGrams ?? 0;

                        return (
                          <tr key={idx} className="hover:bg-white/60 transition-colors">
                            <td className="py-2.5 font-semibold text-slate-900">
                              {item.ingredientName}
                            </td>
                            <td className="py-2.5 text-right font-mono font-medium">
                              {item.caloriesKcal}
                            </td>
                            <td className="py-2.5 text-right font-mono">{item.proteinsGrams} г</td>
                            <td className="py-2.5 text-right font-mono">{itemFats} г</td>
                            <td className="py-2.5 text-right font-mono">{item.carbohydratesGrams} г</td>
                            <td className="py-2.5 text-right">
                              <span
                                className={`inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                                  isOpenFoodFacts
                                    ? 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                                    : 'bg-amber-100 text-amber-800 border border-amber-200'
                                }`}
                              >
                                <span
                                  className={`w-1.5 h-1.5 rounded-full ${
                                    isOpenFoodFacts ? 'bg-emerald-600' : 'bg-amber-500'
                                  }`}
                                />
                                {isOpenFoodFacts ? 'Open Food Facts' : 'Кулинарная оценка'}
                              </span>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>

                {/* Explanation Note for Data Sources */}
                <div className="mt-4 p-3.5 bg-white rounded-xl border border-slate-200/80 text-xs text-slate-600 leading-relaxed space-y-1.5">
                  <div className="flex items-center gap-1.5 font-bold text-slate-800">
                    <span className="w-1.5 h-1.5 rounded-full bg-purple-600" />
                    <span>Что означают источники данных?</span>
                  </div>
                  <p className="text-[11px]">
                    <strong className="text-emerald-700">Open Food Facts</strong> — точные данные найдены и загружены из открытой мировой базы продуктов.
                  </p>
                  <p className="text-[11px]">
                    <strong className="text-amber-700">Кулинарная оценка (Fallback)</strong> — стандартный базовый расчет пищевой ценности. Используется автоматически, если ингредиент написан с опечаткой или отсутствует в мировой базе. Благодаря этому расчет не падает с ошибкой, а сервер работает отказоустойчиво.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-slate-50 border-t border-slate-200 flex items-center justify-between">
          <button
            onClick={() => onRecalculate(recipe)}
            disabled={inProgress}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-purple-700 bg-purple-50 hover:bg-purple-100 rounded-xl transition-colors border border-purple-200 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${inProgress ? 'animate-spin' : ''}`} />
            Пересчитать заново
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
