# Recipe Platform

Учебный проект платформы обмена рецептами и подсчёта КБЖУ: REST API на Spring Boot + SPA-фронтенд на React. Бэкенд оформлен так, чтобы одновременно покрывать требования лабораторных работ 3–8.

> Сводный отчёт по ЛР 4–8 (таблица требований с путями к коду, пояснения, результаты тестов, JMeter и Race Condition): [REPORT.md](REPORT.md).

## Состав проекта

- `src/` — Spring Boot REST API (Java 21, PostgreSQL).
- `frontend/` — SPA на React + TypeScript + Vite + Tailwind CSS, полностью работающий поверх этого API.
- `Dockerfile`, `frontend/Dockerfile`, `Dockerfile.render`, `docker-compose.yml`, `render.yaml`, `.github/workflows/deploy.yml` — контейнеризация, CI/CD и деплой (лабораторная 8).
- `src/main/resources/schema.sql`, `src/main/resources/data.sql` — единственный источник схемы и стартовых данных PostgreSQL.

## КБЖУ считается динамически, а не хранится в рецепте

В сущности `Recipe` нет полей с калориями/белками/жирами/углеводами. Пищевая ценность каждого ингредиента (`caloriesPer100g`, `proteinsPer100g`, `fatsPer100g`, `carbohydratesPer100g`) хранится в сущности `Ingredient`, а КБЖУ рецепта:

- на 100 г, на весь рецепт и на порцию — вычисляется на лету в `RecipeMapper.calculateNutrition(...)` при каждом обращении к `GET /api/recipes` / `GET /api/recipes/{id}`, с учётом фактического количества и единицы измерения каждого ингредиента (`recipe_ingredient_details`, включая перевод "шт" в граммы через `gramsPerUnit`);
- итоговый отчёт также можно посчитать асинхронно — `POST /api/recipes/{id}/nutrition-report` запускает фоновую задачу (`@Async` + `CompletableFuture`, класс `NutritionCalculatorService`), которая суммирует те же реальные значения из связанных ингредиентов (без обращения к внешним сервисам и без захардкоженных значений) и транзакционно (`@Transactional(readOnly = true)`) читает связанные сущности; результат опрашивается через `GET /api/recipes/nutrition-report/{taskId}`.

Если изменить количество ингредиента в рецепте или его пищевую ценность в справочнике ингредиентов — КБЖУ рецепта на карточке и в отчёте пересчитается автоматически.

## Что сохранено из 3 лабораторной

В проекте по-прежнему реализованы все ключевые требования 3 лабы:

1. Сложный `GET`-запрос с фильтрацией по вложенным сущностям `author.username` и `category.name` через `@Query (JPQL)`.
2. Аналогичный запрос через `native query`.
3. Пагинация через `Pageable`.
4. In-memory индекс на основе `HashMap<CacheKey, Page<RecipeFilterDto>>`.
5. Инвалидация индекса при изменении данных рецептов.
6. Устранение `N+1` для лабораторных фильтрующих запросов за счёт проекций вместо ленивой загрузки полного графа сущностей.

### Лабораторные эндпоинты 3 лабы

- `GET /api/recipes/filter/jpql?authorUsername=anna&categoryName=Soups&page=0&size=5`
- `GET /api/recipes/filter/native?authorUsername=anna&categoryName=Soups&page=0&size=5`

Оба эндпоинта возвращают `Page<RecipeFilterDto>` и используют составной ключ кеша:

- тип запроса (`jpql` / `native`)
- имя автора
- название категории
- номер страницы
- размер страницы
- сортировка

## Что реализовано для 4 лабораторной

### 1. Глобальная обработка ошибок через `@RestControllerAdvice`

Глобальный обработчик расположен в `GlobalExceptionHandler` и покрывает:

- `NotFoundException`
- `MethodArgumentNotValidException`
- `ConstraintViolationException`
- `IllegalArgumentException`
- `MissingServletRequestParameterException`
- `MethodArgumentTypeMismatchException`
- `HttpMessageNotReadableException`
- `DataIntegrityViolationException`
- общий `Exception`

### 2. Валидация входных данных через `@Valid`

Валидация подключена для request body во всех основных `POST` и `PUT` endpoint.

Используются, например:

- `@NotBlank`
- `@NotNull`
- `@NotEmpty`
- `@Email`
- `@Valid` для вложенных DTO

Для query-параметров также используется валидация, например:

- `authorUsername` в фильтрующих endpoint
- `title` в `/api/recipes/search`

### 3. Единый формат ошибки для всех endpoint

Все ошибки теперь возвращаются в едином формате `ApiError`:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `details` (опционально)

`details` используется для структурированных данных:

- ошибки валидации полей request body
- ошибки валидации параметров

Пример ответа:

```json
{
  "timestamp": "2026-06-05T04:40:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request body.",
  "path": "/api/users",
  "details": {
    "fieldErrors": {
      "username": "must not be blank",
      "email": "must be a well-formed email address"
    }
  }
}
```

### 4. Логирование через `logback`

Настроен файл `logback-spring.xml`.

