cmake_minimum_required(VERSION 3.10)
project(wakeword)

add_library(wakeword SHARED
    src/audio_capture.cpp
    src/mfcc.cpp
    src/wake_detector.cpp
    src/utils.cpp
    src/wake_jni.cpp
)

# Optional small harness/test executable (OFF by default). CI can set
# -DBUILD_NATIVE_TEST=ON to build and run `native_test` as a host-side smoke
# test. Default OFF to avoid changing normal library build behavior.
option(BUILD_NATIVE_TEST "Build native test executable" OFF)

# If harness source exists, add it to the library (harmless symbol).
if(EXISTS ${CMAKE_SOURCE_DIR}/src/wake_harness.cpp)
    target_sources(wakeword PRIVATE src/wake_harness.cpp)
endif()

if(BUILD_NATIVE_TEST)
    add_executable(native_test src/wake_harness.cpp)
    # link log only on Android where it exists
    if(ANDROID)
        target_link_libraries(native_test PRIVATE ${log-lib})
    endif()
    # Ensure the BUILD_NATIVE_TEST macro is visible to the compiler so that
    # the `main` in `wake_harness.cpp` is compiled.
    target_compile_definitions(native_test PRIVATE BUILD_NATIVE_TEST=1)
endif()

# On Android, link against `log`. For host (CI) builds skip it so the
# harness can be built and run on the runner without the Android NDK.
if(ANDROID)
    find_library(log-lib log)
endif()

target_include_directories(wakeword PUBLIC include)
if(ANDROID)
    target_link_libraries(wakeword PUBLIC ${log-lib})
endif()
