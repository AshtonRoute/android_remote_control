# syntax = docker/dockerfile:experimental
FROM gradle:5.6.0-jdk8

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
