#!/usr/bin/env bash
# Run unit tests using the Dockerized Gradle image
ROOT="$PWD"
IMAGE=gradle:8.3-jdk17
echo "Running tests using project Gradle wrapper if present, else Docker"
if [ -x "./gradlew" ]; then
	echo "Found ./gradlew - running wrapper"
	./gradlew test
else
	echo "No ./gradlew found - running Dockerized Gradle"
	docker run --rm -v "$ROOT":/workspace -w /workspace $IMAGE gradle test --no-daemon --stacktrace
fi