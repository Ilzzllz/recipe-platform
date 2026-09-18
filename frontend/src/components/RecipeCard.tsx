import React from 'react';
import { Recipe } from '../types';
import { ChefHat, ListOrdered, Sparkles, Edit2, Trash2, Eye, Carrot, Tag } from 'lucide-react';

interface RecipeCardProps {
  recipe: Recipe;
  onView: (recipe: Recipe) => void;
  onEdit: (recipe: Recipe) => void;
  onDelete: (id: number, title: string) => void;
  onCalculateNutrition: (recipe: Recipe) => void;
}

export const RecipeCard: React.FC<RecipeCardProps> = ({
  recipe,
  onView,
  onEdit,
  onDelete,
  onCalculateNutrition,
}) => {
  return (
    <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs hover:shadow-lg transition-all duration-200 flex flex-col justify-between overflow-hidden group hover:border-orange-200">
      <div className="p-6">
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full bg-orange-50 text-orange-700 border border-orange-200">
            <Tag className="w-3 h-3" />
            {recipe.category?.name || 'Без категории'}
          </span>
          <span className="text-xs text-slate-400 font-mono">#{recipe.id}</span>
        </div>

        <h3
          onClick={() => onView(recipe)}
          className="text-lg font-bold text-slate-900 group-hover:text-orange-600 transition-colors line-clamp-1 mb-2 cursor-pointer"
        >
          {recipe.title}
        </h3>
        <p className="text-sm text-slate-600 line-clamp-2 mb-4 leading-relaxed">
          {recipe.description}
        </p>

        <div className="flex items-center gap-2 mb-4 pb-4 border-b border-slate-100">
          <div className="w-7 h-7 rounded-full bg-orange-100/70 text-orange-700 flex items-center justify-center text-xs font-bold">
            <ChefHat className="w-4 h-4" />
          </div>
          <span className="text-xs text-slate-600">
            Автор: <strong className="text-slate-800">{recipe.author?.username || 'Аноним'}</strong>
          </span>
        </div>

        <div className="space-y-3 mb-2">
          <div>
            <div className="flex items-center justify-between text-xs text-slate-500 mb-1.5">
              <span className="flex items-center gap-1 font-medium text-emerald-700">
                <Carrot className="w-3.5 h-3.5" />
                Ингредиенты
              </span>
              <span className="text-slate-400">{recipe.ingredients?.length || 0} шт.</span>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {recipe.ingredients && recipe.ingredients.length > 0 ? (
                recipe.ingredients.slice(0, 3).map((ing) => (
                  <span
                    key={ing.id}
                    className="text-xs px-2 py-0.5 rounded-lg bg-emerald-50 text-emerald-800 border border-emerald-200"
                  >
                    {ing.name}
                  </span>
                ))
              ) : (
                <span className="text-xs text-slate-400 italic">Ингредиенты не указаны</span>
              )}
              {recipe.ingredients && recipe.ingredients.length > 3 && (
                <span className="text-xs px-1.5 py-0.5 rounded-lg bg-slate-100 text-slate-600 font-medium">
                  +{recipe.ingredients.length - 3}
                </span>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between text-xs pt-1">
            <span className="flex items-center gap-1 font-medium text-blue-700">
              <ListOrdered className="w-3.5 h-3.5" />
              Шаги приготовления
            </span>
            <span className="font-semibold text-blue-800 bg-blue-50 px-2.5 py-0.5 rounded-lg border border-blue-200">
              {recipe.steps?.length || 0} {recipe.steps?.length === 1 ? 'шаг' : 'шагов'}
            </span>
          </div>
        </div>
      </div>

      <div className="px-6 py-3.5 bg-slate-50/70 border-t border-slate-100 flex items-center justify-between gap-2">
        <button
          onClick={() => onView(recipe)}
          className="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-50 hover:text-orange-600 transition-colors shadow-xs"
        >
          <Eye className="w-3.5 h-3.5" />
          Рецепт
        </button>

        <button
          onClick={() => onCalculateNutrition(recipe)}
          title="Рассчитать пищевую ценность (КБЖУ)"
          className="inline-flex items-center justify-center gap-1 px-2.5 py-1.5 text-xs font-semibold text-purple-700 bg-purple-50 border border-purple-200 rounded-xl hover:bg-purple-100 transition-colors"
        >
          <Sparkles className="w-3.5 h-3.5" />
          КБЖУ
        </button>

        <button
          onClick={() => onEdit(recipe)}
          title="Редактировать рецепт"
          className="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-colors border border-transparent hover:border-blue-200"
        >
          <Edit2 className="w-3.5 h-3.5" />
        </button>

        <button
          onClick={() => onDelete(recipe.id, recipe.title)}
          title="Удалить рецепт"
          className="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-xl transition-colors border border-transparent hover:border-red-200"
        >
          <Trash2 className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
};
