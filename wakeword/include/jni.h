#ifndef DA_MI_JNI_H
#define DA_MI_JNI_H

// Minimal C++-friendly JNI stubs for static analysis and local builds.
// Not a replacement for Android NDK headers; intended to silence analyzer warnings.

typedef int jint;
typedef float jfloat;
typedef unsigned char jboolean;

#define JNI_TRUE 1
#define JNI_FALSE 0
#define JNI_OK 0

typedef void* jobject;
typedef void* jclass;
typedef void* jmethodID;

struct JNIEnv;

struct JavaVM {
	int AttachCurrentThread(JNIEnv** penv, void* args) { (void)penv; (void)args; return JNI_OK; }
	int DetachCurrentThread() { return JNI_OK; }
};

// Simple C++ JNIEnv stub with methods used by the JNI bridge code.
struct JNIEnv {
	int GetJavaVM(JavaVM** vm) { *vm = nullptr; return JNI_OK; }
	jclass FindClass(const char* name) { (void)name; return nullptr; }
	jmethodID GetStaticMethodID(jclass cls, const char* name, const char* sig) { (void)cls; (void)name; (void)sig; return nullptr; }
	void CallStaticVoidMethod(jclass cls, jmethodID mid, ...) { (void)cls; (void)mid; }
	bool ExceptionCheck() { return false; }
	void ExceptionClear() {}
};

// Provide minimal macros for JNI export markers
#ifndef JNIEXPORT
#define JNIEXPORT
#endif

#ifndef JNICALL
#define JNICALL
#endif

#endif // DA_MI_JNI_H
