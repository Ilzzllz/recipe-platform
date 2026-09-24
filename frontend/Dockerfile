# syntax=docker/dockerfile:1.7
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --ignore-scripts --no-audit --no-fund

COPY . .

ARG VITE_API_URL=""
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

FROM nginx:1.27-alpine AS runtime

ENV BACKEND_URL=http://backend:8080 \
    PORT=80
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
    CMD wget -qO- "http://127.0.0.1:${PORT}/" >/dev/null || exit 1
