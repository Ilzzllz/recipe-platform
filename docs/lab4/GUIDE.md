# Гайдлайн по реализации Лабораторной работы №4

Данный документ описывает реализацию всех требований Лабораторной работы №4 (**Error logging/handling**) в проекте **Recipe Platform**.

Документ составлен максимально понятным языком, по шагам, с указанием конкретных файлов, классов и методов в кодовой базе.

---

## Краткое описание темы лабораторной работы

Лабораторная работа №4 посвящена трем ключевым аспектам промышленной разработки на Java Spring Boot:
1. **Обработка ошибок**: вместо падения приложения с непонятными стек-трейсами клиент должен получать предсказуемый и аккуратный JSON-ответ.
2. **Валидация данных**: проверка правильности входящих данных до того, как они попадут в бизнес-логику или базу данных.
3. **Наблюдаемость (Observability)**: правильная запись логов в консоль и файл с ротацией, а также автоматическое измерение скорости работы методов с помощью АОП (аспектно-ориентированного программирования).
4. **Документация**: автоматическая генерация интерактивной спецификации API (Swagger/OpenAPI).

---

## Требование 1. Глобальная обработка ошибок через `@ControllerAdvice`

### Суть требования
Когда внутри контроллера или сервиса происходит ошибка (например, запрашиваемый рецепт не найден или пользователь прислал некорректные данные), исключение не должно приводить к стандартной "белой странице" ошибки Spring (Whitelabel Error Page). Все исключения должны перехватываться в одной центральной точке.

### Где и как реализовано в коде
- **Файл**: [`src/main/java/com/example/recipeplatform/exception/GlobalExceptionHandler.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/exception/GlobalExceptionHandler.java)
- **Аннотация класса**: `@RestControllerAdvice` — сообщает Spring, что этот класс перехватывает ошибки от всех контроллеров и сразу сериализует результат в формат JSON (сочетание `@ControllerAdvice` и `@ResponseBody`).

### Перехватываемые типы исключений:
1. **`NotFoundException`** (HTTP 404 Not Found):
   - Метод: `handleNotFound(NotFoundException, HttpServletRequest)`
   - Срабатывает, когда запрошенный пользователь, рецепт, категория или ингредиент не найдены по ID.
2. **`MethodArgumentNotValidException`** (HTTP 400 Bad Request):
   - Метод: `handleMethodArgumentNotValid(MethodArgumentNotValidException, HttpServletRequest)`
   - Срабатывает, когда тело запроса (`@RequestBody`) не проходит правила аннотаций валидации (например, пустое имя). Собирает список всех ошибочных полей и сообщений в секцию `details.fieldErrors`.
3. **`ConstraintViolationException`** (HTTP 400 Bad Request):
   - Метод: `handleConstraintViolation(ConstraintViolationException, HttpServletRequest)`
   - Срабатывает при нарушении валидации параметров запроса (`@RequestParam`, `@PathVariable`), например, если передан пустой параметр поиска.
4. **`DataIntegrityViolationException`** (HTTP 409 Conflict):
   - Метод: `handleIntegrityViolation(DataIntegrityViolationException, HttpServletRequest)`
   - Срабатывает при конфликтах ограничений базы данных (например, попытка создать пользователя с уже занятым логином или email).
5. **`NoResourceFoundException`** (HTTP 404 Not Found):
   - Метод: `handleNoResourceFound(NoResourceFoundException, HttpServletRequest)`
   - Срабатывает, когда пользователь обратился по несуществующему URL адресу.
6. **`IllegalArgumentException`, `MissingServletRequestParameterException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`** (HTTP 400 Bad Request):
   - Метод: `handleBadRequest(Exception, HttpServletRequest)`
   - Срабатывает при передаче букв вместо чисел в ID, битом JSON-формате или отсутствии обязательного query-параметра.
7. **`Exception.class`** (HTTP 500 Internal Server Error):
    - Метод: `handleGenericException(Exception, HttpServletRequest)`
   - "Ловушка безопасности" для любых непредвиденных серверных ошибок. Логирует ошибку уровня `ERROR` с явным указанием статус-кода `HTTP 500 [INTERNAL_SERVER_ERROR]`, метода, URI и типа исключения, **без вывода стектрейсов в лог** (чистый однострочный структурированный лог), и отдает клиенту аккуратное сообщение в формате `ApiError`.

---

## Требование 2. Валидация входных данных через `@Valid`

### Суть требования
Сервер должен отклонять некорректные данные до того, как они начнут обрабатываться. Проверка должна работать для полей входящих DTO, вложенных объектов и параметров запроса.

### Где и как реализовано в коде

#### 1. Валидация тел запросов в контроллерах:
Во всех методах создания (`POST`) и обновления (`PUT`) перед параметром `@RequestBody` стоит аннотация `@Valid`:
- [`UserController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/UserController.java):
  - `create(@Valid @RequestBody UserCreateDto dto)`
  - `update(@PathVariable Long id, @Valid @RequestBody UserCreateDto dto)`