Что есть:

- вывод логов в консоль
- вывод логов в файл `logs/recipe-platform.log`
- уровни логирования для приложения, Spring и Hibernate
- ротация логов через `SizeAndTimeBasedRollingPolicy`

Параметры ротации:

- шаблон файлов: `logs/recipe-platform-%d{yyyy-MM-dd}.%i.log`
- максимальный размер файла: `10MB`
- история: `30` дней
- общий лимит: `1GB`

### 5. Аспект (AOP) для логирования времени выполнения сервисных методов

Реализован `LoggingAspect`, который перехватывает методы сервисного слоя:

- логирует время выполнения успешных вызовов
- логирует исключения
- логирует длительность даже при неуспешном завершении метода

### 6. Swagger/OpenAPI с описанием endpoint и DTO

Подключён `springdoc-openapi`.

Доступно:

- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`

В контроллерах и DTO используются аннотации:

- `@Tag`
- `@Operation`
- `@ApiResponse`
- `@Schema`
- `@Parameter`

## Что реализовано для 5 лабораторной

### Bulk-операция с бизнес-смыслом

Добавлен массовый импорт рецептов: один POST принимает список новых рецептов, у которых уже существуют автор, категория и ингредиенты.

- `POST /api/recipes/bulk` — атомарный импорт с `@Transactional`.
- `POST /api/recipes/bulk/no-tx` — импорт по одному рецепту без общей транзакции.

Оба endpoint принимают JSON-массив `RecipeCreateDto`. Внутри сервиса применяются `Stream API` для обработки списка и `Optional` при поиске автора, категории и ингредиентов.

### Демонстрация транзакционности

Передайте массив, в котором первый рецепт использует существующие идентификаторы, а во втором укажите несуществующий `ingredientId`.

- Для `/api/recipes/bulk` ответ будет `404`, а первый рецепт не останется в БД: транзакция откатит всю операцию.
- Для `/api/recipes/bulk/no-tx` ответ также будет `404`, но первый рецепт уже будет сохранён: каждый `saveAndFlush` фиксируется отдельной транзакцией репозитория.

Пример запроса:

```json
[
  {
    "title": "Bulk soup",
    "description": "First valid recipe",
    "authorId": 1,
    "categoryId": 1,
    "ingredientIds": [1],
    "steps": [{ "stepOrder": 1, "description": "Cook the soup" }]
  },
  {
    "title": "Broken bulk soup",
    "description": "Second recipe intentionally contains an invalid ingredient",
    "authorId": 1,
    "categoryId": 1,
    "ingredientIds": [999999],
    "steps": [{ "stepOrder": 1, "description": "This step is not saved" }]
  }
]
```

### Тесты

Добавлены Mockito unit-тесты для `RecipeService`: успешный bulk-импорт, обработка отсутствующего `Optional` и частичное сохранение без общей транзакции. Интеграционные тесты дополнительно подтверждают фактическое состояние PostgreSQL после ошибки для обоих вариантов.

## N+1 demo

Также сохранены endpoint из предыдущей части проекта:

- `GET /api/recipes/n-plus-one/problem`
- `GET /api/recipes/n-plus-one/solution`

Они позволяют сравнить количество SQL-запросов до и после оптимизации.

## Лабораторная 8: Docker, CI/CD и деплой

### Что добавлено

| Файл | Назначение |
|------|------------|
| `Dockerfile` | Multi-stage образ бэкенда: сборка через Maven Wrapper на `eclipse-temurin:21-jdk` → запуск на `eclipse-temurin:21-jre-alpine` от непривилегированного пользователя |
| `frontend/Dockerfile` | Multi-stage образ SPA: `npm ci && npm run build` на `node:20-alpine` → раздача статики через `nginx:1.27-alpine` |
| `frontend/nginx.conf.template` | Nginx: SPA-fallback на `index.html`, проксирование `/api` в бэкенд, gzip, долгий кэш для `/assets` |
| `.dockerignore`, `frontend/.dockerignore` | В build context не попадают `target`, `node_modules`, логи, `.git`, `.env` |
| `docker-compose.yml` | 3 сервиса: `db` (PostgreSQL 16), `backend`, `frontend`. Volume `pgdata`, запуск по healthcheck'ам |
| `.env.example`, `frontend/.env.example` | Шаблоны переменных окружения |
| `src/main/resources/application.yml` | Конфигурация Spring Boot, все параметры читаются через `${VAR:default}` (заменил `application.properties`) |
| `.github/workflows/deploy.yml` | GitHub Actions: build → test → docker build → deploy на Render → healthcheck |
| `Dockerfile.render` | Образ для PaaS: SPA собирается и встраивается в Spring Boot, всё приложение на одном URL |
| `render.yaml` | Render Blueprint: бесплатные PostgreSQL + один Web Service (Docker, `Dockerfile.render`) |

Изменения в коде ради этой лабораторной:

- добавлен `spring-boot-starter-actuator`, поэтому работает эндпоинт `GET /actuator/health` (ответ `{"status":"UP"}`);
- CORS больше не зашит в коде: `WebConfig` берёт origin'ы из `CORS_ORIGINS` (список через запятую, можно с `*`);
- фронтенд умеет ходить на отдельный хост API через `VITE_API_URL`. Если переменная пустая, запросы идут на относительный `/api`, как раньше.

### Оптимизация образов

- **Кэширование слоёв.** Сначала копируются только `pom.xml` / `package-lock.json` и скачиваются зависимости (`mvnw dependency:go-offline`, `npm ci`). Этот слой пересобирается только при изменении зависимостей, а не при каждой правке кода. Дополнительно используется BuildKit cache mount (`--mount=type=cache`) для `~/.m2` и `~/.npm`.
- **Слоистый jar.** Fat-jar раскладывается через `java -Djarmode=tools ... extract --layers` на `dependencies / spring-boot-loader / snapshot-dependencies / application`. При изменении кода меняется только тонкий слой `application`.
- **Минимальный runtime.** В итоговых образах нет JDK, Maven, Node.js и исходников. Остаются только JRE Alpine + приложение (≈ 400 МБ вместе с JRE) и Nginx Alpine со статикой (≈ 75 МБ).
- **Healthcheck'и** встроены в оба образа (`HEALTHCHECK`), JVM настроена под контейнер (`-XX:MaxRAMPercentage`).

### Переменные окружения

| Переменная | Где используется | По умолчанию | Описание |
|------------|------------------|--------------|----------|
| `DB_URL` | backend | `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` | JDBC-URL базы данных |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | backend | `localhost` / `5432` / `recipe_db` | Используются, если `DB_URL` не задан (так настроен Render) |
| `DB_USERNAME` | backend, db | `postgres` | Пользователь БД |
| `DB_PASSWORD` | backend, db | — | Пароль БД (**только в `.env` / секретах**) |
| `PORT` | backend | `8080` | Порт HTTP-сервера (PaaS передают его сами) |
| `CORS_ORIGINS` | backend | `http://localhost:*,http://127.0.0.1:*` | Разрешённые origin'ы через запятую |
| `API_URL` → `VITE_API_URL` | frontend (build) | пусто | URL бэкенда, вшивается в бандл при сборке |
| `BACKEND_PORT`, `FRONTEND_PORT`, `DB_EXTERNAL_PORT` | docker-compose | `8080`, `3000`, `5433` | Порты на хосте |
| `JAVA_OPTS` | backend | `-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC` | Параметры JVM |

