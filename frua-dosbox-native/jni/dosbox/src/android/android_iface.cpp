
//#include "render.h"
#include "SDL.h"
#include "dosbox.h"
#include "android_iface.h"
#include "android_internal.h"

//extern Render_t render;
//extern bool CPU_CycleAutoAdjust;
//extern bool CPU_SkipCycleAutoAdjust;
//extern Bit32s CPU_CycleMax;

void (SDLCALL *android_mixer_callback_cpp)(void *userdata, Uint8 *stream, int len);
dosbox_config_t dosbox_config;

android_mixer_callback_t android_mixer_callback;

void android_mixer_callback_c(short* userdata, unsigned char* stream, long len) {
    android_mixer_callback_cpp(userdata, stream, len);
}

void dosbox_init(jint rowbytes, jint memsize, jint frameskip, jint cycles,
        jboolean sound_enable, jboolean cycle_hack, jboolean refresh_hack) {
    dosbox_config.rowbytes = rowbytes;
    dosbox_config.abort = 0;
    dosbox_config.pause = 0;
    dosbox_config.memsize = memsize;
    dosbox_config.frameskip = frameskip;
    dosbox_config.cycles = cycles;
    dosbox_config.sound_enable = sound_enable;
    dosbox_config.cycle_hack = cycle_hack;
    dosbox_config.refresh_hack = refresh_hack;
}

void dosbox_start(const char* conf) {
    const char* argv[] = {"dosbox", "-conf", conf};
	dosbox_main(3, argv);
}

void dosbox_pause(void) {
    dosbox_config.pause = true;
}

void dosbox_resume(void) {
    dosbox_config.pause = false;
}

void dosbox_stop(void) {
    dosbox_config.abort = 1;
}

// settings

void set_rowbytes(jint value) {
    dosbox_config.rowbytes = value;
}

void set_sound_enabled(jboolean enabled) {
    dosbox_config.sound_enable = enabled;
}

void set_memory_size(jint value) {
    dosbox_config.memsize = value;
}

void set_frameskip(jint value) {
    dosbox_config.frameskip = value;
}

void set_cycle_hack_enabled(jboolean enabled) {
    dosbox_config.cycle_hack = enabled;
}

void set_refresh_hack_enabled(jboolean enabled) {
    dosbox_config.refresh_hack = enabled;
}