- [`CategoryController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/CategoryController.java):
  - `create(@Valid @RequestBody CategoryCreateDto dto)`
  - `update(@PathVariable Long id, @Valid @RequestBody CategoryCreateDto dto)`
- [`IngredientController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/IngredientController.java):
  - `create(@Valid @RequestBody IngredientCreateDto dto)`
  - `update(@PathVariable Long id, @Valid @RequestBody IngredientCreateDto dto)`
- [`CookingStepController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/CookingStepController.java):
  - `create(@Valid @RequestBody CookingStepCreateDto request)`
  - `update(@PathVariable Long id, @Valid @RequestBody CookingStepCreateDto request)`
- [`RecipeController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java):
  - `create(@Valid @RequestBody RecipeCreateDto dto)`
  - `update(@PathVariable Long id, @Valid @RequestBody RecipeCreateDto dto)`

#### 2. Валидационные аннотации в DTO-классах:
- [`UserCreateDto.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/dto/UserCreateDto.java):
  - `@NotBlank` для `username` (строка не должна быть пустой или состоять из одних пробелов).
  - `@NotBlank` и `@Email` для `email` (проверка корректного синтаксиса адреса электронной почты).
- [`CategoryCreateDto.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/dto/CategoryCreateDto.java):
  - `@NotBlank` для `name`.
- [`IngredientCreateDto.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/dto/IngredientCreateDto.java):
  - `@NotBlank` для `name`.
- [`CookingStepCreateDto.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/dto/CookingStepCreateDto.java):
  - `@NotNull` для `recipeId` и `stepOrder`.
  - `@NotBlank` для `description`.
- [`RecipeStepCreateDto.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/dto/RecipeStepCreateDto.java):
  - `@NotNull` для `stepOrder`.
  - `@NotBlank` для `description`.
- [`RecipeCreateDto.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/dto/RecipeCreateDto.java):
  - `@NotBlank` для `title` и `description`.
  - `@NotNull` для `authorId` и `categoryId`.
  - `@NotEmpty` для `ingredientIds` (список ингредиентов не должен быть пустым).
  - `@Valid @NotEmpty private List<RecipeStepCreateDto> steps;` — аннотация `@Valid` перед списком шагов включает **каскадную валидацию**: валидируется не только сам список (что он не пустой), но и каждый внутренний элемент `RecipeStepCreateDto`.

#### 3. Валидация параметров запроса (@RequestParam):
- В [`RecipeController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java) над классом установлена аннотация `@Validated`.
- В методе `getByTitle(@RequestParam @NotBlank(message = "Title cannot be blank") String title)` проверяется, что строка поиска не пустая.

---

## Требование 3. Единый формат ошибки для всех endpoint

### Суть требования
Все ответы об ошибках, независимо от того, в каком контроллере и по какой причине они возникли, должны иметь строго одинаковую структуру полей в JSON.

### Где и как реализовано в коде
- **Файл**: [`src/main/java/com/example/recipeplatform/exception/ApiError.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/exception/ApiError.java)
- **Поля структуры**:
  1. `timestamp` (`LocalDateTime`) — точное время возникновения ошибки.
  2. `status` (`int`) — числовой HTTP-код ошибки (например, `400`, `404`, `409`, `500`).
  3. `error` (`String`) — стандартное название статуса HTTP (например, `"Bad Request"`, `"Not Found"`).
  4. `message` (`String`) — понятный для человека текст ошибки.
  5. `path` (`String`) — относительный URI путь, на который пришел ошибочный запрос.
  6. `details` (`Map<String, Object>`) — дополнительный блок с детальной информацией (например, список конкретных полей, не прошедших валидацию). Если деталей нет, поле остается `null`.

