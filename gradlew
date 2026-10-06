#!/bin/sh
# Lightweight project launcher for environments where Gradle is installed via Homebrew.
# Usage: ./gradlew <task>
if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle is not installed or is not on PATH. Install it with: brew install gradle" >&2
  exit 1
fi
exec gradle "$@"
