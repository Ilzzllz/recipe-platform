# Отчёт по лабораторным работам 4–8

**Проект:** «Книга рецептов и КБЖУ». Стек: Java 21 / Spring Boot 3.4 + React 18 / TypeScript / Vite + PostgreSQL 16 + Docker.

**Состояние на момент отчёта:**

| Проверка | Результат |
|----------|-----------|
| `mvnw verify` (все тесты, включая интеграционные на PostgreSQL) | **151 тест, 0 падений, 0 пропусков, BUILD SUCCESS** |
| Покрытие пакета `service` (JaCoCo, правило сборки) | **100%** инструкций, строк, веток и методов (`All coverage checks have been met`) |
| `npm run build` (tsc + vite) | успешно |
| `docker compose up --build` | 3 контейнера `healthy`, `/actuator/health` → `UP` |
| Нагрузочный тест JMeter | 1000 запросов, **0% ошибок**, ~524 req/s |
| Комментарии в исходном коде (Java, SQL, TS/TSX, CSS) | удалены (оставлена только директива `/// <reference types="vite/client" />`, без неё не собирается TypeScript) |

Пути в отчёте указаны от корня проекта. Префикс `B/` означает `src/main/java/com/example/recipeplatform/`.

---

## 1. Таблица прохождения требований

### ЛР 4. Error logging / handling

| № | Пункт задания | Статус | Где реализовано |
|---|---------------|--------|-----------------|
| 4.1 | Глобальная обработка ошибок `@RestControllerAdvice` | ✅ Реализовано | `B/exception/GlobalExceptionHandler.java`: 8 методов `@ExceptionHandler` (`handleNotFound`, `handleBadRequest`, `handleConflict`, `handleMethodArgumentNotValid`, `handleConstraintViolation`, `handleIntegrityViolation`, `handleNoResourceFound`, `handleGenericException`). Собственные исключения: `B/exception/NotFoundException.java`, `ConflictException.java`, `TransactionDemoException.java` |
| 4.2 | Валидация `@Valid` + `jakarta.validation` | ✅ Реализовано | DTO: `B/dto/UserCreateDto.java` (`@NotBlank`, `@Email`, `@Size`), `CategoryCreateDto`, `IngredientCreateDto` (`@DecimalMin`), `RecipeCreateDto` (`@NotBlank`, `@NotNull`, `@Valid` для вложенных списков), `RecipeIngredientCreateDto` (`@NotNull`, `@DecimalMin("0.001")`, `@Size`), `RecipeStepCreateDto`, `CookingStepCreateDto`, `TransactionTestRequestDto`. `@Valid` в 15 методах контроллеров `B/controller/*Controller.java`; `@Validated` + `@NotBlank` на `@RequestParam` в `RecipeController` |
| 4.3 | Единый формат ошибки | ✅ Реализовано | `B/exception/ApiError.java` (`timestamp`, `status`, `error`, `message`, `path`, `details`). Все обработчики строят ответ через `GlobalExceptionHandler.buildError(...)` |
| 4.4 | Логирование через `logback-spring.xml`, уровни, ротация | ✅ Реализовано | `src/main/resources/logback-spring.xml`: `CONSOLE` + `RollingFileAppender` → `logs/recipe-platform.log`, `SizeAndTimeBasedRollingPolicy` (по дням и по 10 МБ, `maxHistory=30`, `totalSizeCap=1GB`). Уровни: `com.example.recipeplatform=DEBUG`, `org.springframework.web=DEBUG`, `org.hibernate=WARN`, root `INFO`. `WARN`/`ERROR` пишутся из `GlobalExceptionHandler` |
| 4.5 | AOP-аспект `@Aspect` / `@Around` для времени выполнения сервисов | ✅ Реализовано | `B/aspect/LoggingAspect.java`, метод `logExecutionTime`, `@Around("execution(* com.example.recipeplatform.service.*.*(..))")`. Тест: `src/test/.../aspect/LoggingAspectTest.java` |
| 4.6 | Swagger / OpenAPI (`springdoc-openapi`) | ✅ Реализовано | `pom.xml` (`springdoc-openapi-starter-webmvc-ui 2.8.17`), `B/config/OpenApiConfig.java` (`@OpenAPIDefinition`), аннотации `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`, `@ExampleObject` во всех контроллерах, `@Schema` в DTO. Спецификация `GET /v3/api-docs`, UI `GET /swagger-ui.html` (`src/main/resources/static/swagger-ui.html`) |

