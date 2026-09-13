import React from 'react';
import { UtensilsCrossed, Layers, RefreshCw, Plus } from 'lucide-react';

interface NavbarProps {
  activeTab: 'recipes' | 'data';
  setActiveTab: (tab: 'recipes' | 'data') => void;
  onRefresh: () => void;
  onOpenCreateModal: () => void;
  isLoading: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  onRefresh,
  onOpenCreateModal,
  isLoading,
}) => {
  return (
    <header className="bg-white/95 backdrop-blur-md border-b border-slate-200 sticky top-0 z-30 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-4">
          {/* Logo & Title */}
          <div
            className="flex items-center space-x-3 cursor-pointer select-none"
            onClick={() => setActiveTab('recipes')}
          >
            <div className="p-2.5 bg-gradient-to-tr from-amber-500 to-orange-500 text-white rounded-2xl shadow-sm">
              <UtensilsCrossed className="w-5 h-5" />
            </div>
            <div>
              <span className="text-xl font-black tracking-tight bg-gradient-to-r from-orange-600 to-amber-600 bg-clip-text text-transparent">
                RecipePlatform
              </span>
              <p className="hidden sm:block text-[11px] text-slate-400 font-medium">
                Кулинарная книга рецептов & КБЖУ
              </p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="flex space-x-1.5 bg-slate-100 p-1 rounded-2xl">
            <button
              onClick={() => setActiveTab('recipes')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-all ${
                activeTab === 'recipes'
                  ? 'bg-white text-orange-600 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <UtensilsCrossed className="w-4 h-4" />
              <span>Рецепты</span>
            </button>

            <button
              onClick={() => setActiveTab('data')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-all ${
                activeTab === 'data'
                  ? 'bg-white text-orange-600 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Layers className="w-4 h-4" />
              <span>Справочники</span>
            </button>
          </nav>

          {/* Quick Actions */}
          <div className="flex items-center space-x-2">
            <button
              onClick={onRefresh}
              disabled={isLoading}
              title="Обновить данные с сервера"
              className="p-2.5 text-slate-500 hover:text-orange-600 hover:bg-orange-50 rounded-xl transition-colors disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin text-orange-500' : ''}`} />
            </button>

            <button
              onClick={onOpenCreateModal}
              className="hidden sm:inline-flex items-center gap-1.5 px-4 py-2 bg-orange-600 hover:bg-orange-700 text-white text-xs sm:text-sm font-bold rounded-xl transition-colors shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Новый рецепт</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
