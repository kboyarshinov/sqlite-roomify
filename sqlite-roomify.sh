#!/usr/bin/env bash
# Run sqlite-roomify.

./gradlew --quiet ":cli-main:installDist" && "./cli-main/build/install/cli-main/bin/cli-main" "$@"