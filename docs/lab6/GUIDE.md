# Гайдлайн по реализации Лабораторной работы №6

Данный документ описывает реализацию всех требований Лабораторной работы №6 (**Concurrency — Многопоточность и асинхронность**) в проекте **Recipe Platform**.

Документ написан простым, понятным языком, со ссылками на конкретные классы, аннотации и методы в исходном коде.

---

## Краткое описание темы лабораторной работы

Лабораторная работа №6 посвящена параллелизму и конкурентной обработке данных в Java Spring Boot:
1. **Асинхронные задачи (`@Async` / `CompletableFuture`)**: вынос тяжелых операций из веб-потока Tomcat в фоновый пул потоков, чтобы не блокировать клиента и возвращать быстрый ответ с идентификатором задачи (Task ID).
2. **Потокобезопасность (Thread Safety)**: разница между небезопасными операциями над общими переменными и потокобезопасными механизмами (`AtomicLong` и `synchronized`).
3. **Состояние гонки (Race Condition)**: практическая демонстрация потери данных при одновременной записи из 50+ параллельных потоков.
4. **Нагрузочное тестирование (JMeter)**: создание параллельной нагрузки на сервер и фиксация расхождения значений счетчиков.

---

## Требование 1. Асинхронная бизнес-операция через `@Async` / `CompletableFuture`

### Суть требования
Нужно реализовать длительную бизнес-операцию, которая:
1. Сразу возвращает клиенту уникальный `taskId` (UUID) со статусом `IN_PROGRESS` (HTTP 202 Accepted).
2. Выполняется в отдельном фоновом потоке, не блокируя вызывающий поток веб-сервера.
3. Позволяет клиенту в любой момент опросить статус выполнения через `GET /api/recipes/nutrition-report/{taskId}` и забрать готовый результат, когда статус станет `COMPLETED`.

### Бизнес-обоснование (Реальная задача)
Вместо искусственного "сна" (`Thread.sleep`) сервер выполняет **реальный расчет пищевой ценности рецепта (КБЖУ: калории, белки, жиры, углеводы)**.
Для каждого ингредиента рецепта сервер отправляет сетевой HTTP-запрос к открытой международной базе продуктов **Open Food Facts API** (`https://world.openfoodfacts.org`).
Так как рецепт содержит множество ингредиентов, опрос внешнего веб-сервиса по сети занимает 1.5–3 секунды. Вынос этой операции в асинхронный поток (`@Async`) абсолютно необходим, чтобы клиентский запрос не "висел".

### Где и как реализовано в коде

#### 1. Настройка пула потоков:
- **Файл**: [`src/main/java/com/example/recipeplatform/config/AsyncConfig.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/config/AsyncConfig.java)
- Аннотация `@EnableAsync` активирует поддержку асинхронных методов в Spring.
- Создан бин `recipeTaskExecutor` типа `ThreadPoolTaskExecutor`:
  - `corePoolSize = 4` — 4 базовых рабочих потока;
  - `maxPoolSize = 10` — максимум 10 потоков при пиковой нагрузке;
  - `queueCapacity = 50` — очередь ожидания на 50 задач;
  - `threadNamePrefix = "recipe-async-"` — понятные имена потоков в логах.

#### 2. Сервис асинхронной обработки:
- **Файл**: [`src/main/java/com/example/recipeplatform/service/NutritionReportService.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/service/NutritionReportService.java)
- Метод `startNutritionReport(Long recipeId)`:
  1. Проверяет наличие рецепта.
  2. Генерирует `UUID taskId = UUID.randomUUID()`.
  3. Сохраняет объект `AsyncTaskResponseDto` в потокобезопасную карту `ConcurrentHashMap<UUID, AsyncTaskResponseDto>` со статусом `IN_PROGRESS`.
  4. Запускает фоновый метод `calculateNutritionAsync(recipeId, taskId)`.
  5. Мгновенно возвращает `taskId`.
- Метод `calculateNutritionAsync`:
  - Помечен аннотацией `@Async("recipeTaskExecutor")` и возвращает `CompletableFuture<NutritionReportDto>`.
  - В цикле опрашивает Open Food Facts API через современный Spring `RestClient`.
  - Суммирует показатели КБЖУ.
  - При завершении обновляет статус в хранилище на `COMPLETED` и прикрепляет итоговый `NutritionReportDto`.
- Метод `getTaskStatus(UUID taskId)`:
  - Отдает текущее состояние задачи клиенту или выбрасывает 404, если задача не найдена.

