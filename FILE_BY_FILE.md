# ORION — File-by-file explanations

This document lists every file from the final project layout and explains its purpose, interactions, implementation notes, and testing considerations. Each entry is at least five sentences long.

---

## README.md

The `README.md` is the project's front door: it explains what ORION is, its core goals, and how to get started building or testing it. It contains a quick start guide with the minimal commands to build the C++ wake engine and run the Android app in an emulator, plus pointers to important configuration files. The file also documents the project assumptions (no LLMs, accessibility-based screen awareness, user-consent-driven actions) so contributors don't accidentally scope-creep. It includes high-level architecture diagrams or links to `docs/architecture.md` and to sample workflows for the most common developer tasks. Finally, it lists known issues and where to look for stable branches, release tags, and CI status.

## LICENSE

This file contains the license terms (e.g., MIT or Apache) under which ORION is distributed. It clarifies reuse, derivative works, and attribution requirements so contributors and downstream users understand legal constraints. The file must be present at repo root to help package managers and automated tooling detect licensing during dependency scans. It also documents any third-party libraries and their licenses if they require special notices. For commercial or Play Store use, this file should be reviewed by legal counsel if you modify it.

## CHANGELOG.md

`CHANGELOG.md` describes the release history and notable changes for each version or tag. Each entry includes dates, a short description of new features, bugfixes, and breaking changes so integrators and testers can assess upgrades. It also includes migration notes when interface or permission behavior changes (e.g., different Android permission flows). Keep this file curated — automated tools and release scripts often parse it to generate release notes. Tests should point at the changelog entry that corresponds to the CI build they validate.

## .gitignore

This file lists patterns for files and folders that should not be committed (build artifacts, IDE settings, secrets). It's tailored to include Android/Gradle build outputs, CMake build folders, and any local demo recordings or generated model headers. The file reduces repository noise and prevents accidental commits of sensitive files like `local.env`. It should be kept up to date as new temp files or toolchains are adopted. CI and pre-commit hooks rely on it to avoid uploading bulky artifacts.

---

# docs/

These files provide deep, focused documentation for maintainers and reviewers. They are the single source of truth for architecture, wake-word design, Play Store compliance, and UX expectations.

## docs/overview.md

A concise project overview that explains ORION's purpose, target audience, and design constraints. It contains a summary of the technology stack (C++ wake engine, Kotlin Android app, no LLMs) and a short list of core features. The file also maps out the major subsystems and their public interfaces so new contributors can find code faster. It includes links to hands-on quickstarts in `examples/` and a security checklist for release. Finally, it outlines where to raise issues and how to propose changes.

## docs/architecture.md

A technical architecture document describing dataflows, process boundaries, and failure modes. It contains diagrams showing the wakeword engine, JNI bridge, accessibility service, intent router, automation engine, and knowledge fetchers. The doc explains critical design choices — e.g., why wakeword runs in C++, why accessibility is used instead of screen capture, and how permissions are handled. It lists expected performance metrics (CPU, latency) and describes where to measure them. There are also guidelines for adding new action types or data fetchers.

## docs/wake_word.md

An implementation-focused design note for the wake-word system: feature extraction, model format, thresholds, and test data expectations. It explains the MFCC pipeline, windowing, frame sizes, and how `model_weights.h` is generated and quantized. The document also explains how to tune detection thresholds, avoid false positives, and run the detector in a foreground service for Android. It lists recommended test recordings and the expected ROC tradeoffs. Finally, it documents JNI interfacing patterns for notifying Android when the keyword fires.

## docs/intent_system.md

This file explains the intent schema, matching algorithm, pattern priority, and fallback behavior. It defines intent types, the confidence scoring model for regex and keyword-based matches, and how to chain priorities and overrides for ambiguous input. It also details how to keep the system deterministic (no stochastic ranking) and where to insert future confidence-weighted heuristics. The document includes examples of complex utterances and how the router should resolve them. Test cases in `tests/android/IntentRouterTest.kt` map directly to the examples here.

## docs/automation.md

Explanation of the automation engine’s rule language: triggers, conditions, actions, and persistence. It details JSON schema for rules, execution semantics (synchronous vs asynchronous actions), failure handling, and how to roll back or cancel actions safely. The doc covers user-facing safety rules (e.g., requiring explicit confirmation for high-risk actions like bulk SMS or payments). It also recommends storage strategies for rules (encrypted local DB) and backup/export formats. Integration tests and end-to-end examples are referenced for each capability.

## docs/accessibility.md

A focused guide about using Android Accessibility APIs: what data is available, limitations, and Play Store policies. It emphasizes ethical usage: showing consent screens, minimal data collection, and transparent privacy language. The document lists accessible NodeInfo properties used by `AccessibilityReader.kt` and explains how to parse the UI tree for common apps. It also notes edge cases where app developers obfuscate UI text or where content descriptions are absent. Finally, it includes troubleshooting steps for emulator vs device behavior.

