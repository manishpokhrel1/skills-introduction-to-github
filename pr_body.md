jni: controlled lifecycle + native test harness; CI: native-harness job

Summary
-------
Implement a safe start/stop JNI lifecycle for `WakeBridge`, add a tiny host-side native harness and optional `native_test` target, and add a CI job to build/run the harness as a smoke test.

Files changed
-------------
- `wakeword/src/wake_jni.cpp`
- `wakeword/src/wake_harness.cpp`
- `wakeword/CMakeLists.txt`
- `.github/workflows/android-ci.yml`

Key points
----------
- Java API unchanged: `WakeBridge.initWake()` / `WakeBridge.stopWake()`.
- Lifecycle: worker thread attaches/detaches to JVM, uses atomic flag + condition variable, and cleanly joins on stop.
- Harness: `wake_harness.cpp` provides a harmless symbol and an optional `main` when `BUILD_NATIVE_TEST=ON`.
- CMake: supports host builds (skips Android `log` when not on Android); `native_test` built only when requested.
- CI: `native-harness` job builds and runs `native_test` (host-side CMake), so CI can validate JNI lifecycle without Android NDK.

Local test (example)
--------------------
```powershell
cmake -S wakeword -B wakeword/build -DBUILD_NATIVE_TEST=ON -DCMAKE_BUILD_TYPE=Release
cmake --build wakeword/build --config Release --target native_test -- /m:2
.\wakeword\build\Release\native_test.exe
# expected output: "native_test: harness started" ... "native_test: harness finished"
```

Notes for reviewers
-------------------
- `BUILD_NATIVE_TEST` is OFF by default; CI enables it for the host smoke test.
- No runtime behavior change if `libwakeword` is absent (Java still handles missing native lib).
- Recommended reviewers: native team + Android lead.
