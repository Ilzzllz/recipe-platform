import React from 'react';
import { UtensilsCrossed, Search, Zap, Layers, RefreshCw } from 'lucide-react';

interface NavbarProps {
  activeTab: 'recipes' | 'filter' | 'concurrency' | 'data';
  setActiveTab: (tab: 'recipes' | 'filter' | 'concurrency' | 'data') => void;
  onRefresh: () => void;
  isLoading: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  onRefresh,
  isLoading,
}) => {
  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Title */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setActiveTab('recipes')}>
            <div className="p-2 bg-gradient-to-tr from-amber-500 to-orange-500 text-white rounded-xl shadow-md">
              <UtensilsCrossed className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xl font-bold bg-gradient-to-r from-orange-600 to-amber-600 bg-clip-text text-transparent">
                RecipePlatform
              </span>
              <span className="hidden sm:inline-block ml-2 text-xs font-semibold px-2 py-0.5 bg-orange-100 text-orange-800 rounded-full">
                Лабораторная 7 (SPA)
              </span>
            </div>
          </div>

          {/* Nav Tabs */}
          <nav className="flex space-x-1 sm:space-x-2">
            <button
              onClick={() => setActiveTab('recipes')}
              className={`flex items-center space-x-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === 'recipes'
                  ? 'bg-orange-50 text-orange-600 border border-orange-200'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <UtensilsCrossed className="w-4 h-4" />
              <span className="hidden md:inline">Рецепты</span>
            </button>

            <button
              onClick={() => setActiveTab('filter')}
              className={`flex items-center space-x-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === 'filter'
                  ? 'bg-orange-50 text-orange-600 border border-orange-200'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <Search className="w-4 h-4" />
              <span className="hidden md:inline">Фильтрация (JPQL/SQL)</span>
            </button>

            <button
              onClick={() => setActiveTab('concurrency')}
              className={`flex items-center space-x-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === 'concurrency'
                  ? 'bg-orange-50 text-orange-600 border border-orange-200'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <Zap className="w-4 h-4" />
              <span className="hidden md:inline">Многопоточность</span>
            </button>

            <button
              onClick={() => setActiveTab('data')}
              className={`flex items-center space-x-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                activeTab === 'data'
                  ? 'bg-orange-50 text-orange-600 border border-orange-200'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <Layers className="w-4 h-4" />
              <span className="hidden md:inline">Справочники</span>
            </button>
          </nav>

          {/* Refresh Action */}
          <div className="flex items-center space-x-2">
            <button
              onClick={onRefresh}
              disabled={isLoading}
              title="Обновить данные"
              className="p-2 text-slate-500 hover:text-orange-600 hover:bg-orange-50 rounded-lg transition-colors disabled:opacity-50"
            >
              <RefreshCw className={`w-5 h-5 ${isLoading ? 'animate-spin text-orange-500' : ''}`} />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
