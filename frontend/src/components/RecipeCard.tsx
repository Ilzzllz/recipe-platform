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
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col justify-between overflow-hidden group">
      <div className="p-6">
        {/* Badges bar */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full bg-orange-50 text-orange-700 border border-orange-200">
            <Tag className="w-3 h-3" />
            {recipe.category?.name || 'Без категории'}
          </span>
          <span className="text-xs text-slate-400 font-mono">ID: #{recipe.id}</span>
        </div>

        {/* Title & Description */}
        <h3 className="text-lg font-bold text-slate-900 group-hover:text-orange-600 transition-colors line-clamp-1 mb-2">
          {recipe.title}
        </h3>
        <p className="text-sm text-slate-600 line-clamp-2 mb-4 leading-relaxed">
          {recipe.description}
        </p>

        {/* Author info */}
        <div className="flex items-center gap-2 mb-4 pb-4 border-b border-slate-100">
          <div className="w-7 h-7 rounded-full bg-slate-100 text-slate-600 flex items-center justify-center text-xs font-bold">
            <ChefHat className="w-4 h-4" />
          </div>
          <span className="text-xs text-slate-600">
            Автор: <strong className="text-slate-800">{recipe.author?.username || 'Аноним'}</strong>
          </span>
        </div>

        {/* Relations Info Grid */}
        <div className="space-y-2.5 mb-4">
          {/* ManyToMany: Ingredients */}
          <div>
            <div className="flex items-center justify-between text-xs text-slate-500 mb-1.5">
              <span className="flex items-center gap-1 font-medium text-emerald-700">
                <Carrot className="w-3.5 h-3.5" />
                Ингредиенты (ManyToMany):
              </span>
              <span className="text-slate-400">{recipe.ingredients?.length || 0} шт.</span>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {recipe.ingredients && recipe.ingredients.length > 0 ? (
                recipe.ingredients.slice(0, 4).map((ing) => (
                  <span
                    key={ing.id}
                    className="text-xs px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-800 border border-emerald-200"
                  >
                    {ing.name}
                  </span>
                ))
              ) : (
                <span className="text-xs text-slate-400 italic">Ингредиенты не указаны</span>
              )}
              {recipe.ingredients && recipe.ingredients.length > 4 && (
                <span className="text-xs px-1.5 py-0.5 rounded-md bg-slate-100 text-slate-600">
                  +{recipe.ingredients.length - 4}
                </span>
              )}
            </div>
          </div>

          {/* OneToMany: Steps */}
          <div>
            <div className="flex items-center justify-between text-xs text-slate-500">
              <span className="flex items-center gap-1 font-medium text-blue-700">
                <ListOrdered className="w-3.5 h-3.5" />
                Шаги приготовления (OneToMany):
              </span>
              <span className="font-semibold text-blue-800 bg-blue-50 px-2 py-0.5 rounded-md border border-blue-200">
                {recipe.steps?.length || 0} шагов
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Card Actions */}
      <div className="px-6 py-3.5 bg-slate-50 border-t border-slate-100 flex items-center justify-between gap-2">
        <button
          onClick={() => onView(recipe)}
          className="flex-1 inline-flex items-center justify-center gap-1 px-3 py-1.5 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 hover:text-orange-600 transition-colors shadow-xs"
        >
          <Eye className="w-3.5 h-3.5" />
          Детали
        </button>

        <button
          onClick={() => onCalculateNutrition(recipe)}
          title="Запустить асинхронный расчет КБЖУ через Open Food Facts API (Лабораторная 6)"
          className="inline-flex items-center justify-center gap-1 px-2.5 py-1.5 text-xs font-medium text-purple-700 bg-purple-50 border border-purple-200 rounded-lg hover:bg-purple-100 transition-colors"
        >
          <Sparkles className="w-3.5 h-3.5" />
          КБЖУ
        </button>

        <button
          onClick={() => onEdit(recipe)}
          title="Редактировать рецепт"
          className="p-1.5 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors border border-transparent hover:border-blue-200"
        >
          <Edit2 className="w-4 h-4" />
        </button>

        <button
          onClick={() => onDelete(recipe.id, recipe.title)}
          title="Удалить рецепт"
          className="p-1.5 text-slate-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors border border-transparent hover:border-red-200"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
