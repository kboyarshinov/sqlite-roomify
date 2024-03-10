#!/usr/bin/env bash
# Run sql-roomify.

./gradlew --quiet ":cli-main:installDist" && "./cli-main/build/install/cli-main/bin/cli-main" "$@"