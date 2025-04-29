#!/bin/bash

set -e

# === Configuration ===
APP_NAME="Klotski"
MAIN_CLASS="io.github.jimzhouzzy.klotski.lwjgl3.Lwjgl3Launcher"
# ICON_PATH="icons/icon.png"     # <-- Optional: Set platform-specific icons here
JAR_DIR="lwjgl3/build/libs"
OUTPUT_DIR="bundled"

# === Run Gradle Build ===
echo "Running Gradle build..."

if [[ "$(uname)" == "MINGW"* || "$(uname)" == "MSYS"* || "$(uname)" == "CYGWIN"* ]]; then
  gradleCmd="./gradlew.bat" # Use the Windows batch file
else
  gradleCmd="./gradlew" # Use the Unix shell script
fi

# Execute the Gradle build
$gradleCmd build

# Check if the build was successful
if [[ $? -ne 0 ]]; then
  echo "Gradle build failed. Exiting."
  exit 1
fi

# === Detect Latest JAR ===
JAR_FILE=$(ls -t "$JAR_DIR"/Klotski-*.jar | head -n 1)

if [ ! -f "$JAR_FILE" ]; then
    echo "No JAR file found in $JAR_DIR"
    exit 1
fi

echo "Using JAR: $JAR_FILE"

# === Prepare Output ===
mkdir -p "$OUTPUT_DIR"

# === jpackage Options ===
COMMON_OPTS=(
  --name "$APP_NAME"
  --input "$(dirname "$JAR_FILE")"
  --main-jar "$(basename "$JAR_FILE")"
  --main-class "$MAIN_CLASS"
  --dest "$OUTPUT_DIR"
  --app-version "1.0.0"
  --vendor "JimZhouZZY"
  --java-options -XstartOnFirstThread
)

if [[ -f "$ICON_PATH" ]]; then
  COMMON_OPTS+=(--icon "$ICON_PATH")
fi

# === Platform-Specific Packaging ===
# DMG for MacOS jpackage "${COMMON_OPTS[@]}" --type dmg

case "$(uname)" in
  Darwin)
    echo "Detected macOS"
    jpackage "${COMMON_OPTS[@]}" --type pkg
    ;;
  Linux)
    echo "Detected Linux"
    jpackage "${COMMON_OPTS[@]}" --type app-image
    ;;
  MINGW* | MSYS* | CYGWIN* | Windows_NT)
    echo "Detected Windows"
    jpackage "${COMMON_OPTS[@]}" --type exe
    ;;
  *)
    echo "Unsupported OS: $(uname)"
    exit 1
    ;;
esac

echo "✅ Bundling completed. Output in $OUTPUT_DIR"