В `application.yml` используется синтаксис Spring `${VAR_NAME:default}`, например `${PORT:8080}`. В `docker-compose.yml` используется синтаксис Compose `${VAR_NAME:-default}`: это та же подстановка значения по умолчанию, но Compose требует `:-`.

> Файл `.env` добавлен в `.gitignore`. Коммитится только `.env.example`.

### Локальный запуск через Docker Compose

Нужен только **Docker Desktop** (или Docker Engine + Compose v2). JDK, Node.js и PostgreSQL устанавливать не нужно.

1. Скопируйте шаблон переменных и при желании поменяйте пароль:

   ```bash
   cp .env.example .env
   ```

   В PowerShell: `Copy-Item .env.example .env`.

2. Соберите и запустите весь стек:

   ```bash
   docker compose up --build
   ```

   Или `docker-compose up --build` для старого Compose v1. Порядок старта:

   1. `db`: PostgreSQL стартует, и healthcheck `pg_isready` ждёт полной готовности БД;
   2. `backend`: стартует **только после** `db: healthy` (`depends_on.condition: service_healthy`). Spring Boot сам создаёт схему и seed-данные из `schema.sql` / `data.sql`;
   3. `frontend`: стартует после `backend: healthy` (проверка `/actuator/health`).

3. Откройте в браузере:

   | Что | URL |
   |-----|-----|
   | SPA-фронтенд | http://localhost:3000 |
   | REST API | http://localhost:8080/api/recipes |
   | Healthcheck | http://localhost:8080/actuator/health |
   | OpenAPI JSON | http://localhost:8080/v3/api-docs |
   | PostgreSQL (pgAdmin/psql) | `localhost:5433`, БД `recipe_db`, логин/пароль из `.env` |

Полезные команды:

```bash
docker compose ps
```

```bash
docker compose logs -f backend
```

```bash
docker compose down
```

```bash
docker compose down -v
```

- `docker compose ps` показывает статус и health контейнеров;
- `docker compose logs -f backend` показывает логи бэкенда;
- `docker compose down` останавливает стек, данные БД сохраняются в volume `pgdata`;
- `docker compose down -v` останавливает стек **и удаляет данные БД**. При следующем старте база будет пересоздана из seed-данных.

### CI/CD: GitHub Actions

Пайплайн `.github/workflows/deploy.yml` запускается на push и pull request в `main` / `master`, а также вручную (`workflow_dispatch`). Он состоит из четырёх последовательных этапов (jobs):

