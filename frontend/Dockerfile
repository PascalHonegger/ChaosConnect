FROM node:15-alpine as build
WORKDIR /app
COPY dummy.html ./dist/out/index.html
# TODO COPY package*.json /app/
# TODO RUN npm ci
# TODO COPY . /app
# TODO RUN npm run build -- --outputPath=./dist/out --configuration production

FROM nginx:mainline-alpine
COPY --from=build /app/dist/out/ /usr/share/nginx/html
