
#ifndef JNI_CONTEXT_H
#define JNI_CONTEXT_H

#include <stdbool.h>
#include <jni.h>
#include "jni_callbacks.h"
#include "android_iface.h"

int jni_context_init(JNIEnv* env, jclass callback_obj, jboolean audio_enabled);

void jni_context_destroy(void);

int callback_video_set_mode(jint width, jint height);

int callback_audio_init(android_mixer_callback_t mixer_callback, jint rate, jint channels, jint encoding, jint buf_size);

int callback_audio_write_buffer(void);

void callback_exit(void);

int lock_bitmap(unsigned char** pixels_p);

int unlock_bitmap(jint start_line, jint end_line);

int reset_bitmap(void);

#endif // JNI_CONTEXT_H