### ЛР 5. Batch data processing & Testing

| № | Пункт задания | Статус | Где реализовано |
|---|---------------|--------|-----------------|
| 5.1 | Bulk-операция с бизнес-смыслом | ✅ Реализовано | `POST /api/recipes/bulk` — пакетный импорт рецептов: `B/controller/RecipeController.createBulk`, `B/service/RecipeService.createBulk` → `saveBulkRecipes` → `convertToRecipe`. В UI: `frontend/src/components/BulkRecipeModal.tsx` |
| 5.2 | Stream API и `Optional` в сервисах | ✅ Реализовано | `RecipeService`: `saveBulkRecipes` (`stream().map(...).map(...).toList()`), `mapRecipeIngredients` (`Optional.orElseThrow`, `stream().filter().findFirst().map().orElse()`), `findDetailedRecipe`; `IngredientService.delete` (`removeIf`, `forEach`); `NutritionReportService`, `UserService`, `CategoryService` (`findById(...).orElseThrow(...)`) |
| 5.3 | Транзакционность bulk: `@Transactional` против отсутствия транзакции | ✅ Реализовано (**исправлено при аудите**) | Bulk: `RecipeService.createBulk` (`@Transactional`) и `createBulkWithoutTransaction` (без транзакции, `POST /api/recipes/bulk/no-tx`). Отдельное демо: `B/service/RecipeTransactionScenarioService.saveWithoutTransactional` / `saveWithTransactional` (`@Transactional`), `B/controller/TransactionDemoController` (`POST /api/demo/transaction/without`, `/with`). **Исправлено:** демо-записи именовались `имя_маркер`, а контроллер считал `countBy…StartingWith(маркер)`, поэтому всегда показывал 0. Теперь маркер стоит префиксом, и частичное сохранение видно |
| 5.4 | Unit-тесты сервисов (JUnit 5 + Mockito) | ✅ Реализовано | `src/test/java/com/example/recipeplatform/service/`: `RecipeServiceUnitTest`, `RecipeServiceIngredientsTest`, `CategoryServiceTest`, `IngredientServiceTest`, `UserServiceTest`, `CookingStepServiceTest`, `NutritionCalculatorServiceTest`, `NutritionCalculatorServiceBranchesTest`, `NutritionReportServiceTest`, `RecipeTransactionScenarioServiceTest`, `RecipeViewCounterServiceTest`. **Покрытие пакета `service`: 100%** (правило `jacoco-check-services` в `pom.xml`) |

### ЛР 6. Concurrency

| № | Пункт задания | Статус | Где реализовано |
|---|---------------|--------|-----------------|
| 6.1 | Асинхронная операция `@Async` / `CompletableFuture`, Task ID и статус | ✅ Реализовано | `B/config/AsyncConfig.java` (`@EnableAsync`, пул `recipeTaskExecutor`, `taskStore` = `ConcurrentHashMap<UUID, AsyncTaskResponseDto>`). `B/service/NutritionReportService.startNutritionReport` возвращает `UUID`, `pollTaskStatus` отдаёт статус. `B/service/NutritionCalculatorService.calculateNutritionAsync` (`@Async("recipeTaskExecutor")`, `CompletableFuture<NutritionReportDto>`). Эндпоинты: `POST /api/recipes/{id}/nutrition-report` → `202 Accepted` + `taskId`, `GET /api/recipes/nutrition-report/{taskId}`. UI: `App.tsx` `handleCalculateNutrition` (опрос статуса) + `components/NutritionModal.tsx` |
| 6.2 | Потокобезопасный счётчик (`AtomicLong` / `synchronized`) | ✅ Реализовано | `B/service/RecipeViewCounterService.java`: `AtomicLong atomicCounter` (`incrementAtomic`), `synchronized incrementSynchronized`, небезопасный `int unsafeCounter` для сравнения. Инкремент на каждый `GET /api/recipes/{id}` (`RecipeController.getById` → `recordView`). Статистика `GET /api/recipes/views/stats`, сброс `POST /api/recipes/views/reset` |
| 6.3 | Race Condition на `ExecutorService` (50+ потоков) и исправление | ✅ Реализовано | `RecipeViewCounterService.demonstrateRaceCondition`: `ExecutorService` на `threadCount` потоков, общий старт через `CountDownLatch`, три счётчика (`int`, `AtomicLong`, `synchronized`). Эндпоинт `POST /api/recipes/demo/race-condition?threadCount=50&incrementsPerThread=100`. Тест: `RecipeViewCounterServiceTest` |
| 6.4 | Нагрузочное тестирование JMeter | ✅ Реализовано | `jmeter/recipe_view_concurrency.jmx`: 50 потоков × 20 итераций на `GET /api/recipes/1`, хост и порт параметризованы `${__P(host,localhost)}` / `${__P(port,8080)}`. Результаты последнего прогона: `jmeter/results.jtl`, HTML-отчёт `jmeter/report/index.html` |

