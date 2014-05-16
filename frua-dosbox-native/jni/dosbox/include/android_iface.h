#ifndef ANDROID_IFACE_H
#define ANDROID_IFACE_H

#include <jni.h>
#include "keyboard.h"

#define KEYBOARD_CTRL_FLAG 0x1
#define KEYBOARD_ALT_FLAG 0x02
#define KEYBOARD_SHIFT_FLAG 0x04

#ifdef __cplusplus
extern "C" {
#endif

typedef struct android_event {
	int event_type;
	int keycode;
	int modifier;
	float x;
	float y;
	float down_x;
	float down_y;
} android_event_t;

typedef void (*android_mixer_callback_t)(short*, unsigned char*, long);

void dosbox_init(jint rowbytes, jint memsize, jint frameskip, jint cycles,
        jboolean sound_enable, jboolean cycle_hack, jboolean refresh_hack);

void dosbox_start(const char* conf);

void dosbox_pause(void);

void dosbox_resume(void);

void dosbox_stop(void);

// settings

void set_rowbytes(jint value);

void set_sound_enabled(jboolean enabled);

void set_memory_size(jint value);

void set_frameskip(jint value);

void set_cycle_hack_enabled(jboolean enabled);

void set_refresh_hack_enabled(jboolean enabled);

#ifdef __cplusplus
}
#endif

#endif // ANDROID_IFACE_H
