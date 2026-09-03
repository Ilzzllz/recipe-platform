# Recipe Platform API

Учебный REST API на Spring Boot для платформы обмена рецептами. Проект сейчас оформлен так, чтобы одновременно покрывать требования 3 и 4 лабораторных работ.

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

## N+1 demo

Также сохранены endpoint из предыдущей части проекта:

- `GET /api/recipes/n-plus-one/problem`
- `GET /api/recipes/n-plus-one/solution`

Они позволяют сравнить количество SQL-запросов до и после оптимизации.

## Запуск проекта

### Требования

- `JDK 21`
- `PostgreSQL`
- `Maven` или `Maven Wrapper`

### Параметры подключения к БД

По умолчанию используются:

- `DB_URL=jdbc:postgresql://localhost:5432/recipe_db`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=07Omemeg`

При необходимости значения можно переопределить через переменные окружения.

`spring.jpa.hibernate.ddl-auto=update` сохраняет таблицы и данные между запусками. Автозаполнение демо-данными отключено по умолчанию; чтобы добавить их один раз в пустую базу, запустите приложение с `APP_SEED_ENABLED=true`.

### Подключение pgAdmin

Приложение подключается не к pgAdmin, а к PostgreSQL-серверу; pgAdmin нужен только для просмотра той же базы. В pgAdmin создайте server connection со значениями:

- Host name/address: `localhost`
- Port: `5432`
- Maintenance database: `postgres`
- Username: значение `DB_USERNAME` (по умолчанию `postgres`)
- Password: значение `DB_PASSWORD`

Затем создайте базу `recipe_db`, если её ещё нет, и откройте её в `Databases`. URL приложения должен совпадать: `jdbc:postgresql://localhost:5432/recipe_db`.

### Запуск

```powershell
.\mvnw spring-boot:run
```

## Тестирование

Для проверки используется:

```powershell
$env:JAVA_HOME="C:\Users\Lenovo\.jdks\ms-21.0.10"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:RUN_DB_TESTS="true"
.\mvnw test
```

Сейчас тесты покрывают:

- загрузку Spring-контекста;
- корректность составного ключа кеша;
- отсутствие `N+1` для `JPQL`- и `native`-фильтрации;
- работу кеша;
- инвалидацию кеша после изменения данных;
- единый формат ошибки для невалидных запросов;
- доступность OpenAPI-спеки.