### Пример единого формата ответа при ошибке валидации:
```json
{
  "timestamp": "2026-09-12T19:54:45.123",
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

### Пример единого формата ответа при 404 (объект не найден):
```json
{
  "timestamp": "2026-09-12T19:54:45.717",
  "status": 404,
  "error": "Not Found",
  "message": "User with id 999 was not found",
  "path": "/api/users/999",
  "details": null
}
```

---

## Требование 4. Настройка логирования через Logback

### Суть требования
1. Настроить уровни логирования для разных пакетов (своего кода, Spring, Hibernate).
2. Настроить ротацию логов: автоматическое создание новых файлов при превышении размера и удаление старых файлов по прошествии времени.

### Где и как реализовано в коде
- **Файл конфигурации**: [`src/main/resources/logback-spring.xml`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/resources/logback-spring.xml)

### Разбор конфигурации:
1. **Аппендеры (куда выводятся логи)**:
   - `<appender name="CONSOLE" ...>` — вывод форматированных логов в консоль приложения.
   - `<appender name="FILE" ...>` (`RollingFileAppender`) — вывод логов в рабочий файл `logs/recipe-platform.log`.
2. **Политика ротации логов (`rollingPolicy`)**:
   - Используется класс `SizeAndTimeBasedRollingPolicy` — ротация и по времени (каждый день новый файл), и по размеру.
   - `<fileNamePattern>logs/recipe-platform-%d{yyyy-MM-dd}.%i.log</fileNamePattern>` — архивные файлы сохраняются с датой и порядковым индексом `%i`.
   - `<maxFileSize>10MB</maxFileSize>` — если в течение одного дня файл вырастает больше 10 мегабайт, он архивируется и открывается файл с индексом `.1`, `.2` и т.д.
   - `<maxHistory>30</maxHistory>` — хранение архивных файлов не более 30 дней.
   - `<totalSizeCap>1GB</totalSizeCap>` — общий максимальный объем всех архивных логов на диске не превышает 1 гигабайт.
3. **Уровни логирования (`logger` и `root`)**:
   - `<logger name="com.example.recipeplatform" level="INFO"/>` — для кода нашего приложения установлен уровень `INFO` (выводятся сообщения уровней INFO, WARN, ERROR).
   - `<logger name="org.springframework.web" level="INFO"/>` — информационные логи веб-слоя Spring.
   - `<logger name="org.hibernate" level="WARN"/>` — заглушение избыточных логов Hibernate до уровня предупреждений `WARN`.
   - `<root level="INFO">` — глобальный уровень по умолчанию для всех остальных библиотек.

---

## Требование 5. Аспект (AOP) для логирования времени выполнения сервисных методов

### Суть требования
Нужно измерять время работы каждого метода сервисного слоя (в миллисекундах) и писать его в лог. При этом нельзя писать код замера вручную в каждом отдельном сервисе (чтобы не нарушать принцип DRY). Замер должен выполняться автоматически через АОП.

### Где и как реализовано в коде
- **Файл**: [`src/main/java/com/example/recipeplatform/aspect/LoggingAspect.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/aspect/LoggingAspect.java)

### Разбор логики работы аспекта:
1. Аннотации:
   - `@Aspect` — указывает Spring, что класс является аспектом.
   - `@Component` — регистрирует аспект как управляемый Spring-бин.
2. Срез точек соединения (Pointcut):
   - `@Around("execution(* com.example.recipeplatform.service.*.*(..))")`
   - Перехватывает вызов **любого** метода с **любыми** аргументами и **любым** типом возвращаемого значения во всех классах пакета `com.example.recipeplatform.service`.
3. Тип совета:
   - `@Around` (вокруг метода) — позволяет выполнить код до вызова целевого метода, запустить сам метод через `joinPoint.proceed()`, и выполнить код после завершения метода.
