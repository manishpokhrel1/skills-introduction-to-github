#!/usr/bin/env bash
set -e
GRADLE_VERSION=8.3
WRAPPER_DIR="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
GRADLE_HOME="$WRAPPER_DIR/gradle-${GRADLE_VERSION}"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"
if [ -x "$GRADLE_BIN" ]; then
  exec "$GRADLE_BIN" "$@"
fi
ZIP_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
TMPZIP="/tmp/gradle-${GRADLE_VERSION}.zip"
mkdir -p "$WRAPPER_DIR"
if [ ! -f "$WRAPPER_DIR/gradle-${GRADLE_VERSION}" ]; then
  echo "Downloading Gradle ${GRADLE_VERSION}..."
  curl -L -o "$TMPZIP" "$ZIP_URL"
  echo "Extracting..."
  unzip -q -o "$TMPZIP" -d "$WRAPPER_DIR"
fi
exec "$WRAPPER_DIR/gradle-${GRADLE_VERSION}/bin/gradle" "$@"
#!/usr/bin/env sh
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
JAVA=java
if [ -z "$JAVA_HOME" ]; then
  JAVACMD="$JAVA"
else
  JAVACMD="$JAVA_HOME/bin/java"
fi
exec "$JAVACMD" -classpath "$HERE/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