#### 3. Эндпоинты в контроллере:
- В [`RecipeController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java):
  - `POST /api/recipes/{id}/nutrition-report` — запуск задачи (возвращает HTTP 202 Accepted + `taskId`).
  - `GET /api/recipes/nutrition-report/{taskId}` — опрос статуса и получение готового отчета.

---

## Требование 2. Потокобезопасный счётчик (`AtomicLong` и `synchronized`)

### Суть требования
Реализовать механизм подсчета просмотров рецептов с использованием механизмов потокобезопасности Java:
- `AtomicLong` (неблокирующий потокобезопасный счетчик на основе инструкции процессора CAS — Compare-And-Swap);
- `synchronized` (потокобезопасный счетчик на основе мьютекса/монитора блокировки объекта);
- Обычный примитив `int` (непотокобезопасный счетчик) для демонстрации проблемы гонки.

### Где и как реализовано в коде
- **Файл**: [`src/main/java/com/example/recipeplatform/service/RecipeViewCounterService.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/service/RecipeViewCounterService.java)

#### Реализация счетчиков:
```java
// 1. Обычный int (НЕ потокобезопасный)
private int unsafeCounter = 0;

// 2. Атомарный счетчик (потокобезопасный)
private final AtomicLong atomicCounter = new AtomicLong(0);

// 3. Синхронизированный счетчик (потокобезопасный)
private int synchronizedCounter = 0;
```

#### Инкремент счетчиков:
- `incrementUnsafe()`: `unsafeCounter++` — неатомарная операция. Состоит из 3 микро-шагов: чтение из памяти -> прибавление единицы в регистре -> запись обратно в память. Если два потока сделают это одновременно, один инкремент затрет другой (**Lost Update**).
- `incrementAtomic()`: `atomicCounter.incrementAndGet()` — атомарная аппаратная операция, гарантирующая точный результат без блокировки потоков.
- `incrementSynchronized()`: метод с ключевым словом `synchronized`, пускающий внутрь только один поток единовременно.

#### Привязка к просмотру рецепта:
В [`RecipeController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java) в методе получения рецепта по ID `GET /api/recipes/{id}`:
```java
@GetMapping("/{id}")
public RecipeDto getById(@PathVariable Long id) {
    recipeViewCounterService.recordView(); // увеличивает все 3 счетчика на 1
    return recipeService.getById(id);
}
```

#### Эндпоинты для анализа и сброса:
- `GET /api/recipes/views/stats` — возвращает объект `CounterStatsDto` со значениями всех трех счетчиков и количеством потерянных инкрементов (`lostUpdates = atomicCounter - unsafeCounter`).
- `POST /api/recipes/views/reset` — сбрасывает все счетчики в 0 перед началом теста.

---

## Требование 3. Демонстрация Race Condition (ExecutorService на 50+ потоков)

### Суть требования
Наглядно продемонстрировать в Java-коде возникновение состояния гонки при параллельной работе пула из 50+ потоков и показать, как `AtomicLong` и `synchronized` полностью предотвращают потерю данных.

### Где и как реализовано в коде
- **В сервисе**: Метод `demonstrateRaceCondition(int threadCount, int incrementsPerThread)` в [`RecipeViewCounterService.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/service/RecipeViewCounterService.java).
- **Принцип работы**:
  1. Создается пул из 50 потоков: `Executors.newFixedThreadPool(50)`.
  2. Используется `CountDownLatch startLatch = new CountDownLatch(1);`, чтобы все 50 потоков запустились **одновременно в одну миллисекунду** (максимизация конкуренции за ресурсы процессора).
  3. Каждый поток выполняет по 100 инкрементов (всего 50 × 100 = 5000 операций).
  4. Потокобезопасные счетчики (`atomic` и `sync`) показывают ровно `5000`.
  5. Непотокобезопасный `unsafeCounter` показывает меньшее число (например, `4312`), фиксируя потерю сотен обновлений!
- **Эндпоинт в Swagger**: `POST /api/recipes/demo/race-condition` — позволяет преподавателю в один клик запустить тест на 50 потоков и увидеть отчет:
```json
{
  "threadCount": 50,
  "incrementsPerThread": 100,
  "expectedTotal": 5000,
  "atomicCounterResult": 5000,
  "synchronizedCounterResult": 5000,
  "unsafeCounterResult": 4486,
  "lostUpdates": 514,
  "executionTimeMs": 42
}
```
- **Автотест**: [`RecipeViewCounterServiceTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/RecipeViewCounterServiceTest.java) автоматически проверяет этот сценарий при сборке проекта.

---

## Требование 4. Нагрузочное тестирование в Apache JMeter

### Суть требования
Провести реальное нагрузочное тестирование работающего HTTP-сервера с помощью утилиты **Apache JMeter**, пустив 50 параллельных потоков на эндпоинт просмотра рецепта, и показать расхождение значений счетчиков.

### Готовый файл тест-плана JMeter
- **Файл**: [`jmeter/recipe_view_concurrency.jmx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/jmeter/recipe_view_concurrency.jmx)
- **Параметры теста**:
  - `Number of Threads (users)`: **50** (50 параллельных клиентов);
  - `Ramp-up period`: **1 секунда** (все 50 пользователей стартуют почти одновременно);
  - `Loop Count`: **20** (каждый поток делает по 20 запросов);
  - `Всего HTTP-запросов`: **1000** (50 × 20);
  - `Целевой URL`: `GET http://localhost:8080/api/recipes/1`.