```
build ──► test ──► deploy ──► healthcheck
  │         │         │            │
  │         │         │            └─ curl APP_URL/actuator/health до "UP" (≤ 25 мин) + smoke-тест SPA и /api/categories
  │         │         └─ POST на Render Deploy Hook (только push в main/master)
  │         └─ mvnw verify: unit + интеграционные тесты на PostgreSQL service + JaCoCo 100% по сервисам
  └─ mvnw package, npm ci + tsc + vite build, сборка Dockerfile / frontend/Dockerfile / Dockerfile.render
```

1. **build.** `./mvnw package` (без тестов), `npm ci` + `npm run build` (проверка типов TypeScript + Vite), сборка трёх Docker-образов с кэшем слоёв GitHub Actions. Jar и `dist` сохраняются как artifact `build-artifacts`.
2. **test.** Поднимает service-контейнер `postgres:16` и выполняет `./mvnw -B verify`: 151 тест (unit, интеграционные `@SpringBootTest` с `RUN_DB_TESTS=true`) и проверку покрытия сервисов JaCoCo (100%). Таблица покрытия выводится в Summary запуска, отчёты сохраняются как artifact `test-reports`.
3. **deploy.** Только при push в `main`/`master`: POST на Deploy Hook Render с `ref=<commit sha>`.
4. **healthcheck.** До ~25 минут опрашивает `APP_URL/actuator/health` через `curl`, пока не получит `"status":"UP"`, затем делает smoke-тест главной страницы и `GET /api/categories`. Если не дождался, job падает.

Если секрет `RENDER_DEPLOY_HOOK` или переменная `APP_URL` не заданы, `deploy` проходит с предупреждением, а `healthcheck` пропускается. Так копия проекта у другого человека не «краснеет», пока он не настроил хостинг.

### Размещение на бесплатном хостинге Render

**Схема на Render (бесплатно):**

```
https://<имя>.onrender.com ──► Web Service «recipe-platform» (Docker, Dockerfile.render, free)
                                  ├─ /              React SPA (собрана внутрь Spring Boot)
                                  ├─ /api/**        REST API
                                  └─ /actuator/health
                                        │  DB_HOST / DB_PORT / DB_NAME / DB_USERNAME / DB_PASSWORD
                                        ▼  (Render подставляет сам)
                               PostgreSQL «recipe-platform-db» (free)
```

Почему один сервис, а не два: SPA и API живут на одном домене, поэтому не нужны ни CORS, ни `VITE_API_URL`, ни ручная правка адресов, если имя на `onrender.com` уже занято. Для локального запуска по-прежнему используются три контейнера из `docker-compose.yml`.

> **Ограничения бесплатного тарифа Render:**
> - сервис засыпает после ~15 минут без запросов, первый запрос будит его ~50 с;
> - 512 МБ RAM (приложение использует ~250 МБ, проверено с `--memory=512m`);
> - бесплатная PostgreSQL работает 30 дней, потом её нужно пересоздать (данные заново заполнятся из `data.sql`);
> - на один аккаунт — одна бесплатная PostgreSQL.

#### Шаг 1. Код в GitHub

Render берёт код из GitHub-репозитория. Если проект уже в вашем репозитории, достаточно запушить изменения:

```powershell
git add .
git commit -m "LR8: Docker, CI/CD, Render"
git push origin master
```

#### Шаг 2. Создать всё в Render через Blueprint

1. Откройте https://dashboard.render.com и войдите через **GitHub** (Sign in with GitHub). Банковская карта для бесплатного тарифа не нужна.
2. **New → Blueprint**.
3. Если репозитория нет в списке: **Configure GitHub → Only select repositories →** выберите репозиторий → **Save**.
4. Выберите репозиторий и ветку `master`. Render прочитает `render.yaml` и покажет:
   - `recipe-platform-db` (PostgreSQL, Free);
   - `recipe-platform` (Web Service, Docker, Free).
5. Введите любое имя Blueprint (например `recipe-platform`) и нажмите **Deploy Blueprint / Apply**.
6. Первая сборка занимает 5–10 минут: **Dashboard → recipe-platform → Events/Logs**. Готово, когда статус **Live**, а в логе есть `Started RecipePlatformApplication`.
7. Адрес приложения указан в левом верхнем углу страницы сервиса: `https://recipe-platform-xxxx.onrender.com`. Проверьте:
   - `https://<адрес>/` — SPA;
   - `https://<адрес>/api/recipes` — JSON с рецептами;
   - `https://<адрес>/actuator/health` — `{"status":"UP",...}`.

На этом приложение уже размещено. Шаги 3–4 нужны для автоматического деплоя из GitHub Actions после каждого push.

#### Шаг 3. Deploy Hook

**Render → recipe-platform → Settings → Deploy Hook → Copy.** Вид: `https://api.render.com/deploy/srv-xxxxxxxx?key=yyyyyyyy`. Это секрет, никому не показывайте.

#### Шаг 4. GitHub Secrets и Variables

GitHub → репозиторий → **Settings → Secrets and variables → Actions**:

| Где | Имя | Значение |
|-----|-----|----------|
| вкладка **Secrets** → New repository secret | `RENDER_DEPLOY_HOOK` | Deploy Hook из шага 3 |
| вкладка **Variables** → New repository variable | `APP_URL` | адрес сервиса, например `https://recipe-platform-xxxx.onrender.com` |

