import React, { useState } from 'react';
import { Category, Ingredient, User } from '../types';
import { api, ApiError } from '../api/client';
import { Tag, Carrot, Users, Plus, Trash2, Layers } from 'lucide-react';
import { ConfirmModal } from './ConfirmModal';

interface CategoryIngredientManagerProps {
  categories: Category[];
  ingredients: Ingredient[];
  users: User[];
  onRefresh: () => void;
  onToast: (type: 'success' | 'error' | 'info' | 'warning', message: string) => void;
}

export const CategoryIngredientManager: React.FC<CategoryIngredientManagerProps> = ({
  categories,
  ingredients,
  users,
  onRefresh,
  onToast,
}) => {
  const [newCatName, setNewCatName] = useState('');
  const [newCatDesc, setNewCatDesc] = useState('');
  const [newIngName, setNewIngName] = useState('');
  const [newUserName, setNewUserName] = useState('');
  const [newUserEmail, setNewUserEmail] = useState('');

  const [isLoading, setIsLoading] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<{
    type: 'category' | 'ingredient';
    id: number;
    name: string;
  } | null>(null);

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCatName.trim()) {
      onToast('error', 'Укажите название категории');
      return;
    }
    try {
      setIsLoading(true);
      await api.createCategory(newCatName.trim(), newCatDesc.trim() || undefined);
      setNewCatName('');
      setNewCatDesc('');
      onToast('success', 'Категория успешно добавлена');
      onRefresh();
    } catch (err: unknown) {
      const msg = err instanceof ApiError ? err.message : 'Не удалось добавить категорию';
      onToast('error', msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      setIsLoading(true);
      if (deleteTarget.type === 'category') {
        await api.deleteCategory(deleteTarget.id);
        onToast('success', `Категория "${deleteTarget.name}" удалена`);
      } else {
        await api.deleteIngredient(deleteTarget.id);
        onToast('success', `Ингредиент "${deleteTarget.name}" удален`);
      }
      onRefresh();
    } catch (err: unknown) {
      const msg = err instanceof ApiError ? err.message : 'Ошибка при удалении';
      onToast('error', msg);
    } finally {
      setIsLoading(false);
      setDeleteTarget(null);
    }
  };

  const handleAddIngredient = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newIngName.trim()) {
      onToast('error', 'Укажите название ингредиента');
      return;
    }
    try {
      setIsLoading(true);
      await api.createIngredient(newIngName.trim());
      setNewIngName('');
      onToast('success', 'Ингредиент успешно добавлен');
      onRefresh();
    } catch (err: unknown) {
      const msg = err instanceof ApiError ? err.message : 'Не удалось добавить ингредиент';
      onToast('error', msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newUserName.trim() || !newUserEmail.trim()) {
      onToast('error', 'Заполните имя автора и email');
      return;
    }
    try {
      setIsLoading(true);
      await api.createUser(newUserName.trim(), newUserEmail.trim());
      setNewUserName('');
      setNewUserEmail('');
      onToast('success', 'Автор успешно зарегистрирован');
      onRefresh();
    } catch (err: unknown) {
      const msg = err instanceof ApiError ? err.message : 'Не удалось добавить пользователя';
      onToast('error', msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-xs flex items-center justify-between">
        <div>
          <h2 className="text-xl font-black text-slate-900 mb-1 flex items-center gap-2">
            <Layers className="w-5 h-5 text-orange-500" />
            Справочники системы
          </h2>
          <p className="text-sm text-slate-500">
            Управление категориями блюд, библиотекой ингредиентов и авторами кулинарных рецептов
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[0.95fr_1.05fr_1.2fr] gap-6 items-stretch">
        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs flex flex-col min-h-[36rem]">
          <div>
            <div className="flex items-center justify-between gap-2 mb-4">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-orange-100 text-orange-600 rounded-xl">
                  <Tag className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-slate-900 text-base">Категории</h3>
              </div>
              <span className="text-sm font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700">
                {categories.length}
              </span>
            </div>

            <form onSubmit={handleAddCategory} className="space-y-2 mb-4">
              <input
                type="text"
                value={newCatName}
                onChange={(e) => setNewCatName(e.target.value)}
                placeholder="Название категории..."
                className="w-full text-sm px-3.5 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-orange-500 bg-slate-50/50"
              />
              <input
                type="text"
                value={newCatDesc}
                onChange={(e) => setNewCatDesc(e.target.value)}
                placeholder="Описание (необязательно)..."
                className="w-full text-sm px-3.5 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-orange-500 bg-slate-50/50"
              />
              <button
                type="submit"
                disabled={isLoading}
                className="w-full inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-orange-600 hover:bg-orange-700 text-white text-sm font-semibold transition-colors disabled:opacity-50 shadow-xs"
              >
                <Plus className="w-3.5 h-3.5" />
                Добавить категорию
              </button>
            </form>

            <div className="space-y-2.5 max-h-[30rem] overflow-y-auto pr-1">
              {categories.map((c) => (
                <div
                  key={c.id}
                  className="flex items-center justify-between gap-3 p-3.5 rounded-xl bg-slate-50/80 border border-slate-100 text-sm"
                >
                  <div>
                    <span className="font-semibold text-slate-800">{c.name}</span>
                    {c.description && (
                      <p className="text-xs text-slate-400 line-clamp-2 mt-0.5">{c.description}</p>
                    )}
                  </div>
                  <button
                    onClick={() =>
                      setDeleteTarget({ type: 'category', id: c.id, name: c.name })
                    }
                    className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Удалить категорию"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs flex flex-col min-h-[36rem]">
          <div>
            <div className="flex items-center justify-between gap-2 mb-4">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-emerald-100 text-emerald-600 rounded-xl">
                  <Carrot className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-slate-900 text-base">Ингредиенты</h3>
              </div>
              <span className="text-sm font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700">
                {ingredients.length}
              </span>
            </div>

            <form onSubmit={handleAddIngredient} className="space-y-2 mb-4">
              <input
                type="text"
                value={newIngName}
                onChange={(e) => setNewIngName(e.target.value)}
                placeholder="Новый ингредиент (напр: Оливковое масло)..."
                className="w-full text-sm px-3.5 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 bg-slate-50/50"
              />
              <button
                type="submit"
                disabled={isLoading}
                className="w-full inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-semibold transition-colors disabled:opacity-50 shadow-xs"
              >
                <Plus className="w-3.5 h-3.5" />
                Добавить ингредиент
              </button>
            </form>

            <div className="space-y-2.5 max-h-[30rem] overflow-y-auto pr-1">
              {ingredients.map((i) => (
                <div
                  key={i.id}
                  className="flex items-center justify-between gap-3 p-3.5 rounded-xl bg-slate-50/80 border border-slate-100 text-sm"
                >
                  <span className="font-semibold text-slate-800">{i.name}</span>
                  <button
                    onClick={() =>
                      setDeleteTarget({ type: 'ingredient', id: i.id, name: i.name })
                    }
                    className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Удалить ингредиент"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs flex flex-col min-h-[36rem]">
          <div>
            <div className="flex items-center justify-between gap-2 mb-4">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-blue-100 text-blue-600 rounded-xl">
                  <Users className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-slate-900 text-base">Авторы</h3>
              </div>
              <span className="text-sm font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700">
                {users.length}
              </span>
            </div>

            <form onSubmit={handleAddUser} className="space-y-2 mb-4">
              <input
                type="text"
                value={newUserName}
                onChange={(e) => setNewUserName(e.target.value)}
                placeholder="Имя автора..."
                className="w-full text-sm px-3.5 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50/50"
              />
              <input
                type="email"
                value={newUserEmail}
                onChange={(e) => setNewUserEmail(e.target.value)}
                placeholder="Email..."
                className="w-full text-sm px-3.5 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50/50"
              />
              <button
                type="submit"
                disabled={isLoading}
                className="w-full inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold transition-colors disabled:opacity-50 shadow-xs"
              >
                <Plus className="w-3.5 h-3.5" />
                Зарегистрировать автора
              </button>
            </form>

            <div className="space-y-2.5 max-h-[30rem] overflow-y-auto pr-1">
              {users.map((u) => (
                <div
                  key={u.id}
                  className="p-3.5 rounded-xl bg-slate-50/80 border border-slate-100 text-sm flex items-center justify-between gap-3"
                >
                  <div>
                    <div className="font-semibold text-slate-800">{u.username}</div>
                    <div className="text-xs text-slate-400 mt-0.5">{u.email}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <ConfirmModal
        isOpen={!!deleteTarget}
        title={`Удалить ${deleteTarget?.type === 'category' ? 'категорию' : 'ингредиент'}?`}
        message={`Вы уверены, что хотите удалить "${deleteTarget?.name}"? Если этот элемент связан с существующими рецептами, операция может быть отклонена базой данных.`}
        confirmText="Да, удалить"
        cancelText="Отмена"
        isDangerous={true}
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
};