## docs/permissions.md

A catalog of all runtime and manifest permissions required by ORION and the rationale for each. It contains suggested dialog text for requesting permissions inline, Play Store submission guidance, and examples of how to degrade gracefully if permissions are denied. The doc recommends minimal-privilege approaches and explains which permissions require elevated review or the SMS default-role. It also documents how to log permission-related telemetry for debugging without storing user data. The final section has a checklist for Play Store reviewers.

## docs/playstore_rules.md

A practical guide for Play Store compliance: restricted APIs, use of accessibility services, SMS/CALL policies, and privacy policy requirements. It summarizes common rejection reasons and ways to avoid them (e.g., using the default SMS app role if needed). The file provides a template privacy policy paragraph tailored to ORION's behavior and sample screenshots for submission. It also explains how to prepare a justification if using AccessibilityService and necessary manifest entries. Lastly, it contains a list of contact points for escalation if a review fails.

---

# wakeword/

The C++ wakeword subproject: compact, testable, and separate from Android-specific code.

## wakeword/CMakeLists.txt

Build configuration for generating a native library. It defines compiler flags for ARM/ARM64, sets optimization levels, and builds position-independent code suitable for JNI. The file also declares unit test binaries under `tests/` and a small example app under `example/` to run on desktops for algorithm tuning. It includes a step to generate or include `model_weights.h` from an offline tool to avoid shipping large model files. Build options allow toggling SIMD acceleration and using platform-specific audio APIs.

## wakeword/include/audio_capture.h

Header that defines a minimal cross-platform audio capture abstraction (start, stop, read). It hides platform-specific details (AAudio/OpenSL ES/Linux ALSA) behind a common API so tests and the detector can consume a raw PCM stream. The header documents buffer sizes, sample rates (default 16000 Hz), and thread-safety guarantees. It also lists recommended error codes and ways to recover from transient mic errors. Consumers must handle blocking vs non-blocking reads according to the implementation notes here.

## wakeword/include/mfcc.h

The interface for computing MFCC features from a PCM buffer. This file documents buffer alignment, fixed-point vs floating implementations, normalization, and the expected output feature vector size (`FEATURE_SIZE`). It includes comments about windowing parameters and the DCT transform used. The header advises on numerical stability and how to produce deterministic feature vectors across platforms. Tests depend on this exact interface to validate feature correctness.

## wakeword/include/wake_detector.h

Declares the `WakeDetector` class: load weights, score MFCC vectors, configure thresholds, and persistent state for averaging. The header explains how to set hysteresis and debounce parameters to reduce fluttering activations. It also documents serialization for tiny per-device calibration metadata. Security notes mention that the detector should never write audio to disk unless explicitly enabled for debug.

## wakeword/include/config.h

Project-wide constants and compile-time configuration flags (sample rate, frame size, feature size). It centralizes options used in both `mfcc.cpp` and `wake_detector.cpp` so tuning is consistent. The file includes toggles for fixed-point builds on low-power devices and sample rate conversion considerations. It also documents where to change thresholds for experimentation. CI scripts read this file to ensure host vs target parity.

## wakeword/src/audio_capture.cpp

Concrete audio capture implementation that binds to the chosen platform API and supplies PCM frames to the processing loop. It contains buffering and a small jitter queue so the MFCC code gets consistent-length frames even under OS scheduling jitter. Built-in sanity checks ensure microphone format matches expected sample rates and channel counts. The file includes debug hooks to route raw audio into test recorders when `DEBUG_AUDIO` is enabled. For Android, this file is compiled into the native lib and accessed via JNI through `WakeBridge.kt`.

## wakeword/src/mfcc.cpp

Implementation of the MFCC pipeline: pre-emphasis, framing, windowing, FFT, mel-filter banks, log compression, and DCT. The implementation is kept simple, deterministic, and fast; it prioritizes CPU efficiency and numeric stability. Comments explain choices (e.g., number of mel bands) and how to alter them for best ROC/false-positive tradeoffs. The file includes test-only hooks for comparing outputs against reference vectors. It is intentionally written to be auditable and to avoid heavy floating-point libraries.

## wakeword/src/wake_detector.cpp

Contains the simple classifier: it loads `model_weights.h`, scores incoming feature vectors, applies smoothing, and emits a detection event when thresholds are exceeded. The file supports a tiny linear model by default but includes a stub to plug a grainy neural net if you later choose to improve accuracy. It exposes a clean API for the JNI bridge to poll or subscribe to detection events. The code also logs detection statistics counters used by CLI tests.

## wakeword/src/model_weights.h

