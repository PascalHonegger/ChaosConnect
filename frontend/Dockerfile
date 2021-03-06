FROM node:15-alpine as build
WORKDIR /app
COPY package*.json /app/
RUN npm ci
COPY . /app
RUN npm run build

FROM nginx:mainline-alpine
COPY --from=build /app/public /usr/share/nginx/html
