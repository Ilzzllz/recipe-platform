# syntax=docker/dockerfile:1.7
# ============================================================
#  Frontend: React + Vite build (Node 20) -> Nginx static server
# ============================================================

# ---------- Stage 1: build ----------
FROM node:20-alpine AS build
WORKDIR /app

# Зависимости отдельным слоем: npm ci повторяется только при смене lock-файла
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund

COPY . .

# URL бэкенда вшивается в бандл на этапе сборки. Пусто -> запросы идут на
# относительный /api, который проксирует Nginx (режим docker-compose).
ARG VITE_API_URL=""
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# ---------- Stage 2: runtime ----------
FROM nginx:1.27-alpine AS runtime

# Шаблон обрабатывается envsubst при старте контейнера (официальный entrypoint nginx)
ENV BACKEND_URL=http://backend:8080 \
    PORT=80
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
    CMD wget -qO- "http://127.0.0.1:${PORT}/" >/dev/null || exit 1
