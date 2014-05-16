
#include <stdbool.h>
#include <stdlib.h>
#include <android/bitmap.h>
#include "jni_context.h"
#include "android_logging.h"

typedef struct jni_context {
    JNIEnv* env;
    jobject callback_obj;
    jclass callback_class;
    jmethodID video_redraw_method;
    jmethodID video_set_mode_method;
    jmethodID audio_init_method;
    jmethodID audio_buffer_write_method;
    jmethodID get_audio_buffer_method;
    jmethodID exit_method;
    bool destroyed;
    jobject video_bitmap;
    jobject audio_buffer;
    bool audio_enabled;
    android_mixer_callback_t mixer_callback;
    jint width;
    jint height;
} jni_context_t;

static jni_context_t ctx = {};

static int callback_video_redraw(jint start_line, jint end_line) {
    if(ctx.destroyed) return 1;
    (*ctx.env)->CallVoidMethod(ctx.env, ctx.callback_obj, ctx.video_redraw_method, ctx.width, ctx.height, start_line, end_line);
    return (*ctx.env)->ExceptionCheck(ctx.env);
}

int jni_context_init(JNIEnv* env, jobject callback_obj, jboolean audio_enabled) {
    ctx.destroyed = false;
    ctx.env = env; 
    ctx.callback_obj = (*env)->NewGlobalRef(env, callback_obj);
    if ((*env)->ExceptionCheck(env)) return 1;
    ctx.callback_class = (*env)->GetObjectClass(env, ctx.callback_obj);
    if ((*env)->ExceptionCheck(env)) return 1;
    ctx.video_redraw_method = (*env)->GetMethodID(env, ctx.callback_class, CALLBACK_VIDEO_REDRAW_NAME, CALLBACK_VIDEO_REDRAW_SIGNATURE);
    ctx.video_set_mode_method = (*env)->GetMethodID(env, ctx.callback_class, CALLBACK_VIDEO_SET_MODE_NAME, CALLBACK_VIDEO_SET_MODE_SIGNATURE);
    ctx.audio_init_method = (*env)->GetMethodID(env, ctx.callback_class, CALLBACK_AUDIO_INIT_NAME, CALLBACK_AUDIO_INIT_SIGNATURE);
    ctx.audio_buffer_write_method = (*env)->GetMethodID(env, ctx.callback_class, CALLBACK_AUDIO_WRITE_BUFFER_NAME, CALLBACK_AUDIO_WRITE_BUFFER_SIGNATURE);
    ctx.get_audio_buffer_method = (*env)->GetMethodID(env, ctx.callback_class, CALLBACK_AUDIO_GET_BUFFER_NAME, CALLBACK_AUDIO_GET_BUFFER_SIGNATURE);
    ctx.exit_method = (*env)->GetMethodID(env, ctx.callback_class, CALLBACK_EXIT_NAME, CALLBACK_EXIT_SIGNATURE);
    ctx.video_bitmap = NULL;
    ctx.audio_buffer = NULL;
    ctx.audio_enabled = audio_enabled;
    return (*env)->ExceptionCheck(env);
}

void jni_context_destroy(void) {
    if(ctx.destroyed) return;
    ctx.destroyed = true;
    (*ctx.env)->DeleteGlobalRef(ctx.env, ctx.callback_obj);
    if(NULL != ctx.video_bitmap) (*ctx.env)->DeleteGlobalRef(ctx.env, ctx.video_bitmap);
    if(NULL != ctx.audio_buffer) (*ctx.env)->DeleteGlobalRef(ctx.env, ctx.audio_buffer);
    ctx.env = NULL;
    ctx.callback_obj = NULL;
    ctx.callback_class = NULL;
    ctx.video_redraw_method = NULL;
    ctx.video_set_mode_method = NULL;
    ctx.audio_init_method = NULL;
    ctx.audio_buffer_write_method = NULL;
    ctx.get_audio_buffer_method = NULL;
    ctx.exit_method = NULL;
    ctx.video_bitmap = NULL;
    ctx.audio_buffer = NULL;
    ctx.mixer_callback = NULL;
}

int callback_video_set_mode(jint width, jint height) {
    if(ctx.destroyed) return 1;
    if(width == ctx.width && height == ctx.height) return 0;
    if(NULL != ctx.video_bitmap) (*ctx.env)->DeleteGlobalRef(ctx.env, ctx.video_bitmap);
    jobject bitmap = (*ctx.env)->CallObjectMethod(ctx.env, ctx.callback_obj, ctx.video_set_mode_method, width, height);
    if((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    ctx.video_bitmap = (*ctx.env)->NewGlobalRef(ctx.env, bitmap);
    if((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    ctx.width = width;
    ctx.height = height;
    return 0;
}

int callback_audio_init(android_mixer_callback_t mixer_callback, jint rate, jint channels,
        jint encoding, jint buf_size) {
    if(ctx.destroyed || !ctx.audio_enabled) return 1;
    (*ctx.env)->CallIntMethod(ctx.env, ctx.callback_obj, ctx.audio_init_method, rate, channels, encoding, buf_size);
    if((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    jshortArray buffer = (*ctx.env)->CallObjectMethod(ctx.env, ctx.callback_obj, ctx.get_audio_buffer_method);
    if ((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    ctx.audio_buffer = (*ctx.env)->NewGlobalRef(ctx.env, buffer);
    if ((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    ctx.mixer_callback = mixer_callback;
    return (*ctx.env)->ExceptionCheck(ctx.env);
}

int callback_audio_write_buffer(void) {
    if(ctx.destroyed || NULL == ctx.mixer_callback) return 1;
    jsize len = (*ctx.env)->GetArrayLength(ctx.env, ctx.audio_buffer);
    if((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    jshort* audio_buffer = (*ctx.env)->GetShortArrayElements(ctx.env, ctx.audio_buffer, NULL);
    if((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    short size = 0;
    (*ctx.mixer_callback)(&size, (unsigned char*) audio_buffer, (len << 1));
    (*ctx.env)->ReleaseShortArrayElements(ctx.env, ctx.audio_buffer, audio_buffer, 0);
    if((*ctx.env)->ExceptionCheck(ctx.env)) return 1;
    if (0 == size) return 0;
    (*ctx.env)->CallVoidMethod(ctx.env, ctx.callback_obj, ctx.audio_buffer_write_method, size);
    return (*ctx.env)->ExceptionCheck(ctx.env);
}

void callback_exit(void) {
    if(ctx.destroyed) return;
    (*ctx.env)->CallVoidMethod(ctx.env, ctx.callback_obj, ctx.exit_method);
}

int lock_bitmap(unsigned char** pixels_p) {
    if(ctx.destroyed || NULL == ctx.video_bitmap) return 1;
    if(AndroidBitmap_lockPixels(ctx.env, ctx.video_bitmap, (void**) pixels_p)) return 1;
    return 0;
}

int unlock_bitmap(jint start_line, jint end_line) {
    if(ctx.destroyed || NULL == ctx.video_bitmap) return 1;
    if(AndroidBitmap_unlockPixels(ctx.env, ctx.video_bitmap)) return 1;
    if(end_line > start_line && callback_video_redraw(start_line, end_line)) return 1;
    return 0;
}

int reset_bitmap(void) {
    if(ctx.destroyed || NULL == ctx.video_bitmap) return 1;
    void* pixels = NULL;
    if(AndroidBitmap_lockPixels(ctx.env, ctx.video_bitmap, &pixels)) return 1;
    memset(pixels, 0, ctx.width * ctx.height * 2);
    if(AndroidBitmap_unlockPixels(ctx.env, ctx.video_bitmap)) return 1;
    return 0;
}