4. Измерение времени и обработка ошибок:
   - Перед вызовом фиксируется метка времени: `long start = System.nanoTime();`.
   - Метод вызывается внутри блока `try`: `Object result = joinPoint.proceed();`.
   - В блоке `finally` вычисляется затраченное время: `(System.nanoTime() - start) / 1_000_000` (перевод в миллисекунды).
   - Если метод отработал успешно: пишется лог уровня `INFO`:
     `Method RecipeService.findAll executed in 12 ms`.
   - Если метод выбросил исключение: флаг `success` остается `false`, и пишется предупреждение уровня `WARN`:
     `Method RecipeService.getById failed after 3 ms`, после чего исключение летит дальше в `GlobalExceptionHandler`.

---

## Требование 6. Подключение Swagger / OpenAPI

### Суть требования
API должно быть снабжено интерактивной документацией, где описаны все пути (эндпоинты), параметры, форматы входящих и исходящих данных, а также возможные ответы с кодами ошибок.

### Где и как реализовано в коде
1. **Зависимость в Maven**:
   - В [`pom.xml`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/pom.xml) подключена библиотека:
     `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17`.
2. **Конфигурационный класс**:
   - [`src/main/java/com/example/recipeplatform/config/OpenApiConfig.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/config/OpenApiConfig.java):
   - Аннотация `@OpenAPIDefinition` с метаданными API (название "Recipe Platform API", версия "4.0", описание, ссылка на сервер разработки `http://localhost:8080`).
3. **Аннотации документирования в контроллерах**:
   - `@Tag(name = "...", description = "...")` — группировка эндпоинтов по функциональным разделам (Users, Recipes, Categories, Ingredients, Cooking Steps).
   - `@Operation(summary = "...", description = "...")` — описание назначения конкретного эндпоинта.
   - `@ApiResponse` и `@ApiResponses` — описание кодов ответов (200, 201, 204, 400, 404, 409) и ссылки на схему `ApiError.class`.
   - `@Parameter(description = "...", example = "...")` — описание параметров URL (`@PathVariable`, `@RequestParam`).
4. **Аннотации документирования в DTO-моделях**:
   - `@Schema(description = "...", example = "...")` на классах и полях DTO.
   - `@ArraySchema` для коллекций идентификаторов.

### Адреса для доступа в браузере:
- **Интерактивный UI Swagger**: `http://localhost:8080/swagger-ui.html`
- **Спецификация OpenAPI в JSON**: `http://localhost:8080/v3/api-docs`

---

## Дополнительно реализованные улучшения в коде

1. **Изолированный набор тестов для Лабораторной 4**:
   - Добавлены быстрые тесты, которые проверяют валидацию, маппинг всех кодов ошибок и АОП без необходимости поднимать базу данных PostgreSQL:
     - [`src/test/java/com/example/recipeplatform/exception/GlobalExceptionHandlerTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/exception/GlobalExceptionHandlerTest.java) — проверяет все 6 обработчиков исключений и структуру `ApiError`.
     - [`src/test/java/com/example/recipeplatform/aspect/LoggingAspectTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/aspect/LoggingAspectTest.java) — проверяет вызов метода через `proceed()`, замер времени и логирование при ошибке.
     - [`src/test/java/com/example/recipeplatform/dto/DtoValidationTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/dto/DtoValidationTest.java) — проверяет срабатывание аннотаций валидации и каскадную проверку вложенных шагов.
2. **Единый ответ для ненайденных статических ресурсов**:
   - В `GlobalExceptionHandler.java` метод `handleNoResourceFound` возвращает стандартный JSON `ApiError` со статусом 404 вместо пустого тела ответа.

---

## Команды для проверки и запуска тестов

Для запуска всех тестов 4-й лабораторной работы без необходимости подключения к базе данных:

```powershell
cmd /c .\mvnw.cmd test -Dtest="GlobalExceptionHandlerTest,LoggingAspectTest,DtoValidationTest,RecipeServiceUnitTest"
```

Все 14 тестов проходят успешно (`BUILD SUCCESS`).