A generated header with small quantized weights and bias for the detector model. This header is produced by an offline tool from your training pipeline and is included so the repository avoids large binary model files. It is intentionally small (kilobytes) and versioned so the detector is reproducible. The header contains a version string and an ad-hoc checksum to detect accidental corruption. Keep it out of public releases if you later choose to train proprietary models.

## wakeword/src/utils.cpp

Small utilities used across the wakeword codebase: circular buffer, fixed-point helpers, and a lightweight logger. The functions here are intentionally minimal to avoid pulling large dependencies into the native binary. The file includes a deterministic random seed generator for any calibration steps. Unit tests call these helpers directly to validate edge cases like buffer wrap-around.

## wakeword/tests/test_mfcc.cpp

Unit tests that verify MFCC outputs against stored reference vectors. They help catch regressions when porting to different CPU architectures or after micro-optimizations. The tests run in CI and on developer machines; failing tests indicate numerical differences that must be audited. Tests also validate boundary conditions like all-zeros input and very short buffers. Test assets are small and checked into `demo_data/` for reproducibility.

## wakeword/tests/test_detector.cpp

Unit tests for scoring logic, hysteresis behavior, and false-positive thresholds. The tests feed recorded buffers and check that the detector fires only in the expected windows. They simulate noisy conditions and validate debounce/hysteresis parameters to prevent flutter. The test harness can also simulate streaming audio to validate long-run stability. CI will run these with desktop audio capture disabled (using stored test files).

## wakeword/example/wake_test_main.cpp

A tiny runnable example that links audio capture → mfcc → wake_detector and prints events. It is intended to be used on a desktop to tune thresholds before packaging for Android. The example includes command-line flags for sample rate, hardware acceleration, and test mode that records PCM for offline analysis. Keep this file runnable so contributors can quickly validate models without the Android toolchain. The example is referenced from `scripts/run_wake_local.sh`.

## wakeword/README.md

Project-specific notes for the wake engine: build steps, cross-compile hints, and recommended hardware for testing. It points to the exact commands to generate `model_weights.h` from the offline training artifacts and how to produce the demo vectors in `demo_data/`. The file explains how to instrument the code for profiling and how to interpret the detection statistics produced by the detector. It also documents where to tweak thresholds for specific languages or accents.

---

# android/

Android app and native binding layer. This subtree contains the app module, resource files, JNI bridge, and Kotlin implementation.

## android/build.gradle.kts

Top-level Gradle Kotlin DSL build file that defines global settings like Kotlin/Gradle plugin versions and common repositories. It configures buildscript dependencies used by all modules and can declare plugin versions to ensure consistent builds across CI and developer machines. The file sets up some global signing config placeholders and could also define flavor dimensions if needed. This file rarely changes but must be kept compatible with the Android Gradle Plugin used by CI.

## android/settings.gradle.kts

Gradle settings file that defines project modules (e.g., :app). It can include composite build references to the native `wakeword/` CMake build if you decide to leverage Gradle's native integration. Keeping this file minimal prevents confusing developers; CI uses it to assemble the full build graph. If you later add more Android modules (e.g., :shared-ui) update this accordingly.

## android/gradle.properties

Properties that tune Gradle behavior: heap size, Kotlin compiler options, and other build flags. It is a convenient place to define default `org.gradle.jvmargs` and any feature toggles. Keep secrets out of this file; use environment variables for signing keys instead. CI reads this file when launching builds to ensure reproducible results.

## android/app/build.gradle.kts

Module-level build configuration for the app: compile SDK, target SDK, dependencies, and native library linkage (CMake). It declares required permissions in `AndroidManifest.xml`, configures packaging options, and links the native library produced by the `wakeword` CMake build. The file also defines build types (debug, release) and signing configs that CI will consume for Play Store artifacts. Keep dependency versions consistent and small to avoid APK bloat.

## android/app/src/main/AndroidManifest.xml

Android manifest with declared permissions, service entries, accessibility-service metadata, and intent filters. It documents the `OrionService` as a foreground service and includes `<uses-permission>` entries for `RECORD_AUDIO`, `CALL_PHONE`, `SEND_SMS`, `ACCESS_FINE_LOCATION`, and `BIND_ACCESSIBILITY_SERVICE`. The file also exposes exported components carefully and sets `android:requestLegacyExternalStorage` only if needed. Play Store reviewers will scrutinize this manifest, so justify each permission in `docs/playstore_rules.md`.

## android/app/src/main/java/com/orion/MainActivity.kt

The app entry point: a minimal UI that shows active service status, permission prompts, and a big mic button for push-to-talk. It provides a simple way to start or stop `OrionService` and to open the accessibility permission screen. The activity can display recent interactions and a quick toggle for automation rules. It intentionally avoids heavy UI logic; the goal is to provide plumbing and clear user consent flows. The file also contains debug toggles to simulate detections for development.

## android/app/src/main/java/com/orion/OrionService.kt

