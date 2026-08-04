# ============================================================================
# React multi-stage Dockerfile (node → nginx)
# TICKET-ADV147
# ============================================================================

# Stage 1: Build with node:22-alpine
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install --no-audit --no-fund
COPY . .
RUN npm run build

# Stage 2: Runtime with nginx:1.27-alpine
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80