После этого каждый push в `master` проходит так: тесты → сборка образов → деплой на Render → healthcheck. Результат смотрите во вкладке **Actions**, job `healthcheck` должен завершиться сообщением `Application is healthy`.

> В `render.yaml` стоит `autoDeploy: false`, поэтому Render не выкатывает непроверенный код сам. Если не хотите настраивать шаги 3–4, обновить сервис можно кнопкой **Manual Deploy → Deploy latest commit** в Render.

### Как разместить проект на хостинге с компьютера другого человека

Предположим, другой человек получил zip (см. раздел «Перенос проекта на другой компьютер») и хочет развернуть **свою** копию на **своём** бесплатном Render. Ничего устанавливать не нужно, достаточно браузера и двух бесплатных аккаунтов: GitHub и Render.

1. **Создать свой репозиторий на GitHub.** github.com → **New repository** → имя `recipe-platform` → Public или Private → **Create repository**. Галочку «Add a README» не ставить.
2. **Загрузить в него проект** одним из способов:
   - **Без git, через браузер:** распакуйте zip. На странице пустого репозитория нажмите **uploading an existing file** и перетащите **содержимое** папки проекта (не саму папку) → **Commit changes**. Скрытые папки `.github` и `.mvn` браузер при перетаскивании может пропустить: проверьте, что они появились. Если нет, создайте файлы через **Add file → Create new file**, указав путь `.github/workflows/deploy.yml` и `.mvn/wrapper/maven-wrapper.properties`, и вставьте содержимое из распакованной папки.
   - **С установленным git (надёжнее):** в PowerShell в папке проекта:

     ```powershell
     git init
     git add .
     git commit -m "Recipe platform"
     git branch -M master
     git remote add origin https://github.com/<ваш-логин>/recipe-platform.git
     git push -u origin master
     ```

3. **Развернуть на Render.** Выполните шаг 2 из раздела выше: dashboard.render.com → Sign in with GitHub → **New → Blueprint** → выбрать **свой** репозиторий → **Apply**. Через 5–10 минут приложение доступно по адресу `https://recipe-platform-xxxx.onrender.com`.
4. **(Необязательно) Автодеплой из GitHub Actions.** Выполните шаги 3–4: `RENDER_DEPLOY_HOOK` в Secrets и `APP_URL` в Variables **своего** репозитория.

Всё, что относится к хостингу, описано в файлах проекта (`render.yaml`, `Dockerfile.render`, `deploy.yml`). Пароли БД Render генерирует сам, поэтому ничего править в коде не нужно.

**Если что-то пошло не так:**

| Симптом | Что сделать |
|---------|-------------|
| Blueprint пишет, что бесплатная БД уже есть | На аккаунте Render допускается одна free-PostgreSQL. Удалите старую (Dashboard → БД → Settings → Delete) или используйте другой аккаунт |
| Сборка упала на `npm ci` / `mvnw` | Откройте Logs. Чаще всего в репозиторий не попали `frontend/package-lock.json`, `.mvn/wrapper/maven-wrapper.properties` или `mvnw` |
| Статус *Deploy failed*, в логе `Timed out ... health check` | Первая сборка + старт JVM на free-тарифе медленные: нажмите **Manual Deploy** ещё раз |
| Сайт открывается ~50 секунд | Бесплатный сервис «спал». Это нормально, повторные запросы быстрые |
| Через 30 дней пропали данные / ошибка подключения к БД | Истёк срок бесплатной PostgreSQL: удалите её и снова примените Blueprint (**Blueprints → Sync**) |
| В Actions job `deploy` с предупреждением, `healthcheck` пропущен | Не заданы `RENDER_DEPLOY_HOOK` / `APP_URL`: это нормально, деплой просто пропущен |

### Альтернативы: Koyeb / Railway

`Dockerfile.render` подходит для любого PaaS с Docker, потому что порт берётся из `PORT`, а БД задаётся переменными:

- **Koyeb** (есть free-инстанс): Create Service → GitHub → Builder: **Dockerfile**, путь `Dockerfile.render`, порт `8080`, health check `/actuator/health`. БД: бесплатная Koyeb Postgres или Neon. Переменные: `DB_URL=jdbc:postgresql://<host>:5432/<db>?sslmode=require`, `DB_USERNAME`, `DB_PASSWORD`.
- **Railway** (пробный кредит): New Project → Deploy from GitHub repo → переменная `RAILWAY_DOCKERFILE_PATH=Dockerfile.render` → добавить PostgreSQL. `DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}`, `DB_USERNAME=${{Postgres.PGUSER}}`, `DB_PASSWORD=${{Postgres.PGPASSWORD}}`.

Healthcheck в CI работает для любого хостинга: достаточно указать его адрес в `APP_URL`.

## Запуск проекта без Docker

> Самый простой способ — `docker compose up --build` (см. раздел выше). Ниже описан ручной запуск.


### Требования