The foreground service that owns the wake engine lifecycle and coordinates the bridge to native code. It ensures the wake detector runs with a persistent notification and manages binding to the native library via `WakeBridge.kt`. `OrionService` also handles starting/stopping SpeechRecognizer after wake and routes recognized text to `IntentRouter`. It enforces power and privacy safeguards such as limiting continuous listening sessions and logging detection events for diagnostics. This service is the hardest-to-get-right piece for Play Store compliance.

## android/app/src/main/java/com/orion/WakeBridge.kt

JNI bridge loader and adapter: loads the native `orion_wake` library, exposes `startWakeEngine()` / `stopWakeEngine()` functions, and receives callbacks for detection events. It marshals small messages between C++ and Kotlin (timestamps, confidence scores) and ensures calls happen on appropriate threads. The file also provides a test hook for running the wake engine in simulation mode to avoid building native libraries during rapid dev cycles. Keep JNI signatures stable; changing them requires recompiling the native library.

## android/app/src/main/java/com/orion/SpeechInput.kt

Wrapper around `SpeechRecognizer` that provides a simple API to start listening with a timeout and return transcribed text via a callback or coroutine `suspend` function. It handles runtime permission checks and result codes, and it supports partial results if the platform provides them. This file also contains logic to cancel listening if the app loses audio focus or another call starts. Error handling is robust and maps different speech errors to actionable UI messages.

## android/app/src/main/java/com/orion/VoiceOutput.kt

Lightweight `TextToSpeech` wrapper with queueing and interruption semantics. It exposes `speak(text, interrupt)` and a cancellable speak token so the system can interrupt long speeches for urgent actions (like emergency calls). It also supports basic SSML markers if you later swap in a more advanced TTS engine. The file centralizes voice rate, pitch, and locale settings and includes a fallback for devices missing TTS data.

## android/app/src/main/java/com/orion/intent/IntentType.kt

Enum-like definitions for all supported intents (CALL, SEND_SMS, OPEN_APP, WHO_IS, WHAT_IS, NEWS, JOKE, LOCATION, etc.). Each enum entry carries metadata: whether it requires permission, whether it’s safe to execute without confirmation, and what default action to take. This file keeps intent metadata centralized so routers and UIs can enforce consistent behavior. Tests assert that each utterance maps to a valid `IntentType`.

## android/app/src/main/java/com/orion/intent/IntentRouter.kt

The deterministic router that receives recognized text and returns a resolved `Intent` object with parameters (contact name, number, topic, etc.). It applies `patterns.json` rules, priority overrides, and fallback heuristics. If an instruction maps to a local action, `IntentRouter` marks it as a `LOCAL_ACTION` so `ActionExecutor` can run it without network fetches. The router also records reasons for a match to help debugging ambiguous matches and to assist feature telemetry.

## android/app/src/main/java/com/orion/intent/IntentMatcher.kt

Low-level pattern-matching utilities: regex-based matching, token normalization (strip punctuation, expand contractions), and small fuzzy matching to handle misrecognitions. It exposes match confidence numbers used by `IntentRouter`. The matcher includes a small dictionary for contact-name resolution and normalization for numerals. This file is intentionally deterministic and free of ML.

## android/app/src/main/java/com/orion/actions/CallAction.kt

Implementation of the call action abstraction: resolves contacts, requests `CALL_PHONE` permission if needed, and starts a `CALL` intent or uses `ACTION_DIAL` as a fallback. It includes safeguards against accidentally calling premium numbers or repeated dialing. The file logs call attempts and outcomes for auditing and testability. For test runs, it simulates call intents rather than actually placing calls.

## android/app/src/main/java/com/orion/actions/SmsAction.kt

Handles sending SMS messages: composes the message, resolves recipients, and sends via `SmsManager` or the default SMS role if required. It enforces confirmation flows for bulk messages and exposes a dry-run mode for developer tests. The file includes retry logic for transient failures and documents Play Store SMS role implications. Beware: sending SMS must be explained clearly in UI to pass review.

## android/app/src/main/java/com/orion/actions/AppLaunchAction.kt

Resolves package names from app names or shortcuts and launches target apps. It supports optional extras in intents (e.g., open YouTube with a specific query) and fallback logic when a package isn’t found (opening the Play Store). The file includes a small whitelist mechanism to avoid launching system-protected components incorrectly. It documents edge cases where packages have multiple launchable activities.

## android/app/src/main/java/com/orion/actions/SystemAction.kt

Performs system-level actions: toggling Do Not Disturb, adjusting volume or brightness (where permitted), and opening specific settings pages. Many of these actions require different API levels and permissions; the file centralizes checks and shims for different Android versions. It handles user confirmation for sensitive actions and logs attempted changes for troubleshooting. The implementation avoids actions that require device admin unless the user explicitly grants it.

## android/app/src/main/ja... (truncated)
