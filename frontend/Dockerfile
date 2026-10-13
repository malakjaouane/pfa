# ==================================================================
# Image du front React : build Vite puis service nginx statique.
# Le build context DOIT être la racine du dépôt (cf. docker-compose.yml).
# ==================================================================

# ---- Étape 1 : build Vite --------------------------------------
FROM node:20-alpine AS construction
WORKDIR /app

COPY frontend/package.json frontend/package-lock.json ./
# npm ci (et non npm install) : réinstallation fidèle au lockfile,
# donc build reproductible.
RUN npm ci

COPY frontend ./
RUN npm run build

# ---- Étape 2 : nginx statique ----------------------------------
FROM nginx:1.27-alpine

RUN rm -rf /usr/share/nginx/html/*
COPY --from=construction /app/dist /usr/share/nginx/html
# Config maison : proxy /api/ vers le backend + routage SPA.
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
