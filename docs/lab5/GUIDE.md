# Гайдлайн по реализации Лабораторной работы №5

Данный документ описывает реализацию всех требований Лабораторной работы №5 (**Batch data processing & Testing**) в проекте **Recipe Platform**.

Документ составлен простым языком, понятным начинающему разработчику, со ссылками на конкретные классы, методы и строки кода.

---

## Краткое описание темы лабораторной работы

Лабораторная работа №5 посвящена двум фундаментальным темам:
1. **Пакетная обработка данных (Bulk / Batch operations)**: умение сервера принимать и сохранять не по одной записи, а целыми пачками (списками) за один запрос, используя возможности **Stream API**, безопасную работу со ссылками через **Optional** и управление транзакциями (**`@Transactional`**).
2. **Модульное тестирование (Unit Testing)**: написание изолированных тестов с подменой зависимостей (библиотека **Mockito**), обеспечивающих 100% покрытие кода сервисного слоя в анализаторе IntelliJ IDEA.

---

## Требование 1. Bulk-операция со списком объектов, имеющая бизнес-смысл

### Суть требования
В реальных системах пользователи и внешние сервисы часто импортируют данные большими пачками (например, выгрузка каталога или кулинарной книги). Делать отдельный HTTP-запрос на каждый рецепт слишком медленно. Нужна пакетная операция (bulk operation), принимающая JSON-массив объектов.

### Где и как реализовано в коде
- **Контроллер**: [`src/main/java/com/example/recipeplatform/controller/RecipeController.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java)
- **Эндпоинты**:
  - `POST /api/recipes/bulk` — атомарный пакетный импорт с единой транзакцией.
  - `POST /api/recipes/bulk/no-tx` — поштучный импорт без общей транзакции (для сравнения и демонстрации).
- **Бизнес-смысл**:
  Клиент отправляет список рецептов (`List<RecipeCreateDto>`). Сервер за одну операцию валидирует рецепты, привязывает их к существующим авторам и категориям, связывает с ингредиентами и сохраняет все шаги приготовления.
- **Валидация входящего списка**:
  ```java
  @PostMapping("/bulk")
  @ResponseStatus(HttpStatus.CREATED)
  public List<RecipeDto> createBulk(
          @RequestBody @NotEmpty(message = "Recipe list must not be empty")
          List<@Valid RecipeCreateDto> dtos)
  ```
  - `@NotEmpty` гарантирует, что клиент не прислал пустой массив `[]`.
  - `@Valid` перед типом элемента списка проверяет каждое поле внутри каждого присланного рецепта.

---

## Требование 2. Использование Stream API и Optional в сервисном слое

### Суть требования
Обработка списков данных должна быть написана в современном функциональном стиле Java с использованием цепочек методов **Stream API** и безопасных контейнеров **Optional**.

### Где и как реализовано в коде
- **Файл**: [`src/main/java/com/example/recipeplatform/service/RecipeService.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/service/RecipeService.java)

#### 1. Использование Stream API:
В методе `saveBulkRecipes`:
```java
private List<RecipeDto> saveBulkRecipes(List<RecipeCreateDto> dtos) {
    return dtos.stream()
            .map(this::convertToRecipe)          // 1. Преобразуем каждый DTO в сущность Recipe
            .map(recipeRepository::saveAndFlush) // 2. Сохраняем каждую сущность в БД
            .map(recipeMapper::toDto)            // 3. Преобразуем сохраненную сущность в ответный DTO
            .toList();                           // 4. Собираем итоговый список
}
```
А также при сборке шагов приготовления (`convertToRecipe` и `mapSteps`):
```java
List<CookingStep> steps = dto.getSteps().stream()
        .map(stepDto -> {
            CookingStep step = new CookingStep();
            step.setStepOrder(stepDto.getStepOrder());
            step.setDescription(stepDto.getDescription());
            return step;
        })
        .collect(Collectors.toList());
```

#### 2. Использование Optional:
В методе `convertToRecipe` поиск связанных сущностей возвращает `Optional<T>`, из которого сущность извлекается через безопасный метод `.orElseThrow(...)`:
```java
User author = userRepository.findById(dto.getAuthorId())
        .orElseThrow(() -> new NotFoundException("User with id " + dto.getAuthorId() + " was not found"));

Category category = categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Category with id " + dto.getCategoryId() + " was not found"));

Set<Ingredient> ingredients = dto.getIngredientIds().stream()
        .map(id -> ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient with id " + id + " was not found")))
        .collect(Collectors.toSet());
```
Если сущности с таким идентификатором нет, выбрасывается понятное исключение `NotFoundException`, прерывающее цепочку.

---

## Требование 3. Транзакционность bulk-операции и демонстрация разницы в БД

