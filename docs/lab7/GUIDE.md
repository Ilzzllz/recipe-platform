# Лабораторная работа №7: Руководство по коду (GUIDE)

В данном руководстве зафиксирована архитектура клиентского SPA-приложения (Single Page Application), его интеграция с REST API Spring Boot платформы `recipe-platform`, а также точное соответствие между требованиями лабораторной работы и их реализацией в кодовой базе.

---

## 1. Стек технологий клиента

- **Фреймворк**: React 18 (TypeScript)
- **Сборщик**: Vite 6
- **Стилизация**: Tailwind CSS (адаптивная верстка, мобильная и десктопная сетка)
- **Иконки**: Lucide React
- **Связь с бэкендом**: Native Fetch API с типизированной оберткой и перехватом ошибок `ApiError`
- **Расположение исходного кода клиента**: каталог `frontend/`
- **Взаимодействие с бэкендом**: Конфигурация CORS в Spring Boot (`WebConfig`) и проксирование запросов через `vite.config.ts`.

---

## 2. Связь требований и реализации в коде

| Требование ТЗ | Где реализовано (Класс / Компонент / Метод) | Назначение и детали реализации |
| :--- | :--- | :--- |
| **1. SPA-клиент (React)** | [`frontend/src/App.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/App.tsx)<br>[`frontend/src/main.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/main.tsx)<br>[`frontend/index.html`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/index.html) | Единая веб-страница без полной перезагрузки браузера. Состояние интерфейса управляется хуками `useState`, `useEffect`, `useCallback`. Плавное переключение табов (Каталог, Фильтрация, Многопоточность, Справочники). |
| **2. Работа с REST API бэкенда** | [`frontend/src/api/client.ts`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/api/client.ts)<br>[`WebConfig.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/config/WebConfig.java)<br>[`frontend/vite.config.ts`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/vite.config.ts) | Типизированный HTTP-клиент, инкапсулирующий вызовы ко всем эндпоинтам бэкенда (`/api/recipes`, `/api/categories`, `/api/ingredients`, `/api/users`). Бэкенд сконфигурирован с `WebMvcConfigurer.addCorsMappings()` для разрешения Cross-Origin запросов от Vite dev server (`http://localhost:5173`). |
| **3. Отображение OneToMany** | [`frontend/src/components/RecipeModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeModal.tsx#L96-L135)<br>[`frontend/src/components/RecipeCard.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeCard.tsx#L80-L92)<br>[`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx#L230-L280) | Связь **Рецепт ➔ Шаги приготовления** (`Recipe` ➔ `CookingStep`).<br>• В карточке рецепта выводится счетчик шагов.<br>• В модальном окне детализации выводится визуальный блок «Шаги приготовления (OneToMany)» с нумерацией шагов (`stepOrder`) и описанием действий.<br>• В форме создания/редактирования реализован динамический список с добавлением, удалением и перемещением шагов (Up/Down). |
| **4. Отображение ManyToMany** | [`frontend/src/components/RecipeModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeModal.tsx#L61-L94)<br>[`frontend/src/components/RecipeCard.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeCard.tsx#L50-L78)<br>[`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx#L198-L228) | Связь **Рецепты ⟷ Ингредиенты** (`Recipe` ⟷ `Ingredient` через связующую таблицу `recipe_ingredients`).<br>• В карточке рецепта выводятся теги используемых ингредиентов.<br>• В детальном окне выделен специальный блок с бейджами ингредиентов и их ID.<br>• В форме создания реализован интерактивный мультивыбор (chips/pills) из общего пула ингредиентов. |
| **5. CRUD: Create (Создание)** | [`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx)<br>[`RecipeController.create()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L129-L134) | Модальная форма с валидацией обязательных полей (название, описание, автор, категория, хотя бы 1 ингредиент и 1 шаг). Отправка запроса `POST /api/recipes` с DTO `RecipeCreateDto`. |
| **6. CRUD: Read (Чтение и поиск)** | [`frontend/src/App.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/App.tsx#L68-L96)<br>[`RecipeController.getAll()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L94-L98)<br>[`RecipeController.getByTitle()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L108-L113)<br>[`RecipeController.getById()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L100-L106) | Вывод сетки рецептов с мгновенной фильтрацией по категориям на клиенте и строкой поиска по названию (`GET /api/recipes/search?title=...`). При открытии карточки вызывается `GET /api/recipes/{id}`, что также безопасно инкрементирует счетчик просмотров на бэкенде. |
| **7. CRUD: Update (Обновление)** | [`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx)<br>[`RecipeController.update()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L136-L143) | Полная замена данных рецепта (`PUT /api/recipes/{id}`): обновление текстовых полей, набора ингредиентов и упорядоченного списка шагов. |
| **8. CRUD: Delete (Удаление)** | [`frontend/src/App.tsx#handleDeleteRecipe`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/App.tsx#L105-L115)<br>[`RecipeController.delete()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L145-L152) | Удаление рецепта с подтверждением (`DELETE /api/recipes/{id}`). Каскадное удаление шагов и очистка промежуточной таблицы связей в JPA. |
| **9. Фильтрация и пагинация (Lab 3)** | [`frontend/src/components/FilterSection.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/FilterSection.tsx)<br>[`RecipeController.filterByAuthorAndCategoryJPQL()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L154-L164)<br>[`RecipeController.filterByAuthorAndCategoryNative()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L166-L176) | Полнофункциональный интерфейс поиска по автору и категории. Поддерживает переключение между запросом JPQL и Native SQL. Отображает серверную пагинацию (страницы, количество найденных записей, размер страницы) на основе объекта `Pageable`. |
| **10. Интеграция с многопоточностью (Lab 6)** | [`frontend/src/components/ConcurrencySection.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/ConcurrencySection.tsx)<br>[`frontend/src/components/RecipeModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeModal.tsx#L137-L210) | • Интерактивный монитор счетчиков просмотров (`AtomicLong`, `synchronized`, незащищенный `int`).<br>• Запуск симуляции Race Condition на 50+ параллельных потоках прямо из браузера с визуализацией `Lost Updates`.<br>• Запуск асинхронного расчета КБЖУ с периодическим опросом статуса задачи (polling) и отображением данных из Open Food Facts. |

---

## 3. Структура каталогов фронтенда

```text
frontend/
├── index.html                     # Главная страница HTML с мета-тегами и контейнером #root
├── package.json                   # Зависимости (React, Vite, Tailwind CSS, Lucide)
├── tsconfig.json                  # Конфигурация компилятора TypeScript
├── vite.config.ts                 # Конфигурация Vite и проксирования запросов
├── tailwind.config.js             # Настройки цветовой палитры и тем оформления
├── postcss.config.js              # Плагины PostCSS (Tailwind, Autoprefixer)
└── src/
    ├── main.tsx                   # Точка входа React (ReactDOM.createRoot)
    ├── App.tsx                    # Главный компонент приложения (управление состоянием и табами)
    ├── index.css                  # Подключение Tailwind (@tailwind directives)
    ├── api/
    │   └── client.ts              # Типизированный клиент вызовов REST API
    ├── types/
    │   └── index.ts               # DTO-интерфейсы моделей и ответов
    └── components/
        ├── Navbar.tsx             # Шапка с табами и кнопкой обновления
        ├── RecipeCard.tsx         # Карточка рецепта в каталоге
        ├── RecipeModal.tsx        # Модалка подробностей рецепта (OneToMany, ManyToMany, КБЖУ)
        ├── RecipeFormModal.tsx    # Форма добавления/редактирования рецепта
        ├── FilterSection.tsx      # Раздел фильтрации JPQL / Native SQL с пагинацией
        ├── ConcurrencySection.tsx # Раздел тестирования Race Condition и счетчиков
        └── CategoryIngredientManager.tsx # Управление справочниками категорий и ингредиентов
```

---

## 4. Инструкция по запуску клиента

1. Запустить Spring Boot бэкенд на порту 8080 (запуск класса `RecipePlatformApplication` в IntelliJ IDEA или `.\mvnw spring-boot:run`).
2. В терминале перейти в каталог `frontend`:
   ```bash
   cd frontend
   npm run dev
   ```
3. Открыть браузер по адресу `http://localhost:5173`.
   Запросы к `/api/*` будут автоматически перенаправляться на Spring Boot благодаря настроенному прокси в `vite.config.ts` и CORS на бэкенде.
