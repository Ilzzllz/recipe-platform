import React, { useState } from 'react';
import { api } from '../api/client';
import { RecipeFilterPage } from '../types';
import { Search, Database, Code, ChevronLeft, ChevronRight, Loader2, Sparkles } from 'lucide-react';

export const FilterSection: React.FC = () => {
  const [queryType, setQueryType] = useState<'jpql' | 'native'>('jpql');
  const [authorUsername, setAuthorUsername] = useState('anna');
  const [categoryName, setCategoryName] = useState('Soups');
  const [page, setPage] = useState(0);
  const [size] = useState(5);
  const [result, setResult] = useState<RecipeFilterPage | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async (targetPage = 0) => {
    if (!authorUsername.trim() || !categoryName.trim()) {
      setError('Заполните имя автора и категорию для поиска');
      return;
    }
    setError(null);
    setIsLoading(true);
    try {
      let data: RecipeFilterPage;
      if (queryType === 'jpql') {
        data = await api.filterRecipesJPQL(authorUsername.trim(), categoryName.trim(), targetPage, size);
      } else {
        data = await api.filterRecipesNative(authorUsername.trim(), categoryName.trim(), targetPage, size);
      }
      setResult(data);
      setPage(targetPage);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Ошибка выполнения запроса';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header card */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
          <div>
            <h2 className="text-xl font-bold text-slate-900 flex items-center gap-2">
              <Search className="w-5 h-5 text-orange-500" />
              Сложная фильтрация рецептов (Лабораторная 3)
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Фильтрация по вложенным полям (Автор + Категория) с пагинацией <code className="bg-slate-100 px-1 py-0.5 rounded text-orange-700">Pageable</code>
            </p>
          </div>

          {/* Mode Switcher */}
          <div className="flex bg-slate-100 p-1 rounded-xl border border-slate-200/80 self-start">
            <button
              onClick={() => {
                setQueryType('jpql');
                setResult(null);
              }}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                queryType === 'jpql'
                  ? 'bg-white text-orange-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Code className="w-3.5 h-3.5" />
              JPQL (@Query)
            </button>
            <button
              onClick={() => {
                setQueryType('native');
                setResult(null);
              }}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                queryType === 'native'
                  ? 'bg-white text-orange-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Database className="w-3.5 h-3.5" />
              Native SQL
            </button>
          </div>
        </div>

        {/* Form Inputs */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch(0);
          }}
          className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-end"
        >
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Имя автора (Username)
            </label>
            <input
              type="text"
              value={authorUsername}
              onChange={(e) => setAuthorUsername(e.target.value)}
              placeholder="Например: anna"
              className="w-full px-3.5 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-sm"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Название категории
            </label>
            <input
              type="text"
              value={categoryName}
              onChange={(e) => setCategoryName(e.target.value)}
              placeholder="Например: Soups"
              className="w-full px-3.5 py-2 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-orange-500 text-sm"
              required
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl font-semibold text-sm text-white bg-orange-600 hover:bg-orange-700 transition-colors shadow-xs disabled:opacity-50"
          >
            {isLoading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Поиск...
              </>
            ) : (
              <>
                <Search className="w-4 h-4" />
                Найти рецепты
              </>
            )}
          </button>
        </form>

        {error && (
          <div className="mt-4 p-3 bg-red-50 text-red-700 border border-red-200 rounded-xl text-xs">
            {error}
          </div>
        )}
      </div>

      {/* Results view */}
      {result && (
        <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2 text-xs text-slate-600">
              <Sparkles className="w-4 h-4 text-orange-500" />
              <span>
                Найдено записей: <strong className="text-slate-900">{result.totalElements}</strong>
              </span>
              <span className="text-slate-300">•</span>
              <span>Страница {result.number + 1} из {result.totalPages || 1}</span>
            </div>
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-700 font-mono">
              Режим: {queryType.toUpperCase()}
            </span>
          </div>

          {result.content.length === 0 ? (
            <div className="p-8 text-center text-slate-400 text-sm">
              Ничего не найдено по заданным критериям поиска.
            </div>
          ) : (
            <div className="divide-y divide-slate-100">
              {result.content.map((item) => (
                <div key={item.recipeId} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-slate-400 font-mono">#{item.recipeId}</span>
                      <h4 className="text-sm font-bold text-slate-900">{item.recipeTitle}</h4>
                    </div>
                    <p className="text-xs text-slate-500 mt-0.5 line-clamp-1">{item.recipeDescription}</p>
                  </div>
                  <div className="flex items-center gap-2 text-xs">
                    <span className="px-2 py-0.5 rounded-md bg-orange-50 text-orange-700 border border-orange-200">
                      Категория: {item.categoryName}
                    </span>
                    <span className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-700">
                      Автор: {item.authorUsername}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination controls */}
          {result.totalPages > 1 && (
            <div className="pt-4 border-t border-slate-100 flex items-center justify-between">
              <button
                disabled={page === 0 || isLoading}
                onClick={() => handleSearch(page - 1)}
                className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-30"
              >
                <ChevronLeft className="w-4 h-4" />
                Назад
              </button>

              <span className="text-xs text-slate-500">
                {page + 1} / {result.totalPages}
              </span>

              <button
                disabled={page >= result.totalPages - 1 || isLoading}
                onClick={() => handleSearch(page + 1)}
                className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-30"
              >
                Вперед
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