### ЛР 7. Client

| № | Пункт задания | Статус | Где реализовано |
|---|---------------|--------|-----------------|
| 7.1 | SPA-клиент (React + TypeScript + Vite) | ✅ Реализовано | `frontend/` (`package.json`: React 18, TypeScript 5.6, Vite 6, Tailwind). Точка входа `frontend/src/main.tsx`, корневой компонент `frontend/src/App.tsx` |
| 7.2 | Полное взаимодействие с REST API | ✅ Реализовано | `frontend/src/api/client.ts`: типизированный `request<T>()`, обработка `ApiError`, методы для recipes, categories, ingredients, users, bulk, filter, nutrition-report, views. Типы в `frontend/src/types/index.ts`. Базовый URL через `VITE_API_URL` |
| 7.3a | OneToMany: Рецепт → Шаги | ✅ Реализовано | Модель: `B/model/Recipe.steps` (`@OneToMany(cascade = ALL, orphanRemoval = true)`) ↔ `B/model/CookingStep.recipe` (`@ManyToOne`). UI: `components/RecipeFormModal.tsx` (добавление, удаление, перестановка шагов), `components/RecipeModal.tsx` (отсортированный список шагов) |
| 7.3b | ManyToMany: Рецепт ↔ Ингредиенты с количеством | ✅ Реализовано | Модель: `Recipe.ingredients` (`@ManyToMany` + `@JoinTable recipe_ingredients`) и сущность связи с количеством `B/model/RecipeIngredient` (`quantity`, `unit`) — `Recipe.recipeIngredientDetails`. КБЖУ на порцию: `B/mapper/RecipeMapper.calculateNutrition`. UI: `RecipeFormModal.tsx` (выбор ингредиента, количество, единица г/мл/шт), `RecipeModal.tsx` и `RecipeCard.tsx` (состав и КБЖУ на порцию) |
| 7.4 | CRUD и динамическая фильтрация | ✅ Реализовано | Рецепты: create/read/update/delete в `App.tsx` (`handleSubmitRecipe`, `handleViewRecipe`, `handleEditRecipe`, `handleConfirmDeleteRecipe`). Категории, ингредиенты, пользователи: `components/CategoryIngredientManager.tsx`. Фильтры (`App.tsx`, `filteredRecipes`): по названию, авторам, категориям, диапазонам калорий, белков, жиров и углеводов на порцию; пагинация. Серверные фильтры JPQL и native: `GET /api/recipes/filter/jpql|native` |

### ЛР 8. Deploy