- `JDK 21`
- `PostgreSQL` (локально или в контейнере)
- `Maven` или `Maven Wrapper`
- `Node.js 18+` и `npm` — только для фронтенда

### 1. PostgreSQL

Создайте пустую базу данных `recipe_db` (например, через `psql` или pgAdmin):

```sql
CREATE DATABASE recipe_db;
```

Схема и стартовые данные создаются не вручную и не через Java-код, а автоматически при старте приложения: Spring Boot выполняет `src/main/resources/schema.sql` (таблицы, ключи, связи), а затем `src/main/resources/data.sql` (базовые пользователи, категории, ингредиенты с КБЖУ на 100 г и несколько рецептов с шагами и составом). Оба скрипта написаны идемпотентно (`CREATE TABLE IF NOT EXISTS`, `ON CONFLICT DO NOTHING`, `WHERE NOT EXISTS`), поэтому их можно безопасно выполнять при каждом перезапуске (`spring.sql.init.mode=always`) — данные не дублируются.

Hibernate при этом работает в режиме `spring.jpa.hibernate.ddl-auto=validate`: он **не создаёт и не изменяет** таблицы сам, а только проверяет, что JPA-сущности соответствуют схеме из `schema.sql`.

#### Параметры подключения к БД

По умолчанию используются:

- `DB_URL=jdbc:postgresql://localhost:5432/recipe_db`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=07Omemeg`

При необходимости значения можно переопределить через переменные окружения (например, `DDL_AUTO=none`, если Hibernate не должен даже валидировать схему).

#### Подключение pgAdmin

Приложение подключается не к pgAdmin, а к PostgreSQL-серверу; pgAdmin нужен только для просмотра той же базы. В pgAdmin создайте server connection со значениями:

- Host name/address: `localhost`
- Port: `5432`
- Maintenance database: `postgres`
- Username: значение `DB_USERNAME` (по умолчанию `postgres`)
- Password: значение `DB_PASSWORD`

Затем откройте базу `recipe_db` в `Databases`. URL приложения должен совпадать: `jdbc:postgresql://localhost:5432/recipe_db`.

### 2. Бэкенд

```powershell
.\mvnw spring-boot:run
```

По умолчанию API поднимается на `http://localhost:8080`. Swagger UI доступен на `http://localhost:8080/swagger-ui.html` (включить, если отключён, свойством `springdoc.swagger-ui.enabled=true`), OpenAPI JSON — на `http://localhost:8080/v3/api-docs`.

### 3. Фронтенд

Фронтенд — отдельное SPA-приложение в `frontend/`, обращается к бэкенду по адресу `http://localhost:8080` (см. `frontend/vite.config.ts`); CORS для `http://localhost:*` уже настроен на бэкенде в `WebConfig`.

```powershell
cd frontend
npm install
npm run dev
```

Приложение откроется на `http://localhost:5173`. Для продакшен-сборки:

```powershell
npm run build
npm run preview
```

### Что можно делать в SPA

- Смотреть список рецептов с поиском по названию и фильтрами по авторам, категориям и диапазону КБЖУ на порцию (калории/белки/жиры/углеводы).
- Открывать детальную карточку рецепта: состав (ManyToMany `Recipe` ↔ `Ingredient` с количеством и единицей измерения на порцию) и шаги приготовления (OneToMany `Recipe` → `CookingStep`).
- Создавать и редактировать рецепты, динамически добавляя/убирая ингредиенты (с количеством и единицей) и шаги.
- Удалять рецепты, ингредиенты и категории на вкладке «Справочники».
- Запускать асинхронный расчёт КБЖУ и наблюдать за статусом фоновой задачи (`IN_PROGRESS` → `COMPLETED`).

## Перенос проекта на другой компьютер (zip-архив)

Проект не привязан к конкретной машине: весь код, схема БД, стартовые данные и Docker-конфигурация лежат в папке проекта.

### 1. Как упаковать проект (на своём компьютере)

В архив **не нужно** класть то, что собирается заново: `frontend/node_modules/`, `frontend/dist/`, `target/`, `logs/`, `.git/`, `.idea/`. Также **нельзя** класть `.env` с паролями, потому что получатель создаст свой из `.env.example`.

**Способ 1: одной командой.** Откройте PowerShell в папке **над** проектом (там, где лежит `recipe-platform (LR8)`) и выполните:

```powershell
tar -a -c -f recipe-platform.zip --exclude=node_modules --exclude=target --exclude=dist --exclude=logs --exclude=.git --exclude=.idea --exclude=.env --exclude=*.log "recipe-platform (LR8)"
```

Получится `recipe-platform.zip` размером меньше 1 МБ. `tar` встроен в Windows 10/11.

**Способ 2: вручную.** Удалите из копии проекта папки `frontend/node_modules`, `frontend/dist`, `target`, `logs` и файл `.env`. Затем нажмите правой кнопкой по папке → **Отправить → Сжатая ZIP-папка**.

Обязательно должны попасть в архив: `src/`, `frontend/` (с `package.json` и `package-lock.json`), `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf.template`, `docker-compose.yml`, `.env.example`, `.dockerignore`, `Dockerfile.render`, `render.yaml`, `.github/`.

