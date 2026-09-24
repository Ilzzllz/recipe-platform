import React, { useEffect, useState } from 'react';
import { Category, Ingredient, RecipeCreatePayload, User } from '../types';
import { ApiError } from '../api/client';
import { Plus, Trash2, X, AlertCircle } from 'lucide-react';

interface BulkRecipeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (payload: RecipeCreatePayload[]) => Promise<void>;
  categories: Category[];
  ingredients: Ingredient[];
  users: User[];
}

type BulkRow = RecipeCreatePayload & { key: string };
const emptyRow = (users: User[], categories: Category[]): BulkRow => ({
  key: `${Date.now()}-${Math.random()}`,
  title: '', description: '', authorId: users[0]?.id || 0, categoryId: categories[0]?.id || 0,
  ingredientIds: [], recipeIngredients: [], portions: 4,
  steps: [{ stepOrder: 1, description: '' }],
});

export const BulkRecipeModal: React.FC<BulkRecipeModalProps> = ({ isOpen, onClose, onSubmit, categories, ingredients, users }) => {
  const [rows, setRows] = useState<BulkRow[]>([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  useEffect(() => { if (isOpen) { setRows([emptyRow(users, categories)]); setError(''); } }, [isOpen, users, categories]);
  if (!isOpen) return null;

  const update = (key: string, patch: Partial<BulkRow>) => setRows((prev) => prev.map((row) => row.key === key ? { ...row, ...patch } : row));
  const toggleIngredient = (row: BulkRow, ingredientId: number) => {
    const selected = row.recipeIngredients || [];
    const exists = selected.some((item) => item.ingredientId === ingredientId);
    const next = exists ? selected.filter((item) => item.ingredientId !== ingredientId) : [...selected, { ingredientId, quantity: 100, unit: 'г' }];
    update(row.key, { ingredientIds: next.map((item) => item.ingredientId), recipeIngredients: next });
  };
  const updateIngredient = (row: BulkRow, ingredientId: number, patch: Partial<{ quantity: number; unit: string }>) => update(row.key, { recipeIngredients: (row.recipeIngredients || []).map((item) => item.ingredientId === ingredientId ? { ...item, ...patch } : item) });
  const updateStep = (row: BulkRow, index: number, description: string) => update(row.key, { steps: row.steps.map((step, stepIndex) => stepIndex === index ? { ...step, description } : step) });
  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    const invalid = rows.some((row) => !row.title.trim() || !row.description.trim() || !row.authorId || !row.categoryId || !(row.recipeIngredients || []).length || (row.recipeIngredients || []).some((item) => !item.quantity || item.quantity <= 0) || !row.steps.some((step) => step.description.trim()));
    if (invalid) { setError('Заполните название, описание, автора, категорию, ингредиенты с количеством и хотя бы один шаг в каждом рецепте.'); return; }
    try { setSaving(true); await onSubmit(rows.map(({ key, ...row }) => ({ ...row, title: row.title.trim(), description: row.description.trim(), steps: row.steps.filter((step) => step.description.trim()).map((step, index) => ({ ...step, stepOrder: index + 1, description: step.description.trim() })) }))); }
    catch (err: unknown) { setError(err instanceof ApiError ? err.message : 'Не удалось добавить рецепты'); }
    finally { setSaving(false); }
  };

  return <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
    <div className="bg-white rounded-3xl shadow-2xl max-w-5xl w-full max-h-[92vh] flex flex-col overflow-hidden border border-slate-200">
      <div className="px-6 py-5 border-b flex items-center justify-between"><div><h2 className="text-2xl font-black text-slate-900">Массовое добавление рецептов</h2><p className="text-base text-slate-500">Заполните несколько рецептов и сохраните их одной кнопкой.</p></div><button onClick={onClose} className="p-2 rounded-full hover:bg-slate-100"><X className="w-6 h-6" /></button></div>
      <form onSubmit={submit} className="p-6 overflow-y-auto space-y-5">
        {error && <div className="p-4 rounded-2xl bg-red-50 text-red-700 border border-red-200 text-base flex gap-2"><AlertCircle className="w-5 h-5 shrink-0" />{error}</div>}
        {rows.map((row, index) => <div key={row.key} className="rounded-2xl border border-slate-200 p-5 space-y-4 bg-slate-50/50">
          <div className="flex justify-between items-center"><h3 className="text-lg font-bold">Рецепт {index + 1}</h3>{rows.length > 1 && <button type="button" onClick={() => setRows((prev) => prev.filter((item) => item.key !== row.key))} className="text-red-600"><Trash2 className="w-5 h-5" /></button>}</div>
          <div className="grid md:grid-cols-2 gap-3"><input value={row.title} onChange={(e) => update(row.key, { title: e.target.value })} placeholder="Название блюда" className="px-4 py-3 rounded-xl border text-base" /><input value={row.description} onChange={(e) => update(row.key, { description: e.target.value })} placeholder="Краткое описание" className="px-4 py-3 rounded-xl border text-base" /><select value={row.authorId} onChange={(e) => update(row.key, { authorId: Number(e.target.value) })} className="px-4 py-3 rounded-xl border text-base"><option value={0}>Автор</option>{users.map((user) => <option key={user.id} value={user.id}>{user.username}</option>)}</select><select value={row.categoryId} onChange={(e) => update(row.key, { categoryId: Number(e.target.value) })} className="px-4 py-3 rounded-xl border text-base"><option value={0}>Категория</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></div>
          <div><p className="text-base font-semibold mb-2">Ингредиенты</p><div className="flex flex-wrap gap-2">{ingredients.map((ingredient) => { const item = (row.recipeIngredients || []).find((selected) => selected.ingredientId === ingredient.id); return <button type="button" key={ingredient.id} onClick={() => toggleIngredient(row, ingredient.id)} className={`px-3 py-2 rounded-xl border text-sm ${item ? 'bg-emerald-100 border-emerald-300 text-emerald-900' : 'bg-white border-slate-200'}`}>{ingredient.name}</button>; })}</div>{(row.recipeIngredients || []).map((item) => <div key={item.ingredientId} className="flex gap-2 mt-2"><span className="flex-1 px-3 py-2 bg-white rounded-xl border text-sm">{ingredients.find((ingredient) => ingredient.id === item.ingredientId)?.name}</span><input type="number" min="0.01" step="0.01" value={item.quantity} onChange={(e) => updateIngredient(row, item.ingredientId, { quantity: Number(e.target.value) })} className="w-24 px-3 py-2 rounded-xl border text-sm" /><select value={item.unit} onChange={(e) => updateIngredient(row, item.ingredientId, { unit: e.target.value })} className="w-20 px-2 py-2 rounded-xl border text-sm"><option>г</option><option>мл</option><option>шт</option></select></div>)}</div>
          <div><p className="text-base font-semibold mb-2">Шаги приготовления</p>{row.steps.map((step, stepIndex) => <textarea key={stepIndex} value={step.description} onChange={(e) => updateStep(row, stepIndex, e.target.value)} placeholder={`Шаг ${stepIndex + 1}`} rows={2} className="w-full mb-2 px-4 py-3 rounded-xl border text-base" />)}<button type="button" onClick={() => update(row.key, { steps: [...row.steps, { stepOrder: row.steps.length + 1, description: '' }] })} className="inline-flex items-center gap-1 text-sm text-indigo-700 font-semibold"><Plus className="w-4 h-4" />Добавить шаг</button></div>
        </div>)}
        <button type="button" onClick={() => setRows((prev) => [...prev, emptyRow(users, categories)])} className="inline-flex items-center gap-2 px-4 py-3 rounded-xl border border-indigo-300 text-indigo-700 font-semibold text-base"><Plus className="w-5 h-5" />Добавить ещё рецепт</button>
        <div className="flex justify-end gap-3 pt-2"><button type="button" onClick={onClose} className="px-5 py-3 rounded-xl bg-slate-100 text-base font-semibold">Отмена</button><button type="submit" disabled={saving} className="px-6 py-3 rounded-xl bg-indigo-600 text-white text-base font-bold disabled:opacity-50">{saving ? 'Сохраняем…' : 'Сохранить рецепты'}</button></div>
      </form>
    </div>
  </div>;
};
