import React, { useState, useEffect } from 'react';
import { Recipe, RecipeCreatePayload, Category, Ingredient, User } from '../types';
import { X, Plus, Trash2, ArrowUp, ArrowDown, Network, Database } from 'lucide-react';

interface RecipeFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (payload: RecipeCreatePayload, recipeId?: number) => Promise<void>;
  initialRecipe?: Recipe | null;
  categories: Category[];
  ingredients: Ingredient[];
  users: User[];
}

export const RecipeFormModal: React.FC<RecipeFormModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  initialRecipe,
  categories,
  ingredients,
  users,
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [authorId, setAuthorId] = useState<number | ''>('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [selectedIngredientIds, setSelectedIngredientIds] = useState<number[]>([]);
  const [steps, setSteps] = useState<{ stepOrder: number; description: string }[]>([
    { stepOrder: 1, description: '' },
  ]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isEditing = !!initialRecipe;

  useEffect(() => {
    if (initialRecipe) {
      setTitle(initialRecipe.title || '');
      setDescription(initialRecipe.description || '');
      setAuthorId(initialRecipe.author?.id || '');
      setCategoryId(initialRecipe.category?.id || '');
      setSelectedIngredientIds(initialRecipe.ingredients?.map((i) => i.id) || []);
      const sortedSteps = [...(initialRecipe.steps || [])]
        .sort((a, b) => a.stepOrder - b.stepOrder)
        .map((s, idx) => ({ stepOrder: idx + 1, description: s.description }));
      setSteps(sortedSteps.length > 0 ? sortedSteps : [{ stepOrder: 1, description: '' }]);
    } else {
      setTitle('');
      setDescription('');
      setAuthorId(users[0]?.id || '');
      setCategoryId(categories[0]?.id || '');
      setSelectedIngredientIds([]);
      setSteps([{ stepOrder: 1, description: '' }]);
    }
    setError(null);
  }, [initialRecipe, isOpen, users, categories]);

  if (!isOpen) return null;

  const handleAddStep = () => {
    setSteps((prev) => [...prev, { stepOrder: prev.length + 1, description: '' }]);
  };

  const handleRemoveStep = (index: number) => {
    if (steps.length === 1) return;
    const newSteps = steps
      .filter((_, idx) => idx !== index)
      .map((step, idx) => ({ ...step, stepOrder: idx + 1 }));
    setSteps(newSteps);
  };

  const handleStepChange = (index: number, val: string) => {
    setSteps((prev) =>
      prev.map((step, idx) => (idx === index ? { ...step, description: val } : step))
    );
  };

  const handleMoveStep = (index: number, direction: 'up' | 'down') => {
    const targetIdx = direction === 'up' ? index - 1 : index + 1;
    if (targetIdx < 0 || targetIdx >= steps.length) return;
    const updated = [...steps];
    const temp = updated[index];
    updated[index] = updated[targetIdx];
    updated[targetIdx] = temp;
    setSteps(updated.map((s, idx) => ({ ...s, stepOrder: idx + 1 })));
  };

  const toggleIngredient = (id: number) => {
    setSelectedIngredientIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!title.trim()) {
      setError('Укажите название рецепта');
      return;
    }
    if (!description.trim()) {
      setError('Укажите описание рецепта');
      return;
    }
    if (!authorId) {
      setError('Выберите автора рецепта');
      return;
    }
    if (!categoryId) {
      setError('Выберите категорию рецепта');
      return;
    }
    if (selectedIngredientIds.length === 0) {
      setError('Выберите хотя бы один ингредиент (связь ManyToMany)');
      return;
    }
    const cleanSteps = steps
      .map((s, idx) => ({ stepOrder: idx + 1, description: s.description.trim() }))
      .filter((s) => s.description.length > 0);

    if (cleanSteps.length === 0) {
      setError('Добавьте хотя бы один шаг приготовления с описанием (связь OneToMany)');
      return;
    }

    const payload: RecipeCreatePayload = {
      title: title.trim(),
      description: description.trim(),
      authorId: Number(authorId),
      categoryId: Number(categoryId),
      ingredientIds: selectedIngredientIds,
      steps: cleanSteps,
    };

    try {
      setIsSubmitting(true);
      await onSubmit(payload, initialRecipe?.id);
      onClose();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Произошла ошибка при сохранении';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 sm:p-6 animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl shadow-2xl max-w-3xl w-full max-h-[90vh] flex flex-col overflow-hidden border border-slate-200">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
          <div>
            <h2 className="text-xl font-bold text-slate-900">
              {isEditing ? `Редактирование рецепта #${initialRecipe.id}` : 'Создание нового рецепта'}
            </h2>
            <p className="text-xs text-slate-500">
              Заполните поля формы для отправки на REST API Spring Boot (POST / PUT)
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-6">
          {error && (
            <div className="p-3 bg-red-50 text-red-700 border border-red-200 rounded-xl text-xs font-medium">
              {error}
            </div>
          )}

          {/* Title & Description */}
          <div className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Название рецепта *
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Например: Тыквенный крем-суп с сухариками"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent text-sm"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Краткое описание *
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Нежный осенний суп-пюре со сливками и специями..."
                rows={3}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent text-sm"
                required
              />
            </div>
          </div>

          {/* Author & Category Pickers */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Автор (User ManyToOne) *
              </label>
              <select
                value={authorId}
                onChange={(e) => setAuthorId(Number(e.target.value))}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-sm bg-white"
                required
              >
                <option value="" disabled>
                  Выберите автора
                </option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.username} ({u.email || `id: ${u.id}`})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Категория (Category ManyToOne) *
              </label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(Number(e.target.value))}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-sm bg-white"
                required
              >
                <option value="" disabled>
                  Выберите категорию
                </option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* ManyToMany: Ingredients Selection */}
          <div className="rounded-2xl border-2 border-emerald-100 bg-emerald-50/20 p-4">
            <div className="flex items-center gap-2 mb-2">
              <Network className="w-4 h-4 text-emerald-600" />
              <label className="text-xs font-bold text-emerald-950 uppercase tracking-wider">
                Ингредиенты (ManyToMany связь) *
              </label>
            </div>
            <p className="text-xs text-slate-500 mb-3">
              Нажмите на ингредиент, чтобы привязать его к рецепту через промежуточную таблицу связей.
            </p>

            <div className="flex flex-wrap gap-2 max-h-48 overflow-y-auto p-1">
              {ingredients.map((ing) => {
                const isSelected = selectedIngredientIds.includes(ing.id);
                return (
                  <button
                    type="button"
                    key={ing.id}
                    onClick={() => toggleIngredient(ing.id)}
                    className={`text-xs px-3 py-1.5 rounded-xl font-medium transition-all ${
                      isSelected
                        ? 'bg-emerald-600 text-white shadow-xs scale-105'
                        : 'bg-white text-slate-700 border border-slate-200 hover:border-emerald-300'
                    }`}
                  >
                    {isSelected ? '✓ ' : '+ '}
                    {ing.name}
                  </button>
                );
              })}
            </div>
            <div className="mt-2 text-right text-xs font-semibold text-emerald-800">
              Выбрано: {selectedIngredientIds.length}
            </div>
          </div>

          {/* OneToMany: Steps List */}
          <div className="rounded-2xl border-2 border-blue-100 bg-blue-50/20 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Database className="w-4 h-4 text-blue-600" />
                <label className="text-xs font-bold text-blue-950 uppercase tracking-wider">
                  Шаги приготовления (OneToMany связь) *
                </label>
              </div>
              <button
                type="button"
                onClick={handleAddStep}
                className="inline-flex items-center gap-1 text-xs font-bold px-3 py-1 rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                Добавить шаг
              </button>
            </div>
            <p className="text-xs text-slate-500">
              Каждый шаг является дочерней сущностью CookingStep с собственным порядковым номером stepOrder.
            </p>

            <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
              {steps.map((step, idx) => (
                <div
                  key={idx}
                  className="flex items-center gap-2 bg-white p-2.5 rounded-xl border border-slate-200 shadow-xs"
                >
                  <span className="w-6 h-6 rounded-md bg-blue-100 text-blue-800 font-bold text-xs flex items-center justify-center flex-shrink-0">
                    {step.stepOrder}
                  </span>
                  <input
                    type="text"
                    value={step.description}
                    onChange={(e) => handleStepChange(idx, e.target.value)}
                    placeholder={`Описание действия на шаге ${step.stepOrder}...`}
                    className="flex-1 text-xs px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
                    required
                  />

                  {/* Move Up/Down */}
                  <div className="flex items-center">
                    <button
                      type="button"
                      disabled={idx === 0}
                      onClick={() => handleMoveStep(idx, 'up')}
                      className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-20"
                    >
                      <ArrowUp className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      disabled={idx === steps.length - 1}
                      onClick={() => handleMoveStep(idx, 'down')}
                      className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-20"
                    >
                      <ArrowDown className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  {/* Delete Step */}
                  <button
                    type="button"
                    disabled={steps.length === 1}
                    onClick={() => handleRemoveStep(idx)}
                    className="p-1 text-slate-400 hover:text-red-600 disabled:opacity-20"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* Footer Submit */}
          <div className="pt-4 border-t border-slate-200 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 text-sm font-semibold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-100 transition-colors"
            >
              Отмена
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-6 py-2.5 text-sm font-semibold text-white bg-orange-600 hover:bg-orange-700 rounded-xl transition-colors shadow-sm disabled:opacity-50"
            >
              {isSubmitting ? 'Сохранение...' : isEditing ? 'Обновить рецепт' : 'Создать рецепт'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
