# Лабораторная работа №7: Руководство по коду (GUIDE)

В данном руководстве зафиксирована архитектура клиентского SPA-приложения (Single Page Application), его интеграция с REST API Spring Boot платформы `recipe-platform`, а также точное соответствие между требованиями лабораторной работы и их реализацией в кодовой базе.

---

## 1. Стек технологий клиента

- **Фреймворк**: React 18 (TypeScript)
- **Сборщик**: Vite 6
- **Стилизация**: Tailwind CSS (адаптивная мобильная и десктопная верстка, современные скругления, тени)
- **Иконки**: Lucide React
- **Связь с бэкендом**: Native Fetch API с типизированной оберткой и перехватом ошибок `ApiError`
- **Система уведомлений**: всплывающие Toast-уведомления (`ToastContainer`) и диалоговые окна подтверждения (`ConfirmModal`)
- **Расположение исходного кода клиента**: каталог `frontend/`
- **Взаимодействие с бэкендом**: Конфигурация CORS в Spring Boot (`WebConfig`) и проксирование запросов через `vite.config.ts`.

---

## 2. Связь требований и реализации в коде

| Требование ТЗ | Где реализовано (Класс / Компонент / Метод) | Назначение и детали реализации |
| :--- | :--- | :--- |
| **1. SPA-клиент (React)** | [`frontend/src/App.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/App.tsx)<br>[`frontend/src/main.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/main.tsx)<br>[`frontend/index.html`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/index.html) | Единая веб-страница без полной перезагрузки браузера. Состояние интерфейса управляется хуками `useState`, `useEffect`, `useCallback`. Навигация между каталогом рецептов и справочниками. |
| **2. Работа с REST API бэкенда** | [`frontend/src/api/client.ts`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/api/client.ts)<br>[`WebConfig.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/config/WebConfig.java)<br>[`frontend/vite.config.ts`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/vite.config.ts) | Типизированный HTTP-клиент, инкапсулирующий вызовы ко всем эндпоинтам бэкенда (`/api/recipes`, `/api/categories`, `/api/ingredients`, `/api/users`). Бэкенд сконфигурирован с `WebMvcConfigurer.addCorsMappings()` для разрешения Cross-Origin запросов от Vite dev server (`http://localhost:5173`). |
| **3. Отображение OneToMany** | [`frontend/src/components/RecipeModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeModal.tsx)<br>[`frontend/src/components/RecipeCard.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeCard.tsx)<br>[`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx) | Связь **Рецепт ➔ Шаги приготовления** (`Recipe` ➔ `CookingStep`).<br>• В карточке рецепта выводится счетчик шагов.<br>• В модальном окне выводится пошаговый процесс с возможностью отмечать выполненные шаги в реальном времени (интерактивный режим готовки).<br>• В форме создания/редактирования реализован динамический список с добавлением, удалением и перемещением шагов (Up/Down). |
| **4. Отображение ManyToMany** | [`frontend/src/components/RecipeModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeModal.tsx)<br>[`frontend/src/components/RecipeCard.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeCard.tsx)<br>[`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx) | Связь **Рецепты ⟷ Ингредиенты** (`Recipe` ⟷ `Ingredient` через связующую таблицу `recipe_ingredients`).<br>• В карточке рецепта выводятся теги используемых ингредиентов.<br>• В детальном окне выделен специальный блок со списком ингредиентов.<br>• В форме создания реализован интерактивный мультивыбор (chips) из общего каталога ингредиентов. |
| **5. CRUD: Create (Создание)** | [`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx)<br>[`RecipeController.create()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L129-L134) | Модальная форма с инлайн-валидацией каждого поля (подсветка ошибок прямо под полями ввода), автор, категория, ингредиенты и шаги. Отправка запроса `POST /api/recipes`. |
| **6. CRUD: Read (Чтение и поиск)** | [`frontend/src/App.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/App.tsx)<br>[`RecipeController.getAll()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L94-L98)<br>[`RecipeController.getByTitle()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L108-L113)<br>[`RecipeController.getById()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L100-L106) | Вывод сетки рецептов с фильтрацией по категориям на клиенте и строкой поиска по названию (`GET /api/recipes/search?title=...`). При открытии карточки вызывается `GET /api/recipes/{id}`. |
| **7. CRUD: Update (Обновление)** | [`frontend/src/components/RecipeFormModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeFormModal.tsx)<br>[`RecipeController.update()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L136-L143) | Редактирование существующего рецепта (`PUT /api/recipes/{id}`): обновление названия, описания, ингредиентов и упорядоченного списка шагов. |
| **8. CRUD: Delete (Удаление)** | [`frontend/src/components/ConfirmModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/ConfirmModal.tsx)<br>[`RecipeController.delete()`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/controller/RecipeController.java#L145-L152) | Удаление рецепта через подтверждение в стилизованном модальном окне (`DELETE /api/recipes/{id}`). Каскадное удаление шагов и очистка промежуточной таблицы связей в JPA. |
| **9. Асинхронный расчет КБЖУ** | [`frontend/src/components/RecipeModal.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/RecipeModal.tsx)<br>[`NutritionReportService.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/service/NutritionReportService.java) | Запуск асинхронного расчета нутриентов (`POST /api/recipes/{id}/nutrition-report`), периодический опрос статуса (`GET /api/recipes/nutrition-report/{taskId}`), карточки макронутриентов (калории, белки, жиры, углеводы) и таблица ингредиентов с интеграцией Open Food Facts. |
| **10. Обработка ошибок и Toast-уведомления** | [`frontend/src/components/ToastContainer.tsx`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/components/ToastContainer.tsx)<br>[`frontend/src/api/client.ts`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/frontend/src/api/client.ts) | Любая ошибка сети, сервера или валидации перехватывается в `ApiError` и выводится пользователю в виде всплывающего Toast-уведомления с цветовой дифференциацией (зеленый для успеха, красный для ошибок, синий для информации). |

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
    ├── App.tsx                    # Главный компонент приложения (управление состоянием, табами, тостами)
    ├── index.css                  # Подключение Tailwind (@tailwind directives)
    ├── api/
    │   └── client.ts              # Типизированный клиент вызовов REST API с парсингом ошибок
    ├── types/
    │   └── index.ts               # DTO-интерфейсы моделей, ответов и Toast
    └── components/
        ├── Navbar.tsx             # Шапка приложения с вкладками («Рецепты», «Справочники») и быстрыми действиями
        ├── RecipeCard.tsx         # Карточка рецепта в каталоге (ингредиенты, шаги, кнопки действий)
        ├── RecipeModal.tsx        # Модалка подробностей рецепта (интерактивные шаги готовки, КБЖУ)
        ├── RecipeFormModal.tsx    # Форма добавления/редактирования рецепта с валидацией
        ├── CategoryIngredientManager.tsx # Управление категориями, ингредиентами и авторами
        ├── ConfirmModal.tsx       # Универсальный диалог подтверждения удаления без браузерного confirm
        └── ToastContainer.tsx     # Контейнер всплывающих уведомлений (success, error, info)
```

---

## 4. Инструкция по запуску клиента

1. Запустить Spring Boot бэкенд на порту 8080 (запуск класса `RecipePlatformApplication` в IDE или `.\mvnw spring-boot:run`).
2. В терминале перейти в каталог `frontend`:
   ```bash
   cd frontend
   npm run dev
   ```
3. Открыть браузер по адресу `http://localhost:5173`.
   Запросы к `/api/*` автоматически перенаправляются на Spring Boot благодаря настроенному прокси в `vite.config.ts` и CORS на бэкенде.

---

## 5. Что такое «Кулинарная оценка» в результатах расчета КБЖУ?

В результатах расчета пищевой ценности в таблице ингредиентов отображаются два возможных источника данных:
1. 🟢 **Open Food Facts API** — точные данные, успешно полученные по сети из открытой всемирной базы пищевых продуктов (Open Food Facts).
2. 🟡 **Кулинарная оценка (Standard culinary estimate)** — стандартный базовый расчет пищевой ценности.

### Почему это сделано и в каких случаях применяется?
В реальной жизни пользователь может ввести ингредиент с опечаткой (например, *"морквь"* вместо *"морковь"*), указать специфическое название или у сервера может временно пропасть связь с внешним API Open Food Facts.

Если бы приложение полагалось исключительно на внешний API, при любой опечатке весь расчет рецепта падал бы с ошибкой `500 Internal Server Error` и пользователь не получал бы никакого результата.

Чтобы этого избежать, в сервисе [`NutritionReportService.java`](file:///C:/Users/Formatis/Documents/GitHub/recipe-platform/src/main/java/com/example/recipeplatform/service/NutritionReportService.java) реализован паттерн **Graceful Degradation (Мягкая деградация / Fallback)**:
- Если продукт найден в Open Food Facts — берутся точные нутриенты из базы.
- Если продукт не найден в базе или произошел сетевой таймаут — сервер не падает, а подставляет среднюю кулинарную оценку для растительно-овощных продуктов (45 ккал, 1.5 г белка, 0.5 г жира, 9 г углеводов на порцию) и честно помечает источник как *"Кулинарная оценка"*.

**Результат**: расчет всегда успешно завершается со статусом `COMPLETED`, интерфейс не ломается, а пользователь наглядно видит, какие ингредиенты были распознаны в мировой базе, а для каких применилась усредненная оценка.