| № | Пункт задания | Статус | Где реализовано |
|---|---------------|--------|-----------------|
| 8.1 | Multi-stage Dockerfile бэкенда и фронтенда | ✅ Реализовано | `Dockerfile` (JDK 21 + Maven Wrapper → JRE 21 Alpine, слоистый jar, non-root, `HEALTHCHECK`), `frontend/Dockerfile` (Node 20 → Nginx 1.27 Alpine), `frontend/nginx.conf.template`. Для PaaS: `Dockerfile.render` (SPA встраивается в Spring Boot, один URL) |
| 8.2 | `docker-compose.yml` с PostgreSQL, healthcheck, volumes | ✅ Реализовано | `docker-compose.yml`: `db` (`postgres:16-alpine`, `pg_isready`), `backend` (`depends_on: db: service_healthy`), `frontend` (`depends_on: backend: service_healthy`). Volumes `pgdata`, `backend-logs` |
| 8.3 | Переменные окружения | ✅ Реализовано | `.env.example`, `frontend/.env.example`; `src/main/resources/application.yml` (`${PORT:8080}`, `${DB_URL:...}`, `${DB_USERNAME:...}`, `${DB_PASSWORD:...}`, `${CORS_ORIGINS:...}`); `docker-compose.yml` (`${VAR:-default}`); `B/config/WebConfig.java` (CORS из `app.cors.allowed-origins`) |
| 8.4 | Конфигурация бесплатного PaaS | ✅ Реализовано | `render.yaml` (Render Blueprint: free PostgreSQL + Docker Web Service, `healthCheckPath: /actuator/health`), `Dockerfile.render`. Проверено локально с лимитом `--memory=512m` (≈240 МБ) |
| 8.5 | CI/CD GitHub Actions: build, тесты, деплой, healthcheck | ✅ Реализовано (**переименовано при аудите**) | `.github/workflows/deploy.yml` (раньше назывался `ci-cd.yml`): 4 последовательных job — `build` (`mvnw package`, `npm run build`, 3 Docker-образа) → `test` (`mvnw verify` + PostgreSQL service + JaCoCo 100%) → `deploy` (Render Deploy Hook) → `healthcheck` (`curl` до `"status":"UP"` на `/actuator/health` + smoke-тест SPA и `/api/categories`) |

---

## 2. Пояснения к реализации и ответы на вопросы

### ЛР 4. Обработка ошибок, логирование, AOP, OpenAPI

**Схема обработки исключений:**

```
HTTP-запрос
   │
   ▼
DispatcherServlet ──► @Valid DTO ──(ошибка)──► MethodArgumentNotValidException ─┐
   │                                                                             │
   ▼                                                                             │
Controller ──► Service ──(throw)──► NotFoundException / ConflictException /     │
                                    IllegalArgumentException /                   │
                                    DataIntegrityViolationException / ...        │
                                                                                 ▼
                                               GlobalExceptionHandler (@RestControllerAdvice)
                                               ├─ выбирает @ExceptionHandler по типу
                                               ├─ пишет лог (WARN для 4xx, ERROR для 5xx и 409 БД)
                                               └─ buildError() → ResponseEntity<ApiError>
```

Соответствие исключений и HTTP-статусов:

| Исключение | Статус |
|------------|--------|
| `NotFoundException`, `NoResourceFoundException` | 404 |
| `IllegalArgumentException`, `MissingServletRequestParameterException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException` | 400 |
| `MethodArgumentNotValidException` (`@Valid` тела запроса) | 400 + `details.fieldErrors` |
| `ConstraintViolationException` (`@Validated` параметров) | 400 + `details.violations` |
| `ConflictException`, `DataIntegrityViolationException` | 409 |
| любое другое `Exception` | 500 (текст ошибки наружу не отдаётся) |

**Единый JSON ошибки** (реальные ответы запущенного приложения):

```json
POST /api/users  {"username":"","email":"bad"}
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request body.",
  "path": "/api/users",
  "details": { "fieldErrors": { "username": "must not be blank",
                                "email": "must be a well-formed email address" } },
  "timestamp": "2026-09-24T00:41:21.193240712"
}

GET /api/recipes/999999
{ "status": 404, "error": "Not Found", "message": "Recipe with id 999999 was not found",
  "path": "/api/recipes/999999", "details": null, "timestamp": "2026-09-24T00:41:21.312871186" }
```

**Архитектура AOP-логгера** (`LoggingAspect`): pointcut `execution(* com.example.recipeplatform.service.*.*(..))` охватывает все публичные методы сервисов. Совет `@Around` засекает `System.nanoTime()` до `joinPoint.proceed()`:
- при успехе пишет `INFO Method RecipeService.findAll executed in 12 ms`;
- при исключении пишет `WARN ... failed after N ms` и пробрасывает исключение дальше, в `GlobalExceptionHandler`.

Бизнес-код про логирование ничего не знает.

**Logback:** логи идут одновременно в консоль (удобно для `docker logs`) и в файл с ротацией по дате и размеру: `logs/recipe-platform-2026-09-24.0.log`, … Хранится до 30 дней, общий объём не больше 1 ГБ.

