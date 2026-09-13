import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { CounterStats, RaceConditionResult } from '../types';
import { Zap, ShieldCheck, AlertTriangle, Play, RotateCcw, Loader2, Info } from 'lucide-react';

export const ConcurrencySection: React.FC = () => {
  const [stats, setStats] = useState<CounterStats | null>(null);
  const [demoResult, setDemoResult] = useState<RaceConditionResult | null>(null);
  const [isLoadingStats, setIsLoadingStats] = useState(false);
  const [isRunningDemo, setIsRunningDemo] = useState(false);
  const [threads, setThreads] = useState(50);
  const [increments, setIncrements] = useState(100);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = async () => {
    try {
      setIsLoadingStats(true);
      const data = await api.getViewStats();
      setStats(data);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Не удалось загрузить статистику счетчиков';
      setError(msg);
    } finally {
      setIsLoadingStats(false);
    }
  };

  const handleReset = async () => {
    try {
      await api.resetViewCounters();
      await fetchStats();
      setDemoResult(null);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Не удалось сбросить счетчики';
      setError(msg);
    }
  };

  const handleRunDemo = async () => {
    try {
      setIsRunningDemo(true);
      setError(null);
      const result = await api.demoRaceCondition(threads, increments);
      setDemoResult(result);
      await fetchStats();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Ошибка запуска демонстрации';
      setError(msg);
    } finally {
      setIsRunningDemo(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  return (
    <div className="space-y-6">
      {/* Overview Banner */}
      <div className="bg-gradient-to-br from-amber-500 to-orange-600 rounded-3xl p-6 sm:p-8 text-white shadow-lg">
        <div className="flex items-center gap-3 mb-2">
          <div className="p-2 bg-white/20 backdrop-blur-xs rounded-xl">
            <Zap className="w-6 h-6" />
          </div>
          <h2 className="text-2xl font-black tracking-tight">
            Многопоточность и Race Condition (Лабораторная 6)
          </h2>
        </div>
        <p className="text-orange-100 text-sm max-w-2xl leading-relaxed">
          Наглядное доказательство состояния гонки (Race Condition) и потерянных обновлений (Lost Updates).
          Сравнение потокобезопасного <code className="bg-white/20 px-1.5 py-0.5 rounded text-white font-mono">AtomicLong</code> и небезопасного примитива <code className="bg-white/20 px-1.5 py-0.5 rounded text-white font-mono">int count++</code>.
        </p>
      </div>

      {error && (
        <div className="p-4 bg-red-50 text-red-700 border border-red-200 rounded-2xl text-xs font-medium">
          {error}
        </div>
      )}

      {/* Live View Counters Monitor */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <div>
            <h3 className="text-lg font-bold text-slate-900">
              Текущее состояние счётчиков просмотров рецептов
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Счётчики инкрементируются при каждом вызове <code className="bg-slate-100 px-1 rounded text-orange-700">GET /api/recipes/{'{id}'}</code>
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={fetchStats}
              disabled={isLoadingStats}
              className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
            >
              Обновить
            </button>
            <button
              onClick={handleReset}
              className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl border border-red-200 text-xs font-medium text-red-700 hover:bg-red-50"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              Сбросить в 0
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* AtomicLong */}
          <div className="p-5 rounded-2xl bg-emerald-50/50 border-2 border-emerald-200 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-emerald-800 uppercase tracking-wider">
                  AtomicLong
                </span>
                <ShieldCheck className="w-5 h-5 text-emerald-600" />
              </div>
              <p className="text-xs text-emerald-700 mb-4">
                Аппаратная инструкция CAS (Compare-And-Swap) без блокировок. Потокобезопасен.
              </p>
            </div>
            <div className="text-3xl font-black text-emerald-950 font-mono">
              {stats ? stats.atomicCount : '—'}
            </div>
          </div>

          {/* Synchronized */}
          <div className="p-5 rounded-2xl bg-blue-50/50 border-2 border-blue-200 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-blue-800 uppercase tracking-wider">
                  synchronized
                </span>
                <ShieldCheck className="w-5 h-5 text-blue-600" />
              </div>
              <p className="text-xs text-blue-700 mb-4">
                Монитор объекта (Mutex). Потокобезопасен за счёт очередей блокировок.
              </p>
            </div>
            <div className="text-3xl font-black text-blue-950 font-mono">
              {stats ? stats.synchronizedCount : '—'}
            </div>
          </div>

          {/* Unsafe int */}
          <div className="p-5 rounded-2xl bg-rose-50/50 border-2 border-rose-200 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-rose-800 uppercase tracking-wider">
                  Незащищённый int
                </span>
                <AlertTriangle className="w-5 h-5 text-rose-600" />
              </div>
              <p className="text-xs text-rose-700 mb-4">
                Операция non-atomic read-modify-write. Возникает потеря данных (Lost Updates).
              </p>
            </div>
            <div className="text-3xl font-black text-rose-950 font-mono">
              {stats ? stats.unsafeIntCount : '—'}
            </div>
          </div>
        </div>
      </div>

      {/* Stress Race Condition Demonstration */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200 shadow-xs space-y-6">
        <div>
          <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-rose-500" />
            Интерактивный запуск Race Condition (50+ потоков)
          </h3>
          <p className="text-xs text-slate-500 mt-1">
            Сервер создаст параллельные потоки с <code className="bg-slate-100 px-1 py-0.5 rounded text-orange-700 font-mono">CountDownLatch</code> и одновременно запустит инкремент одного и того же счетчика.
          </p>
        </div>

        {/* Inputs & Trigger */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-end bg-slate-50 p-4 rounded-2xl border border-slate-200/80">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Количество потоков
            </label>
            <input
              type="number"
              min={10}
              max={200}
              value={threads}
              onChange={(e) => setThreads(Number(e.target.value))}
              className="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-orange-500 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Инкрементов на поток
            </label>
            <input
              type="number"
              min={10}
              max={1000}
              value={increments}
              onChange={(e) => setIncrements(Number(e.target.value))}
              className="w-full px-3 py-2 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-orange-500 focus:outline-none"
            />
          </div>

          <button
            onClick={handleRunDemo}
            disabled={isRunningDemo}
            className="w-full inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl font-bold text-sm text-white bg-rose-600 hover:bg-rose-700 transition-colors shadow-xs disabled:opacity-50"
          >
            {isRunningDemo ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Тестирование...
              </>
            ) : (
              <>
                <Play className="w-4 h-4" />
                Запустить атаку гонки
              </>
            )}
          </button>
        </div>

        {/* Demo Output Presentation */}
        {demoResult && (
          <div className="rounded-2xl border-2 border-slate-200 p-6 space-y-4 bg-slate-50/40">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-600">
                Результаты эксперимента
              </span>
              <span
                className={`text-xs font-bold px-3 py-1 rounded-full ${
                  demoResult.raceConditionDetected
                    ? 'bg-rose-100 text-rose-800 border border-rose-200'
                    : 'bg-emerald-100 text-emerald-800'
                }`}
              >
                {demoResult.raceConditionDetected
                  ? '⚠️ Состояние гонки зафиксировано!'
                  : 'Потери не зафиксированы'}
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="bg-white p-4 rounded-xl border border-slate-200 text-center">
                <span className="text-xs text-slate-500 block">Ожидалось</span>
                <strong className="text-lg text-slate-900 font-mono">
                  {demoResult.expectedTotal}
                </strong>
              </div>
              <div className="bg-white p-4 rounded-xl border border-emerald-200 text-center">
                <span className="text-xs text-emerald-600 block">AtomicLong</span>
                <strong className="text-lg text-emerald-700 font-mono">
                  {demoResult.atomicResult}
                </strong>
              </div>
              <div className="bg-white p-4 rounded-xl border border-rose-200 text-center">
                <span className="text-xs text-rose-600 block">Небезопасный int</span>
                <strong className="text-lg text-rose-700 font-mono">
                  {demoResult.unsafeIntResult}
                </strong>
              </div>
              <div className="bg-white p-4 rounded-xl border border-rose-300 text-center bg-rose-50/30">
                <span className="text-xs text-rose-800 block font-bold">Потеряно (Lost Updates)</span>
                <strong className="text-lg text-rose-600 font-mono">
                  -{demoResult.lostUpdates}
                </strong>
              </div>
            </div>

            <div className="flex items-start gap-2 bg-white p-3.5 rounded-xl border border-slate-200 text-xs text-slate-600 leading-relaxed">
              <Info className="w-4 h-4 text-orange-500 flex-shrink-0 mt-0.5" />
              <span>{demoResult.description}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
