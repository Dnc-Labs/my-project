# ===== STAGE 1: BUILD — Node + toàn bộ devDependencies, dùng xong vứt =====
FROM node:22-alpine AS build
WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ===== STAGE 2: RUNTIME — nginx phục vụ file tĩnh =====
FROM nginx:alpine
COPY --from=build /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