---

### Пошаговая инструкция проведения демонстрации для преподавателя

#### Шаг 1: Подготовка
1. Запустите приложение Spring Boot (`.\mvnw spring-boot:run`).
2. Откройте Swagger UI: `http://localhost:8080/swagger-ui.html`.
3. В разделе **Recipes** найдите `POST /api/recipes/views/reset` и нажмите **Execute** (счетчики обнулены).
4. Проверьте через `GET /api/recipes/views/stats`: все счетчики равны `0`.

#### Шаг 2: Запуск теста нагрузки в JMeter
**Вариант А — через графический интерфейс JMeter (GUI):**
1. Откройте JMeter.
2. Нажмите **File -> Open** и выберите файл `jmeter/recipe_view_concurrency.jmx`.
3. Нажмите зеленую кнопку **Start (Плей)**.
4. Во вкладке **Summary Report** вы увидите, как за 1-2 секунды улетает 1000 запросов с нулевым процентом ошибок (`Error %: 0.00%`).

**Вариант Б — через командную строку (CLI без GUI):**
```powershell
jmeter -n -t jmeter/recipe_view_concurrency.jmx -l jmeter/results.csv
```

#### Шаг 3: Проверка результатов в Swagger UI
1. Возвращаемся в Swagger UI на эндпоинт `GET /api/recipes/views/stats` и нажимаем **Execute**.
2. Получаем результат:
```json
{
  "atomicCounter": 1000,
  "synchronizedCounter": 1000,
  "unsafeCounter": 864,
  "lostUpdates": 136,
  "raceConditionObserved": true
}
```
3. **Что говорим преподавателю**:
   - На сервер было отправлено ровно 1000 запросов в 50 конкурентных потоков.
   - Потокобезопасные счетчики (`atomicCounter` и `synchronizedCounter`) зафиксировали ровно **1000** вызовов.
   - Непотокобезопасный счетчик (`unsafeCounter`) зафиксировал только **864** вызова из-за эффекта гонки потоков (Lost Updates).
   - Потеряно **136** обновлений. Проблема гонки доказана наглядно.

---

### Пошаговая инструкция демонстрации асинхронной задачи и внешнего API (Open Food Facts)

#### Вариант 1: Через веб-интерфейс (React SPA)
1. В браузере открываем `http://localhost:5173`.
2. Нажимаем кнопку **«Детали»** на любом рецепте (например, Борщ).
3. В открывшемся окне нажимаем фиолетовую кнопку **«Рассчитать КБЖУ (Async)»**.
4. **Что происходит на экране**:
   - Кнопка блокируется, появляется надпись: *«Выполняется расчет...»* со спиннером.
   - Ниже отображается карточка задачи: *«Статус задачи: IN_PROGRESS. Фоновый поток запрашивает калорийность ингредиентов через Open Food Facts API...»*.
   - Через 1–2 секунды (после завершения фоновых HTTP-запросов к внешнему API) статус меняется на **`COMPLETED`**, и на экране появляется готовая раскладка: суммарные калории, белки, жиры, углеводы и таблица по каждому ингредиенту.

#### Вариант 2: Через Swagger UI
1. Открываем `http://localhost:8080/swagger-ui.html`.
2. Находим раздел **Recipes**, эндпоинт `POST /api/recipes/{id}/nutrition-report`.
3. Указываем `id: 1` и нажимаем **Execute**.
4. Сервер мгновенно (за 5 мс) возвращает ответ `HTTP 202 Accepted`:
   ```json
   {
     "taskId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
     "status": "IN_PROGRESS",
     "message": "Fetching nutrition data from Open Food Facts API..."
   }
   ```
5. Копируем значение `taskId`.
6. Переходим к эндпоинту `GET /api/recipes/nutrition-report/{taskId}`, вставляем скопированный `taskId` и нажимаем **Execute**.
7. Получаем ответ со статусом `COMPLETED` и полным объектом `report` с КБЖУ каждого ингредиента.

#### Что сказать преподавателю при показе:
> *«Для реализации асинхронной операции мы подключили внешнее REST API сервиса Open Food Facts. Сервер в фоновом потоке пула `@Async` отправляет реальные HTTP-запросы в интернет, чтобы получить пищевую ценность ингредиентов. Так как сетевые запросы требуют времени (1-2 секунды), операция вынесена в фон: клиент сразу получает HTTP 202 и ID задачи, основной веб-поток не блокируется, а готовый результат забирается по готовности»*.

---

## Проверка тестов

Команда для запуска модульных тестов на многопоточность и асинхронность:
```powershell
cmd /c .\mvnw.cmd test -Dtest="RecipeViewCounterServiceTest,NutritionReportServiceTest"
```
Все 8 тестов проходят успешно (`BUILD SUCCESS`).
