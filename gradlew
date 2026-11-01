#!/bin/sh
# ArmorStandEditor wrapper launcher. Uses the official Gradle 9.8.2 wrapper JAR.
# When checking out this archive, obtain the published JAR and verify its hash.
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL='https://raw.githubusercontent.com/gradle/gradle/v9.8.2/gradle/wrapper/gradle-wrapper.jar'
WRAPPER_SHA256='3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98'

verify_wrapper() {
    if command -v sha256sum >/dev/null 2>&1; then
        actual=$(sha256sum "$WRAPPER_JAR" | cut -d ' ' -f 1)
    elif command -v shasum >/dev/null 2>&1; then
        actual=$(shasum -a 256 "$WRAPPER_JAR" | cut -d ' ' -f 1)
    else
        echo 'A SHA-256 tool (sha256sum or shasum) is required.' >&2
        exit 1
    fi
    if [ "$actual" != "$WRAPPER_SHA256" ]; then
        echo "Unexpected Gradle Wrapper JAR checksum: $actual" >&2
        exit 1
    fi
}

if [ ! -f "$WRAPPER_JAR" ]; then
    echo 'Fetching published Gradle 9.8.2 Wrapper JAR (checksum-verified)...'
    tmp="$WRAPPER_JAR.tmp.$$"
    trap 'rm -f "$tmp"' EXIT HUP INT TERM
    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --silent --show-error --retry 3 --output "$tmp" "$WRAPPER_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget --quiet -O "$tmp" "$WRAPPER_URL"
    else
        echo 'curl or wget is required to download the Gradle Wrapper JAR.' >&2
        exit 1
    fi
    mv "$tmp" "$WRAPPER_JAR"
    verify_wrapper
    trap - EXIT HUP INT TERM
else
    verify_wrapper
fi

if [ -n "${JAVA_HOME:-}" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi
if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo 'Java not found: configure JAVA_HOME or install Java 25.' >&2
    exit 1
fi

exec "$JAVACMD" -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
