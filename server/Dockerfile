# syntax = docker/dockerfile:experimental
FROM gradle:5.6.0-jdk8 AS builder

RUN --mount=type=cache,target=/var/cache/apt --mount=type=cache,target=/var/lib/apt --mount=type=cache,target=/etc/apt/cache \
  apt update && apt install -y \
    adb

# download and install Android SDK
# https://developer.android.com/studio/#downloads
ARG ANDROID_SDK_VERSION=4333796
ENV ANDROID_HOME /opt/android-sdk
RUN mkdir -p ${ANDROID_HOME} && cd ${ANDROID_HOME} && \
    curl -LO https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_VERSION}.zip && \
    unzip *tools*linux*.zip && \
    rm *tools*linux*.zip \
    && yes | ./tools/bin/sdkmanager --licenses

WORKDIR /

# hack for gradle single project
RUN echo "rootProject.name = 'root'\ninclude ':app'" > settings.gradle
COPY root.gradle /build.gradle
COPY dependencies.gradle /

WORKDIR /app

COPY build.gradle proguard-rules.pro /app/
COPY src /app/src/main

RUN --mount=type=cache,target=/home/gradle/.gradle/caches --mount=type=cache,target=/app/build \
 gradle --no-daemon --warning-mode all assembleRelease \
 && mkdir /app/dist \
 && cp "/app/build/outputs/apk/release/app-release-unsigned.apk" "/app/dist/remote_control_server.jar"

#  gradle --no-daemon --warning-mode all assembleDebug \
#  && mkdir /app/dist \
#  && cp "/app/build/outputs/apk/debug/app-debug.apk" "/app/dist/remote_control_server.jar"

### runner
FROM alpine:edge AS runner

# needed for android-tools
RUN echo http://dl-cdn.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories

RUN --mount=type=cache,target=/var/cache/apk --mount=type=cache,target=/var/lib/apk --mount=type=cache,target=/etc/apk/cache \
  apk add \
    android-tools \
    ffmpeg \
    virtualgl

COPY --from=builder /app/dist/server/scrcpy-server.jar /usr/local/share/scrcpy/
COPY --from=builder /app/dist/app/scrcpy /usr/local/bin/

RUN --mount=type=cache,target=/var/cache/apk --mount=type=cache,target=/var/lib/apk --mount=type=cache,target=/etc/apk/cache \
  apk add \
    mesa-dri-nouveau

### runner (amd)
#FROM runner AS amd

#RUN apk add --no-cache mesa-dri-swrast

### runner (intel)
#FROM runner AS intel

#RUN apk add --no-cache mesa-dri-intel

### runner (nvidia)
#FROM runner AS nvidia

#RUN apk add --no-cache mesa-dri-nouveau