### 2. Как запустить у получателя: вариант А, через Docker (рекомендуется)

Нужен **только Docker Desktop** (https://www.docker.com/products/docker-desktop/). Java, Node.js, Maven и PostgreSQL устанавливать **не нужно**, всё собирается внутри контейнеров.

1. Установите и запустите Docker Desktop. Дождитесь статуса *Engine running*.
2. Распакуйте архив, например в `C:\projects\`.
3. Откройте PowerShell в распакованной папке проекта (где лежит `docker-compose.yml`):

   ```powershell
   cd "C:\projects\recipe-platform (LR8)"
   ```

4. Создайте файл с настройками. Шаг необязательный: без `.env` используются значения по умолчанию, пароль БД `postgres`.

   ```powershell
   Copy-Item .env.example .env
   ```

5. Соберите и запустите всё одной командой:

   ```powershell
   docker compose up --build
   ```

   Первый запуск занимает 3–7 минут, потому что скачиваются образы и зависимости. Когда в логе появится `Started RecipePlatformApplication`, откройте:

   - http://localhost:3000 — приложение (SPA);
   - http://localhost:8080/api/recipes — REST API;
   - http://localhost:8080/actuator/health — `{"status":"UP"}`.

6. Остановка: `Ctrl+C` или `docker compose down`. Данные БД сохраняются, при следующем `docker compose up` всё поднимется за несколько секунд.

Если порт 3000, 8080 или 5433 занят, поменяйте `FRONTEND_PORT`, `BACKEND_PORT` или `DB_EXTERNAL_PORT` в `.env`.

**Ошибка `password authentication failed for user "postgres"` и бэкенд постоянно перезапускается.** PostgreSQL запоминает пароль при **первом** создании volume `pgdata`. Если потом поменять `DB_PASSWORD` в `.env`, создать или удалить `.env`, пароли перестанут совпадать. Решение: пересоздать базу (стартовые данные загрузятся заново, добавленные вручную записи удалятся):

```powershell
docker compose down -v
docker compose up --build
```

### 3. Как запустить у получателя: вариант Б, без Docker (JDK + PostgreSQL + Node.js)

#### Требования на новом компьютере

- **JDK 21** (не 17 и не 8 — версия зафиксирована в `pom.xml`: `<java.version>21</java.version>`). Например, Eclipse Temurin: https://adoptium.net/temurin/releases/?version=21
- **PostgreSQL** (любая современная версия, 14+).
- **Node.js 18+** и `npm` — только для фронтенда.
- Интернет-соединение при первом запуске бэкенда — `mvnw` скачивает сам Maven и все зависимости проекта в `~/.m2/repository`.

#### Шаг 1. Распаковать архив

Распакуйте в любую папку без пробелов и не слишком длинным путём, например `C:\projects\recipe-platform`.

#### Шаг 2. Установить и настроить PostgreSQL

1. Установите PostgreSQL (https://www.postgresql.org/download/). При установке инсталлятор попросит задать пароль для пользователя `postgres` — запомните его (или сразу задайте `07Omemeg`, чтобы не менять конфиг проекта).
2. Запомните порт, который выбрал инсталлятор (по умолчанию `5432`; уточнить порт уже установленного сервера можно через `SELECT current_setting('port');` в psql/pgAdmin или в `postgresql.conf` в каталоге данных).
3. Создайте пустую базу данных:

   ```sql
   CREATE DATABASE recipe_db;
   ```

   Через psql:

   ```powershell
   & "C:\Program Files\PostgreSQL\<версия>\bin\psql.exe" -h localhost -p 5432 -U postgres -c "CREATE DATABASE recipe_db;"
   ```

   Таблицы и стартовые данные (пользователи, категории, ингредиенты, рецепты) создавать вручную не нужно — при первом запуске бэкенд сам выполнит `src/main/resources/schema.sql` и `src/main/resources/data.sql`.

4. Если пароль или порт отличаются от значений по умолчанию в проекте (`DB_PASSWORD=07Omemeg`, порт `5432`), не редактируйте `application.yml` — задайте переменные окружения перед запуском (см. Шаг 3).

#### Шаг 3. Запустить бэкенд

В корне проекта (`recipe-platform/`), в PowerShell:

```powershell
$env:JAVA_HOME = "C:\Path\To\jdk-21"        # если java -version показывает не 21-ю версию
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# только если у вас нестандартный порт/пароль/пользователь PostgreSQL:
$env:DB_URL = "jdbc:postgresql://localhost:5432/recipe_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "07Omemeg"

.\mvnw.cmd spring-boot:run
```

Первый запуск займёт несколько минут — Maven скачивает зависимости. Готово, когда в логе появится `Started RecipePlatformApplication in ... seconds`. API будет на `http://localhost:8080`.

#### Шаг 4. Запустить фронтенд

В отдельном терминале:

```powershell
cd frontend
npm install
npm run dev
```

Откройте `http://localhost:5173` — фронтенд обращается к бэкенду на `http://localhost:8080` (см. `frontend/vite.config.ts`), CORS уже разрешён для `localhost:*`.

#### Проверка

- `http://localhost:8080/api/recipes` — должен вернуть JSON со списком рецептов (10 штук из `data.sql`).
- `http://localhost:5173` — должна открыться SPA со списком рецептов, карточками и рабочими фильтрами.

#### Частые проблемы

| Проблема | Причина | Решение |
|---|---|---|
| `mvnw.cmd` пишет, что не может найти `maven-wrapper.properties` | Файл не попал в архив | Скопировать `.mvn/wrapper/maven-wrapper.properties` из репозитория (содержимое см. в самом файле в git-истории) |
| Ошибка компиляции про `release 21` / `invalid target release` | Установлен JDK младше 21 (или `JAVA_HOME` указывает не туда) | Поставить JDK 21 и явно выставить `JAVA_HOME`/`Path` перед `mvnw` |
| `FATAL: password authentication failed for user "postgres"` | Пароль PostgreSQL на новой машине не совпадает с `07Omemeg` | Задать реальный пароль через `$env:DB_PASSWORD` перед запуском, либо сменить пароль в PostgreSQL на `07Omemeg` через `ALTER USER postgres WITH PASSWORD '07Omemeg';` |
| `Connection refused` к БД | Неверный порт (у вас может быть не `5432`, если на машине уже стоял другой PostgreSQL) | Проверить порт в `postgresql.conf` и указать его в `DB_URL` |
| Hibernate падает на `Schema-validation` | `schema.sql` не выполнился (например, `spring.sql.init.mode` переопределён) или база не пустая/повреждена | Убедиться, что `recipe_db` создана пустой и `application.yml` не переопределён локальными env-переменными |
| Фронтенд открывается, но список рецептов пуст и в консоли браузера ошибка сети | Бэкенд не запущен или запущен на другом порту | Проверить, что `http://localhost:8080/api/recipes` отвечает |

## Тестирование и покрытие кода

### Что покрыто

- **Сервисный слой (`com.example.recipeplatform.service`) покрыт на 100%**: по инструкциям, строкам, веткам и методам.
- Это не просто цифра в отчёте, а правило сборки. В `pom.xml` настроен `jacoco-maven-plugin` с целью `check`, и `mvnw verify` **падает**, если покрытие сервисов опустится ниже 100%. Эта же проверка работает в GitHub Actions.
- 100% достигаются **одними unit-тестами** (Mockito), поэтому для проверки покрытия PostgreSQL не нужен.
- Дополнительно есть 12 интеграционных тестов `@SpringBootTest` (`RecipePlatformApplicationTests`) на настоящей БД. Они включаются переменной `RUN_DB_TESTS=true`: N+1, кеш и его инвалидация, транзакции bulk-операций, формат ошибок, OpenAPI.
- Также есть тесты мапперов, контроллеров, `GlobalExceptionHandler`, AOP-аспекта, валидации DTO и `TextNormalizer`. Всего 151 тест.

### Запуск тестов: вариант 1, установлен JDK 21

```powershell
.\mvnw.cmd verify
```

Unit-тесты выполняются, интеграционные пропускаются, покрытие проверяется.

С интеграционными тестами: нужна запущенная БД, например `docker compose up -d db` (порт 5433):

```powershell
$env:RUN_DB_TESTS = "true"
$env:DB_URL = "jdbc:postgresql://localhost:5433/recipe_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"   # или значение DB_PASSWORD из вашего .env
.\mvnw.cmd verify
```

### Запуск тестов: вариант 2, есть только Docker (JDK не нужен)

Из папки проекта в PowerShell, только unit-тесты и проверка покрытия:

```powershell
docker run --rm -v "${PWD}:/app" -v recipe-m2:/root/.m2 -w /app eclipse-temurin:21-jdk-jammy sh -c "sed -i 's/\r$//' mvnw && ./mvnw -B verify"
```

Все тесты, включая интеграционные. Сначала поднимите БД командой `docker compose up -d db`:

```powershell
docker run --rm --network recipe-platform_default -v "${PWD}:/app" -v recipe-m2:/root/.m2 -w /app -e RUN_DB_TESTS=true -e DB_URL=jdbc:postgresql://db:5432/recipe_db -e DB_USERNAME=postgres -e DB_PASSWORD=postgres eclipse-temurin:21-jdk-jammy sh -c "sed -i 's/\r$//' mvnw && ./mvnw -B verify"
```

Если вы создали `.env`, подставьте в `DB_PASSWORD` значение из него.

### Как понять, что всё хорошо

В конце вывода должно быть:

```
[INFO] Tests run: 151, Failures: 0, Errors: 0, Skipped: 0
[INFO] All coverage checks have been met.
[INFO] BUILD SUCCESS
```

Без БД будет `Skipped: 12`: это пропущенные интеграционные тесты, так и должно быть.

**HTML-отчёт о покрытии** создаётся в `target/site/jacoco/index.html`. Откройте его в браузере и перейдите в пакет `com.example.recipeplatform.service`: там везде 100%.

```powershell
start target\site\jacoco\index.html
```
