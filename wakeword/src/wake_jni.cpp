#include "../include/jni.h"
#include "../include/android/log.h"
#include <thread>
#include <chrono>
#include <atomic>
#include <mutex>
#include <condition_variable>
#include <memory>

#define LOG_TAG "wake_jni"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static std::atomic<bool> g_running(false);
static std::unique_ptr<std::thread> g_worker;
static std::mutex g_mu;
static std::condition_variable g_cv;
static JavaVM* g_jvm = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_daami_WakeBridge_initWake(JNIEnv* env, jclass cls) {
    ALOGI("initWake called");
    bool expected = false;
    if (!g_running.compare_exchange_strong(expected, true)) {
        ALOGI("initWake: already running");
        return JNI_TRUE;
    }

    if (env == nullptr) {
        ALOGE("initWake: null JNIEnv");
        g_running = false;
        return JNI_FALSE;
    }

    if (env->GetJavaVM(&g_jvm) != JNI_OK || g_jvm == nullptr) {
        ALOGE("initWake: unable to get JavaVM");
        g_running = false;
        return JNI_FALSE;
    }

    // Start worker thread that will attach to JVM and call back periodically.
    g_worker.reset(new std::thread([]() {
        JNIEnv* threadEnv = nullptr;
        if (g_jvm->AttachCurrentThread(&threadEnv, nullptr) != JNI_OK) {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed to attach thread to JVM");
            return;
        }

        jclass callbackCls = threadEnv->FindClass("com/daami/WakeBridgeCallbacks");
        if (callbackCls == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Could not find WakeBridgeCallbacks class");
            g_jvm->DetachCurrentThread();
            return;
        }
        jmethodID mid = threadEnv->GetStaticMethodID(callbackCls, "onDetect", "(F)V");
        if (mid == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Could not find onDetect method");
            g_jvm->DetachCurrentThread();
            return;
        }

        ALOGI("wake_jni: worker started");
        std::unique_lock<std::mutex> lk(g_mu);
        while (g_running.load()) {
            // wait for either stop signal or timeout
            g_cv.wait_for(lk, std::chrono::seconds(4));
            if (!g_running.load()) break;

            // call Java callback with a dummy confidence value
            threadEnv->CallStaticVoidMethod(callbackCls, mid, (jfloat)0.75f);
            if (threadEnv->ExceptionCheck()) {
                threadEnv->ExceptionClear();
            }
        }

        ALOGI("wake_jni: worker stopping");
        g_jvm->DetachCurrentThread();
    }));

    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_daami_WakeBridge_stopWake(JNIEnv* env, jclass cls) {
    ALOGI("stopWake called");
    if (!g_running.load()) {
        ALOGI("stopWake: not running");
        return JNI_TRUE;
    }

    g_running = false;
    g_cv.notify_all();

    if (g_worker && g_worker->joinable()) {
        g_worker->join();
    }
    g_worker.reset();

    ALOGI("stopWake: stopped");
    return JNI_TRUE;
}
