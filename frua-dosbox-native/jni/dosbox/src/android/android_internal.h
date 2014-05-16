
#ifndef ANDROID_INTERNAL_H
#define ANDROID_INTERNAL_H

#include <jni.h>
#include "SDL.h"
#include "android_iface.h"

typedef struct dosbox_config {
	unsigned long rowbytes;
	long memsize;
	long frameskip;
	long cycles;
	bool sound_enable;
	bool cycle_hack;
	bool refresh_hack;
	long abort;
	bool pause;
} dosbox_config_t;

// http://david.tribble.com/text/cdiffs.htm#C99-func-ptr
extern void (SDLCALL *android_mixer_callback_cpp)(void *userdata, Uint8 *stream, int len);

extern android_mixer_callback_t android_mixer_callback;

extern "C" void android_mixer_callback_c(short* userdata, unsigned char* stream, long len);

int dosbox_main(int argc, const char* argv[]);

// global declaration
extern dosbox_config_t dosbox_config;

#endif // ANDROID_INTERNAL_H