### Суть требования
Показать, как аннотация `@Transactional` защищает базу данных от "мусорных" и неполных данных:
- **С `@Transactional`**: если в пачке из 10 рецептов 9 правильных, а 10-й сломанный — отменяется (rollback) сохранение **всех** рецептов. База данных остается в исходном чистом состоянии.
- **Без `@Transactional`**: если в методе нет общей транзакции, то первые 9 рецептов уже зафиксируются в базе данных, а ошибка на 10-м оставит базу в наполовину сохраненном состоянии (частичный коммит).

### Где и как реализовано в коде
В [`RecipeService.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/service/RecipeService.java):
```java
@Transactional
public List<RecipeDto> createBulk(List<RecipeCreateDto> dtos) {
    try {
        return saveBulkRecipes(dtos);
    } finally {
        recipeQueryCacheService.invalidateAll();
    }
}

public List<RecipeDto> createBulkWithoutTransaction(List<RecipeCreateDto> dtos) {
    try {
        return saveBulkRecipes(dtos);
    } finally {
        recipeQueryCacheService.invalidateAll();
    }
}
```
*Почему без аннотации происходит частичное сохранение?*
Каждый вызов `recipeRepository.saveAndFlush(...)` сам по себе выполняется в маленькой отдельной транзакции Spring Data JPA. Без внешней транзакции на уровне сервиса каждый успешный `saveAndFlush` тут же фиксируется (коммитится) в БД.

---

### Пошаговая демонстрация для преподавателя через Swagger UI

Для демонстрации не нужно открывать консоль базы данных — достаточно встроенных ручек нашего API в Swagger UI: `http://localhost:8080/swagger-ui.html`.

#### Шаг 1: Смотрим исходное состояние
1. В Swagger открываем раздел **Recipes** -> `GET /api/recipes` -> нажимаем **Try it out** -> **Execute**.
2. Обращаем внимание на список существующих рецептов (или запоминаем их количество).

#### Шаг 2: Тестируем атомарный эндпоинт `POST /api/recipes/bulk` (с транзакцией)
1. Открываем `POST /api/recipes/bulk`.
2. Вставляем тело запроса, где первый рецепт валидный, а у второго указан несуществующий ингредиент `999999`:
```json
[
  {
    "title": "СУП 1 ВАЛИДНЫЙ (TX ТЕСТ)",
    "description": "Первый правильный рецепт",
    "authorId": 1,
    "categoryId": 1,
    "ingredientIds": [1],
    "steps": [{ "stepOrder": 1, "description": "Сварить суп" }]
  },
  {
    "title": "СУП 2 СЛОМАННЫЙ",
    "description": "Второй рецепт намеренно с ошибкой",
    "authorId": 1,
    "categoryId": 1,
    "ingredientIds": [999999],
    "steps": [{ "stepOrder": 1, "description": "Этот шаг не сохранится" }]
  }
]
```
3. Нажимаем **Execute**.
4. Сервер возвращает статус `404 Not Found` с текстом `"Ingredient with id 999999 was not found"`.
5. Снова нажимаем **Execute** в `GET /api/recipes`.
6. **Результат**: Рецепта `"СУП 1 ВАЛИДНЫЙ (TX ТЕСТ)"` в списке **НЕТ**. Транзакция полностью откатила операцию!

#### Шаг 3: Тестируем неатомарный эндпоинт `POST /api/recipes/bulk/no-tx` (без транзакции)
1. Открываем `POST /api/recipes/bulk/no-tx`.
2. Вставляем точно такое же тело запроса (можно изменить заголовок на `"СУП 1 ВАЛИДНЫЙ (БЕЗ ТРАНЗАКЦИИ)"`):
```json
[
  {
    "title": "СУП 1 ВАЛИДНЫЙ (БЕЗ ТРАНЗАКЦИИ)",
    "description": "Первый правильный рецепт",
    "authorId": 1,
    "categoryId": 1,
    "ingredientIds": [1],
    "steps": [{ "stepOrder": 1, "description": "Сварить суп" }]
  },
  {
    "title": "СУП 2 СЛОМАННЫЙ",
    "description": "Второй рецепт намеренно с ошибкой",
    "authorId": 1,
    "categoryId": 1,
    "ingredientIds": [999999],
    "steps": [{ "stepOrder": 1, "description": "Этот шаг не сохранится" }]
  }
]
```
3. Нажимаем **Execute**.
4. Сервер точно так же возвращает `404 Not Found` на втором объекте.
5. Снова нажимаем **Execute** в `GET /api/recipes`.
6. **Результат**: Рецепт `"СУП 1 ВАЛИДНЫЙ (БЕЗ ТРАНЗАКЦИИ)"` **ПОЯВИЛСЯ В СПИСКЕ**! Без `@Transactional` первый объект успел зафиксироваться в базе до того, как второй упал с ошибкой. Разница доказана наглядно.

---

## Требование 4. Unit-тесты для сервисов (Mockito) со 100% покрытием

### Суть требования
Написать модульные тесты для сервисного слоя с использованием библиотеки **Mockito**. Сервисы должны тестироваться в полной изоляции: без поднятия Spring-контекста и без реальной базы данных. Все репозитории и мапперы подменяются моками (`@Mock`).

В анализаторе покрытия кода IntelliJ IDEA для пакета `com.example.recipeplatform.service` должно отображаться **100% покрытия строк и методов**.

### Тестовые классы в проекте:
Все тесты расположены в [`src/test/java/com/example/recipeplatform/service/`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/):

1. [`RecipeServiceUnitTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/RecipeServiceUnitTest.java) — **20 тестов**:
   - `findAll` и `getById` (успех и выброс `NotFoundException`);
   - `searchByTitle`;
   - `create` и `update` со всеми ветками валидации автора, категории, списка ингредиентов;
   - `delete` (успех и `NotFoundException`);
   - демонстрация N+1 (`demonstrateNPlusOneProblem`, `demonstrateNPlusOneSolution`);
   - чтение фильтрации через JPQL и Native (с проверкой попадания в кеш и промаха мимо кеша);
   - `createBulk` (успех, откат транзакции, падение на несуществующем авторе/ингредиенте).
2. [`UserServiceTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/UserServiceTest.java) — **12 тестов**:
   - `findAll`, `getById` (успех, 404);
   - `create` (успех, конфликт логина, конфликт email);
   - `update` (успех, сохранение собственных данных, конфликт с чужим логином/email);
   - `delete` (успех пустого пользователя, запрет удаления пользователя с рецептами).
3. [`CategoryServiceTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/CategoryServiceTest.java) — **10 тестов**:
   - `findAll`, `getById` (успех, 404);
   - `create` (успех, проверка уникальности имени);
   - `update` (успех, конфликт имени);
   - `delete` (успех, запрет удаления непустой категории).
4. [`CookingStepServiceTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/CookingStepServiceTest.java) — **10 тестов**:
   - `findAll`, `findById` (успех, 404);
   - `create` (успех, ошибка если `recipeId == null`, ошибка если рецепт не найден);
   - `update` (успех, шаг не найден, рецепт не найден);
   - `delete` (успех, шаг не найден).
5. [`IngredientServiceTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/IngredientServiceTest.java) — **9 тестов**:
   - `findAll`, `getById` (успех, 404);
   - `create`, `update` (проверка дубликатов имени);
   - `delete` (отвязка ингредиента от всех связанных рецептов и удаление).
6. [`RecipeTransactionScenarioServiceTest.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/test/java/com/example/recipeplatform/service/RecipeTransactionScenarioServiceTest.java) — **4 теста**:
   - `saveWithoutTransactional` и `saveWithTransactional` (проверка вызовов репозиториев и обработки пустых опциональных полей bio/description).

Всего: **65 изолированных unit-тестов** на Mockito.

---

## Команда для запуска всех тестов сервисов

```powershell
cmd /c .\mvnw.cmd test -Dtest="*Service*Test"
```

Результат:
```
[INFO] Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
Время выполнения: менее 7 секунд.
В IntelliJ IDEA при запуске пакета `com.example.recipeplatform.service` с покрытием (Run with Coverage) достигается **100% Class, Method и Line Coverage**.

---

## Справочно: Автоматическое заполнение базы данными (Сидирование)

### Что это такое
Чтобы при запуске приложения база данных не была пустой, в проекте реализован сидер начальных данных: [`DataInitializer.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/config/DataInitializer.java).

### Как пользоваться
В файле [`src/main/resources/application.properties`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/resources/application.properties) есть настройка:
```properties
app.seed.enabled=true
```
- **Когда `true` (по умолчанию)**: При первом запуске бэкенда в базу автоматически добавляются тестовые пользователи (`anna`, `nikita`), категории (`Soups`, `Desserts`), ингредиенты и готовые рецепты (Борщ, Тыквенный суп).
- **Если преподаватель просит показать запуск с пустой базой**:
  1. Измените значение на `app.seed.enabled=false`.
  2. Перезапустите приложение — база будет абсолютно чистой.

### Что ответить преподавателю, если спросит про сидирование:
> *«В классе `DataInitializer` реализован стандартный бин Spring `CommandLineRunner`. Он срабатывает один раз при старте контекста приложения и, если параметр `app.seed.enabled` равен `true`, сохраняет стартовый набор сущностей через репозитории в одной транзакции»*.