**OpenAPI:** `http://localhost:8080/v3/api-docs` отдаёт JSON-спецификацию, `http://localhost:8080/swagger-ui.html` — интерактивную документацию. У каждого эндпоинта есть описание, коды ответов (в том числе схема `ApiError`) и примеры тел запросов (например, пример bulk-запроса с «битым» ингредиентом).

### ЛР 5. Bulk-операция и транзакции

**Бизнес-смысл bulk-операции:** массовый импорт рецептов (например, перенос кулинарной книги). В UI это «Добавить пакет рецептов» (`BulkRecipeModal.tsx`).

Каждый элемент валидируется (`List<@Valid RecipeCreateDto>`, `@NotEmpty`), затем проходит конвейер Stream API:

```java
dtos.stream().map(this::convertToRecipe).map(recipeRepository::saveAndFlush).map(recipeMapper::toDto).toList();
```

Кэш фильтров сбрасывается в `finally`.

**Разница состояния БД при ошибке во втором элементе:**

| Сценарий | Эндпоинт | Что остаётся в БД |
|----------|----------|-------------------|
| `@Transactional` | `POST /api/recipes/bulk` | **ничего**: первый рецепт уже прошёл `saveAndFlush`, но при `NotFoundException` на втором Spring откатывает всю транзакцию |
| без транзакции | `POST /api/recipes/bulk/no-tx` | **первый рецепт сохранён**: каждый `saveAndFlush` коммитится в своей транзакции репозитория |

Отдельное демо на 4 сущностях (реальные ответы после исправления):

```json
POST /api/demo/transaction/without
{"scenario":"Without @Transactional","message":"User, Category and Ingredient saved (partial commit), recipe NOT saved due to error.",
 "persistedRecords":{"users":1,"categories":1,"ingredients":1,"recipes":0}}

POST /api/demo/transaction/with
{"scenario":"With @Transactional","message":"Everything rolled back due to intentional failure.",
 "persistedRecords":{"users":0,"categories":0,"ingredients":0,"recipes":0}}
```

Оба поведения проверяются автоматически интеграционными тестами `RecipePlatformApplicationTests` (`transactionalBulkShouldRollbackEveryRecipeWhenLaterItemFails`, `bulkWithoutTransactionShouldKeepEarlierRecipeWhenLaterItemFails` и их HTTP-варианты через MockMvc).

**Тесты и покрытие:**

| Группа | Тестов | Что проверяют |
|--------|--------|---------------|
| Unit-тесты сервисов (Mockito), 11 классов | 103 | вся бизнес-логика, включая все ветки единиц измерения, валидацию количеств, bulk, кэш, асинхронный расчёт, счётчики |
| Мапперы, контроллеры, обработчик ошибок, аспект, DTO, утилиты | 36 | маппинг и расчёт КБЖУ, делегирование контроллеров, формат `ApiError`, AOP, валидация |
| Интеграционные `@SpringBootTest` на PostgreSQL | 12 | N+1, кэш и его инвалидация, rollback bulk, формат ошибок по HTTP, OpenAPI |
| **Всего** | **151** | |

Покрытие пакета `service` = **100%** (инструкции, строки, ветки, методы). Правило `jacoco-check-services` в `pom.xml` роняет сборку при снижении покрытия. Отчёт: `target/site/jacoco/com.example.recipeplatform.service/index.html`.

### ЛР 6. Асинхронность, потокобезопасность, нагрузка

**Асинхронный процесс (расчёт КБЖУ рецепта):**

```
Клиент                        RecipeController / NutritionReportService         пул recipeTaskExecutor (4–10 потоков)
  │ POST /recipes/1/nutrition-report
  ├──────────────────────────► taskId = UUID, task IN_PROGRESS → taskStore
  │                            calculateNutritionAsync(1, taskId) ─────────────► @Async: читает рецепт, считает КБЖУ
  │ ◄── 202 {taskId, IN_PROGRESS}                                                по количествам и единицам,
  │ GET /nutrition-report/{taskId}  (опрос раз в секунду из App.tsx)             ждёт минимум 10 с (демо-прогресс)
  │ ◄── {IN_PROGRESS, "still being processed"}                                   task.status = COMPLETED + result
  │ GET /nutrition-report/{taskId}
  │ ◄── {COMPLETED, result: {totalCaloriesKcal: 342.1, ...}}
```

