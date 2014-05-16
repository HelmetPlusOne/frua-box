
#include <stdlib.h>
#include "android_iface.h"
#include "jni_functions.h"
#include "jni_context.h"
#include "user_input.h"
#include "android_logging.h"

#define TAG "DOSBOX"

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeStart(JNIEnv* env, jobject callback_obj, jstring conf,
        jobject bitmap, jint width, jint height, jint memsize, jint frameskip, jint cycles, jboolean sound_enable,
        jboolean cycle_hack, jboolean refresh_hack) {
    if(jni_context_init(env, callback_obj, sound_enable)) return;
//    ALOGE(TAG, "JNI context initialized");
    user_input_init();
//    ALOGE(TAG, "User input initialized");
    dosbox_init(width * 2, memsize, frameskip, cycles, sound_enable, cycle_hack, refresh_hack);
    const char* confpath = (*env)->GetStringUTFChars(env, conf, NULL);
//    ALOGE(TAG, "DOSBox initialized, starting with confpath: [%s]", confpath);
    dosbox_start(confpath);
//    ALOGE(TAG, "DOSBox stopping");
    // shutdown code here
    (*env)->ReleaseStringUTFChars(env, conf, confpath);
    user_input_destroy();
    callback_exit();
    jni_context_destroy();
//    ALOGE(TAG, "DOSBox stopped");
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativePause(JNIEnv* env, jclass callback_class) {
    dosbox_pause();
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeResume(JNIEnv* env, jclass callback_class) {
    dosbox_resume();
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeStop(JNIEnv* env, jclass callback_class) {
    dosbox_stop();
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetSoundEnabled(JNIEnv* env, jclass callback_class, jboolean enabled) {
    set_sound_enabled(enabled);
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetMemorySize(JNIEnv* env, jclass callback_class, jint value) {
    set_memory_size(value);
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetFrameskip(JNIEnv* env, jclass callback_class, jint value) {
    set_frameskip(value);
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetCycleHackEnabled(JNIEnv* env, jclass callback_class, jboolean enabled) {
    set_cycle_hack_enabled(enabled);
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetRefreshHackEnabled(JNIEnv* env, jclass callback_class, jboolean enabled) {
    set_refresh_hack_enabled(enabled);
}

void Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeMouse(JNIEnv* env, jclass callback_class, jint x, jint y,
        jint down_x, jint down_y, jint action, jint button) {
    mouse_event(x, y, down_x, down_y, action, button);
}

jboolean Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeKey(JNIEnv* env, jclass callback_class, jint key_code,
        jint down, jboolean ctrl, jboolean alt, jboolean shift) {
    return keyboard_event(key_code, down, ctrl, alt, shift) ? JNI_TRUE : JNI_FALSE;
}
