#ifndef DA_MI_ANDROID_LOG_H
#define DA_MI_ANDROID_LOG_H

#define ANDROID_LOG_INFO 4
#define ANDROID_LOG_WARN 5
#define ANDROID_LOG_ERROR 6

static inline int __android_log_print(int prio, const char* tag, const char* fmt, ...) {
    (void)prio; (void)tag; (void)fmt; return 0;
}

#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, "wake_jni", __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, "wake_jni", __VA_ARGS__)

#endif // DA_MI_ANDROID_LOG_H