Реальный прогон для рецепта «Борщ»: старт `00:41:21.51`, завершение `00:41:31.53`, `COMPLETED`, `totalCaloriesKcal: 342.1`. При ошибке, например если рецепт не найден, задача получает статус `FAILED` с текстом причины. `taskStore` — это `ConcurrentHashMap`, безопасный для одновременной записи из пула и чтения из HTTP-потоков.

**Race Condition и его устранение** (`POST /api/recipes/demo/race-condition?threadCount=50&incrementsPerThread=1000`, реальный результат):

| Счётчик | Ожидалось | Получено | Потеряно |
|---------|-----------|----------|----------|
| `int unsafe++` | 50 000 | **49 096** | **904** |
| `AtomicLong.incrementAndGet()` | 50 000 | 50 000 | 0 |
| `synchronized` метод | 50 000 | 50 000 | 0 |

Почему `unsafe++` теряет обновления: это три операции (read, +1, write), и потоки перезаписывают результаты друг друга. `AtomicLong` решает это аппаратным CAS, `synchronized` — взаимным исключением по монитору. Все 50 потоков стартуют одновременно через `CountDownLatch`, поэтому гонка воспроизводится стабильно.

**JMeter** (`jmeter/recipe_view_concurrency.jmx`, 50 потоков × 20 итераций, `GET /api/recipes/1`, прогон на docker-compose):

| Метрика | Значение |
|---------|----------|
| Запросов | 1000 |
| Ошибок | 0 (0.00%) |
| Пропускная способность | ≈ 524 запроса/с |
| Среднее / медиана | 63.6 мс / 29 мс |
| 90% / 95% / 99% | 63 мс / 148 мс / 753 мс |
| Min / Max | 6 мс / 817 мс |
| Счётчики после теста (`/api/recipes/views/stats`) | `atomic=1000`, `synchronized=1000`, **`unsafe=957` (43 потерянных обновления)**, `raceConditionObserved=true` |

Как повторить (нужен только Docker):

```powershell
curl.exe -X POST http://localhost:8080/api/recipes/views/reset
docker run --rm --network recipe-platform_default -v "${PWD}\jmeter:/jmeter" justb4/jmeter:5.5 -n -t /jmeter/recipe_view_concurrency.jmx -Jhost=backend -Jport=8080 -l /jmeter/results.jtl -e -o /jmeter/report
curl.exe http://localhost:8080/api/recipes/views/stats
```

Перед повторным запуском удалите старые `jmeter/results.jtl` и `jmeter/report`: JMeter не перезаписывает непустую папку отчёта.

### ЛР 7. SPA-клиент

**Архитектура:**

```
main.tsx ─► App.tsx  (состояние: рецепты, справочники, фильтры, пагинация, тосты, опрос async-задачи)
            ├─ components/Navbar.tsx
            ├─ components/RecipeCard.tsx              карточка: категория, автор, КБЖУ на порцию, действия
            ├─ components/RecipeModal.tsx             просмотр: состав с количествами, шаги по порядку
            ├─ components/RecipeFormModal.tsx         создание и редактирование (OneToMany + ManyToMany)
            ├─ components/BulkRecipeModal.tsx         пакетное создание (ЛР 5)
            ├─ components/NutritionModal.tsx          асинхронный расчёт КБЖУ (ЛР 6)
            ├─ components/CategoryIngredientManager.tsx  CRUD категорий, ингредиентов, пользователей
            ├─ components/ConfirmModal.tsx, ToastContainer.tsx
            └─ api/client.ts ─► fetch → /api/**  (Vite proxy в dev, Nginx в Docker, тот же домен на Render)
```

**Формы для связей:**
- **OneToMany (Рецепт → Шаги):** в `RecipeFormModal` шаги хранятся списком `{stepOrder, description}`. Их можно добавлять, удалять и двигать вверх/вниз, `stepOrder` пересчитывается автоматически. На бэкенде `Recipe.replaceSteps()` полностью заменяет коллекцию, а `orphanRemoval` удаляет лишние шаги.
- **ManyToMany с количеством (Рецепт ↔ Ингредиенты):** в форме выбираются ингредиенты, у каждого задаются количество и единица (`г` / `мл` / `шт`). Уходит массив `recipeIngredients: [{ingredientId, quantity, unit}]`. Бэкенд нормализует единицы (`гр`, `грамм`, `pcs`, `штука` и т. д.) и сохраняет строки `recipe_ingredient_details`. КБЖУ считается динамически (для `шт` через `gramsPerUnit`) и показывается **на порцию** с учётом поля `portions`.

