
#include <stdbool.h>
#include "android_iface.h"
#include "dosbox_android.h"
#include "user_input.h"
#include "jni_context.h"
#include "android_logging.h"

#define TAG "DOSBOX"

static bool destroyed = false;

static int fail(void) {
    if(!destroyed) {
        dosbox_stop();
        destroyed = true;
    }
    return 1;
}

int android_set_video_mode(int width, int height, int depth) {
//    ALOGE(TAG, "");
    if(destroyed) return 1;
    if(callback_video_set_mode(width, height)) return fail();
    set_rowbytes(width * 2);
    return 0;
}

int android_lock_surface(unsigned char** buffer_p) {
//    ALOGE(TAG, "");
    if(destroyed) return 1;
    if(lock_bitmap(buffer_p)) return fail();
    return 0;
}

int android_unlock_surface(int start_line, int end_line) {
    if(destroyed) return 1;
    if(unlock_bitmap(start_line, end_line)) return fail();
    return 0;
}

int android_reset_screen(void) {
//    ALOGE(TAG, "");
    if(reset_bitmap()) return fail();
    return 0;
}

int android_open_audio(android_mixer_callback_t mixer_callback, int rate, int channels, int encoding, int buf_size) {
//    ALOGE(TAG, "");
    if(callback_audio_init(mixer_callback, rate, channels, encoding, buf_size)) return fail();
//    ALOGE(TAG, "success");
    return 0;
}

int android_audio_write_buffer(void) {
//    ALOGE(TAG, "");
    if(callback_audio_write_buffer()) return fail();
    return 0;
}

int android_poll_event(android_event_t* event) {
    return poll_event(event);
}
