#!/usr/bin/env bash
# Simple wrapper to run Gradle commands inside Docker
if [ -z "$PWD" ]; then
  echo "PWD not set"
  exit 1
fi
ROOT="$PWD"
IMAGE=gradle:8.3-jdk17
if [ -x "./gradlew" ]; then
  echo "Using local project wrapper ./gradlew"
  ./gradlew "$@"
else
  echo "Using Dockerized Gradle ($IMAGE)"
  docker run --rm -v "$ROOT":/workspace -w /workspace $IMAGE gradle "$@"
fi