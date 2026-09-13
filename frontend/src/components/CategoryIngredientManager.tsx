import React, { useState } from 'react';
import { Category, Ingredient, User } from '../types';
import { api } from '../api/client';
import { Tag, Carrot, Users, Plus, Trash2 } from 'lucide-react';

interface CategoryIngredientManagerProps {
  categories: Category[];
  ingredients: Ingredient[];
  users: User[];
  onRefresh: () => void;
}

export const CategoryIngredientManager: React.FC<CategoryIngredientManagerProps> = ({
  categories,
  ingredients,
  users,
  onRefresh,
}) => {
  const [newCatName, setNewCatName] = useState('');
  const [newCatDesc, setNewCatDesc] = useState('');
  const [newIngName, setNewIngName] = useState('');
  const [newUserName, setNewUserName] = useState('');
  const [newUserEmail, setNewUserEmail] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCatName.trim()) return;
    try {
      setIsLoading(true);
      setError(null);
      await api.createCategory(newCatName.trim(), newCatDesc.trim() || undefined);
      setNewCatName('');
      setNewCatDesc('');
      onRefresh();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Ошибка добавления категории');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteCategory = async (id: number) => {
    if (!confirm('Удалить категорию?')) return;
    try {
      setIsLoading(true);
      await api.deleteCategory(id);
      onRefresh();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Ошибка удаления категории');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddIngredient = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newIngName.trim()) return;
    try {
      setIsLoading(true);
      setError(null);
      await api.createIngredient(newIngName.trim());
      setNewIngName('');
      onRefresh();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Ошибка добавления ингредиента');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteIngredient = async (id: number) => {
    if (!confirm('Удалить ингредиент?')) return;
    try {
      setIsLoading(true);
      await api.deleteIngredient(id);
      onRefresh();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Ошибка удаления ингредиента');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newUserName.trim() || !newUserEmail.trim()) return;
    try {
      setIsLoading(true);
      setError(null);
      await api.createUser(newUserName.trim(), newUserEmail.trim());
      setNewUserName('');
      setNewUserEmail('');
      onRefresh();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Ошибка добавления пользователя');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs">
        <h2 className="text-xl font-bold text-slate-900 mb-1">Справочники системы</h2>
        <p className="text-xs text-slate-500">
          Управление базовыми сущностями (Категории, Ингредиенты, Пользователи/Авторы), используемыми в отношениях OneToMany и ManyToMany.
        </p>
      </div>

      {error && (
        <div className="p-4 bg-red-50 text-red-700 border border-red-200 rounded-2xl text-xs font-medium">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Categories Manager */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <Tag className="w-5 h-5 text-orange-500" />
              <h3 className="font-bold text-slate-900 text-sm">Категории ({categories.length})</h3>
            </div>

            <form onSubmit={handleAddCategory} className="space-y-2 mb-4">
              <input
                type="text"
                value={newCatName}
                onChange={(e) => setNewCatName(e.target.value)}
                placeholder="Новая категория..."
                className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              />
              <input
                type="text"
                value={newCatDesc}
                onChange={(e) => setNewCatDesc(e.target.value)}
                placeholder="Описание (опционально)..."
                className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-orange-500"
              />
              <button
                type="submit"
                disabled={isLoading}
                className="w-full inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-orange-600 hover:bg-orange-700 text-white text-xs font-semibold transition-colors disabled:opacity-50"
              >
                <Plus className="w-3.5 h-3.5" />
                Добавить категорию
              </button>
            </form>

            <div className="space-y-1.5 max-h-72 overflow-y-auto pr-1">
              {categories.map((c) => (
                <div
                  key={c.id}
                  className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-100 text-xs"
                >
                  <span className="font-medium text-slate-800">{c.name}</span>
                  <button
                    onClick={() => handleDeleteCategory(c.id)}
                    className="p-1 text-slate-400 hover:text-red-600 rounded-md"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Ingredients Manager */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <Carrot className="w-5 h-5 text-emerald-500" />
              <h3 className="font-bold text-slate-900 text-sm">Ингредиенты ({ingredients.length})</h3>
            </div>

            <form onSubmit={handleAddIngredient} className="space-y-2 mb-4">
              <input
                type="text"
                value={newIngName}
                onChange={(e) => setNewIngName(e.target.value)}
                placeholder="Новый ингредиент (например: Базилик)..."
                className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                required
              />
              <button
                type="submit"
                disabled={isLoading}
                className="w-full inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold transition-colors disabled:opacity-50"
              >
                <Plus className="w-3.5 h-3.5" />
                Добавить ингредиент
              </button>
            </form>

            <div className="space-y-1.5 max-h-72 overflow-y-auto pr-1">
              {ingredients.map((i) => (
                <div
                  key={i.id}
                  className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-100 text-xs"
                >
                  <span className="font-medium text-slate-800">{i.name}</span>
                  <button
                    onClick={() => handleDeleteIngredient(i.id)}
                    className="p-1 text-slate-400 hover:text-red-600 rounded-md"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Users / Authors Manager */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <Users className="w-5 h-5 text-blue-500" />
              <h3 className="font-bold text-slate-900 text-sm">Авторы ({users.length})</h3>
            </div>

            <form onSubmit={handleAddUser} className="space-y-2 mb-4">
              <input
                type="text"
                value={newUserName}
                onChange={(e) => setNewUserName(e.target.value)}
                placeholder="Имя пользователя (username)..."
                className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
              <input
                type="email"
                value={newUserEmail}
                onChange={(e) => setNewUserEmail(e.target.value)}
                placeholder="Email..."
                className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
              <button
                type="submit"
                disabled={isLoading}
                className="w-full inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold transition-colors disabled:opacity-50"
              >
                <Plus className="w-3.5 h-3.5" />
                Добавить автора
              </button>
            </form>

            <div className="space-y-1.5 max-h-72 overflow-y-auto pr-1">
              {users.map((u) => (
                <div
                  key={u.id}
                  className="p-2.5 rounded-xl bg-slate-50 border border-slate-100 text-xs"
                >
                  <div className="font-medium text-slate-800">{u.username}</div>
                  <div className="text-[11px] text-slate-500">{u.email}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
