
#ifndef USER_INPUT_H
#define USER_INPUT_H

#include <stdbool.h>
#include <jni.h>
#include "circular_buffer.h"
#include "android_iface.h"

#define CBUF_SIZE 32 

CBUF_TYPEDEF(cbuf_t, android_event_t, CBUF_SIZE);

void user_input_init(void);

void user_input_destroy(void);

bool poll_event(android_event_t* event);

void mouse_event(jint x, jint y, jint downX, jint downY, jint action, jint button);

bool keyboard_event(jint key_code, jint down, jboolean ctrl, jboolean alt, jboolean shift);

#endif //USER_INPUT_H