**Фильтрация** (`App.tsx`, `filteredRecipes`) работает мгновенно, без перезагрузки: строка поиска по названию, мультивыбор авторов и категорий, диапазоны «от–до» для калорий, белков, жиров и углеводов на порцию. Результат разбит на страницы по 6 карточек. Для ЛР 3 сохранены серверные фильтры JPQL и native SQL с пагинацией и кэшем.

**Ошибки API** показываются тостами на русском. Поля с ошибками валидации (`details.fieldErrors`) подсвечиваются прямо в форме.

### ЛР 8. Docker, CI/CD, PaaS

**Структура `docker-compose.yml`:**

```
db (postgres:16-alpine) ── healthcheck pg_isready ── volume pgdata
   ▲ depends_on: service_healthy
backend (Dockerfile) ── PORT, DB_URL, DB_USERNAME, DB_PASSWORD, CORS_ORIGINS из .env ── healthcheck /actuator/health ── :8080
   ▲ depends_on: service_healthy
frontend (frontend/Dockerfile, Nginx) ── /api → backend:8080, SPA fallback ── :3000
```

Запуск: `docker compose up --build`, затем `http://localhost:3000`.

**CI/CD** (`.github/workflows/deploy.yml`, push и PR в `main`/`master`), 4 этапа:

```
build ──► test ──► deploy ──► healthcheck
  │         │         │            │
  │         │         │            └─ curl APP_URL/actuator/health до "UP" (≤ 25 мин) + smoke-тест SPA и /api/categories
  │         │         └─ POST на Render Deploy Hook (только push в main/master)
  │         └─ mvnw verify: unit + интеграционные тесты на PostgreSQL service + JaCoCo 100% по сервисам
  └─ mvnw package, npm ci + tsc + vite build, сборка Dockerfile / frontend/Dockerfile / Dockerfile.render
```

Без секрета `RENDER_DEPLOY_HOOK` и переменной `APP_URL` job `deploy` проходит с предупреждением, `healthcheck` пропускается, и пайплайн остаётся зелёным.

**Проверка деплоя на PaaS (Render):**
1. Запушить репозиторий в GitHub.
2. dashboard.render.com → **New → Blueprint** → выбрать репозиторий → **Apply**. Создадутся `recipe-platform-db` (free PostgreSQL) и `recipe-platform` (Docker, `Dockerfile.render`).
3. Дождаться статуса **Live** и открыть:
   - `https://<имя>.onrender.com/` — SPA;
   - `/api/recipes` — JSON с рецептами;
   - `/actuator/health` — `{"status":"UP"}`;
   - `/swagger-ui.html` — документация API.
4. Для автодеплоя добавить в GitHub `RENDER_DEPLOY_HOOK` (Secret) и `APP_URL` (Variable). В **Actions** job `healthcheck` должен закончиться сообщением `Application is healthy`.

Подробные пошаговые инструкции, перенос проекта в zip и запуск тестов описаны в `README.md`.

---

## 3. Изменения, внесённые при финальном аудите

| Что | Почему |
|-----|--------|
| Удалены все комментарии из исходников: 35 в Java/SQL, 6 в TS/TSX (включая закомментированный мёртвый блок JSX в `App.tsx`), 1 в CSS | Требование задачи. Удаление через синтаксический разбор, поэтому `//` в строках, URL и регулярных выражениях не затронуты. Сохранена функциональная директива `/// <reference types="vite/client" />` и комментарии в Docker/CI/YAML-конфигурации |
| `RecipeTransactionScenarioService`: маркер стал префиксом имён | Демо «без транзакции» всегда показывало 0 сохранённых записей, разница с `@Transactional` не была видна (ЛР 5.3) |
| `.github/workflows/ci-cd.yml` → `deploy.yml` | Имя файла из требований ЛР 8.5 |
| `jmeter/recipe_view_concurrency.jmx`: `host`/`port` через `${__P(...)}` | Тест можно запускать из Docker и против удалённого хоста. Выполнен реальный прогон, результаты в `jmeter/report/` |
