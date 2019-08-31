# syntax = docker/dockerfile:experimental
FROM node:12.8.1-alpine

RUN \
  --mount=type=cache,target=/var/cache/apk \
  --mount=type=cache,target=/var/lib/apk \
  --mount=type=cache,target=/etc/apk/cache \
  apk --update add \
  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/testing \
  android-tools

WORKDIR /app

RUN yarn config set save-exact
RUN --mount=type=cache,target=/usr/local/share/.cache/yarn \
  yarn global add npm-check-updates

COPY package.json yarn.lock /app/
RUN --mount=type=cache,target=/usr/local/share/.cache/yarn \
  yarn install

COPY babel.config.js /app/
COPY src /app/src

RUN npm run build

CMD ["node", "/app/dist"]
