# Stage 1: Build Environment
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Install dependencies for Android SDK
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV ANDROID_HOME /opt/android-sdk
ENV ANDROID_SDK_ROOT ${ANDROID_HOME}
ENV PATH ${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Download and install Android Command Line Tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

# Accept Android SDK licenses
RUN yes | sdkmanager --licenses

# Install required Android SDK components
RUN sdkmanager "platform-tools" "platforms;android-37.0" "build-tools;37.0.0"

# Copy root project files first for better caching
COPY gradlew .
COPY gradle gradle
COPY *.gradle.kts .
COPY *.properties .

# Fix line endings for gradlew (crucial if building from Windows)
RUN dos2unix gradlew && chmod +x gradlew

# Copy app module
COPY app app

# Increase Gradle memory for the build
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m"

# Build the APK with stacktrace for better error reporting
RUN ./gradlew :app:assembleDebug --no-daemon --stacktrace

# Create a volume to export the APK
CMD ["sh", "-c", "mkdir -p /output && cp app/build/outputs/apk/debug/app-debug.apk /output/"]
