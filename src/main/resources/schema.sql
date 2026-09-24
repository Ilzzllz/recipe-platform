CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    bio      TEXT,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    CONSTRAINT uk_categories_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS ingredients (
    id                     BIGSERIAL PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL,
    calories_per100g       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    proteins_per100g       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    fats_per100g           NUMERIC(10, 2) NOT NULL DEFAULT 0,
    carbohydrates_per100g  NUMERIC(10, 2) NOT NULL DEFAULT 0,
    grams_per_unit         NUMERIC(10, 2) NOT NULL DEFAULT 1,
    CONSTRAINT uk_ingredients_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS recipes (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    portions    INTEGER NOT NULL DEFAULT 1,
    author_id   BIGINT NOT NULL REFERENCES users(id),
    category_id BIGINT NOT NULL REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS cooking_steps (
    id          BIGSERIAL PRIMARY KEY,
    step_order  INTEGER NOT NULL,
    description TEXT NOT NULL,
    recipe_id   BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recipe_ingredients (
    recipe_id     BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
    PRIMARY KEY (recipe_id, ingredient_id)
);

CREATE TABLE IF NOT EXISTS recipe_ingredient_details (
    id            BIGSERIAL PRIMARY KEY,
    recipe_id     BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
    quantity      NUMERIC(10, 3) NOT NULL,
    unit          VARCHAR(16) NOT NULL,
    CONSTRAINT uk_recipe_ingredient_details UNIQUE (recipe_id, ingredient_id)
);

CREATE INDEX IF NOT EXISTS idx_recipes_author_id ON recipes(author_id);
CREATE INDEX IF NOT EXISTS idx_recipes_category_id ON recipes(category_id);
CREATE INDEX IF NOT EXISTS idx_cooking_steps_recipe_id ON cooking_steps(recipe_id);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredient_details_recipe_id ON recipe_ingredient_details(recipe_id);
