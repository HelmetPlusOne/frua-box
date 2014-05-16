
#include <stdbool.h>
#include <unistd.h>
#include "SDL_events.h"
#include "keycodes.h"
#include "user_input.h"
#include "android_logging.h"

static android_event_t NO_EVENT = {.event_type = SDL_NOEVENT};
static cbuf_t event_buf = {}; 

static void getKeyFromUnicode(int unicode, android_event_t* event) {
	switch (unicode) {
		case '!': case '@':	case '#': case '$': case '%': case '^': case '&': case '*':	case '(': case ')':
		case '~': case '_': case '+': case '?': case '{': case '}': case ':': case '"': case '<': case '>':
		case '|':
			event->modifier = KEYBOARD_SHIFT_FLAG;
			break;
		default:
			if ((unicode >= 'A') && (unicode <= 'Z')) {
				event->modifier = KEYBOARD_SHIFT_FLAG;
			}
			break;
	}
	int dosboxKeycode = KBD_NONE;
	switch (unicode){
		case '!': case '1': dosboxKeycode = KBD_1; break;
		case '@': case '2': dosboxKeycode = KBD_2; break;
		case '#': case '3': dosboxKeycode = KBD_3; break;
		case '$': case '4': dosboxKeycode = KBD_4; break;
		case '%': case '5': dosboxKeycode = KBD_5; break;
		case '^': case '6': dosboxKeycode = KBD_6; break;
		case '&': case '7': dosboxKeycode = KBD_7; break;
		case '*': case '8': dosboxKeycode = KBD_8; break;
		case '(': case '9': dosboxKeycode = KBD_9; break;
		case ')': case '0': dosboxKeycode = KBD_0; break;
		case 'a': case 'A': dosboxKeycode = KBD_a; break;
		case 'b': case 'B': dosboxKeycode = KBD_b; break;
		case 'c': case 'C': dosboxKeycode = KBD_c; break;
		case 'd': case 'D': dosboxKeycode = KBD_d; break;
		case 'e': case 'E': dosboxKeycode = KBD_e; break;
		case 'f': case 'F': dosboxKeycode = KBD_f; break;
		case 'g': case 'G': dosboxKeycode = KBD_g; break;
		case 'h': case 'H': dosboxKeycode = KBD_h; break;
		case 'i': case 'I': dosboxKeycode = KBD_i; break;
		case 'j': case 'J': dosboxKeycode = KBD_j; break;
		case 'k': case 'K': dosboxKeycode = KBD_k; break;
		case 'l': case 'L': dosboxKeycode = KBD_l; break;
		case 'm': case 'M': dosboxKeycode = KBD_m; break;
		case 'n': case 'N': dosboxKeycode = KBD_n; break;
		case 'o': case 'O': dosboxKeycode = KBD_o; break;
		case 'p': case 'P': dosboxKeycode = KBD_p; break;
		case 'q': case 'Q': dosboxKeycode = KBD_q; break;
		case 'r': case 'R': dosboxKeycode = KBD_r; break;
		case 's': case 'S': dosboxKeycode = KBD_s; break;
		case 't': case 'T': dosboxKeycode = KBD_t; break;
		case 'u': case 'U': dosboxKeycode = KBD_u; break;
		case 'v': case 'V': dosboxKeycode = KBD_v; break;
		case 'w': case 'W': dosboxKeycode = KBD_w; break;
		case 'x': case 'X': dosboxKeycode = KBD_x; break;
		case 'y': case 'Y': dosboxKeycode = KBD_y; break;
		case 'z': case 'Z': dosboxKeycode = KBD_z; break;
		case 0x08: dosboxKeycode = KBD_backspace; break;
		case 0x09: dosboxKeycode = KBD_tab; break;
		case 0x20: dosboxKeycode = KBD_space; break;
		case 0x0A: dosboxKeycode = KBD_enter; break;
		case '~': case '`': dosboxKeycode = KBD_grave; break;
		case '_': case '-': dosboxKeycode = KBD_minus; break;
		case '+': case '=': dosboxKeycode = KBD_equals; break;
		case '?': case '/': dosboxKeycode = KBD_slash; break;
		case '{': case '[': dosboxKeycode = KBD_leftbracket; break;
		case '}': case ']': dosboxKeycode = KBD_rightbracket; break;
		case ':': case ';': dosboxKeycode = KBD_semicolon; break;
		case '"': case '\'': dosboxKeycode = KBD_quote; break;
		case '<': case ',': dosboxKeycode = KBD_comma; break;
		case '>': case '.': dosboxKeycode = KBD_period; break;
		case '|': case '\\': dosboxKeycode = KBD_backslash; break;
		case 0x1B: dosboxKeycode = KBD_esc; break;
		case 0x1C: dosboxKeycode = KBD_left; break;
		case 0x1D: dosboxKeycode = KBD_right; break;
		case 0x1E: dosboxKeycode = KBD_up; break;
		case 0x1F: dosboxKeycode = KBD_down; break;
		default: dosboxKeycode = KBD_NONE; break;
	}
	event->keycode = dosboxKeycode;
}

void user_input_init(void) {
    CBUF_INIT(event_buf, CBUF_SIZE, NO_EVENT);
}

void user_input_destroy(void) {
    while(!CBUF_IS_EMPTY(event_buf)) {
        CBUF_GET(event_buf, android_event_t);
    }
}

bool poll_event(android_event_t* event) {
    if(CBUF_IS_EMPTY(event_buf)) return false;
    android_event_t ev = CBUF_GET(event_buf, android_event_t);
    memcpy(event, &ev, sizeof(android_event_t));
    return true;
}

void mouse_event(jint x, jint y, jint down_x, jint down_y, jint action, jint button) {
	android_event_t	event = {.event_type = SDL_NOEVENT};

	switch (action) {
		case 0:
			event.event_type = SDL_MOUSEBUTTONDOWN;
			event.down_x = down_x;
			event.down_y = down_y;
			event.keycode = button;
			break;
		case 1:
			event.event_type = SDL_MOUSEBUTTONUP;
			event.keycode = button;
			break;
		case 2:
			event.event_type = SDL_MOUSEMOTION;
			event.down_x = down_x;
			event.down_y = down_y;
			event.x = x;
			event.y = y;

			break;
	}

	if 	(event.event_type != SDL_NOEVENT) {
        CBUF_PUT(event_buf, event);
    }
}

bool keyboard_event(jint key_code, jint down, jboolean ctrl, jboolean alt, jboolean shift) {
	int unicode = (key_code >> 8) & 0xFF;
	key_code = key_code & 0xFF;

	android_event_t event = {.keycode = KBD_NONE, .modifier = 0};

	if (unicode != 0) {
		getKeyFromUnicode(unicode, &event);
		if ((event.keycode != KBD_NONE) && (event.modifier == KEYBOARD_SHIFT_FLAG))
			shift = 1;
	}
	if (event.keycode == KBD_NONE) {
		int dosboxKeycode = KBD_NONE;

		switch (key_code) {
			case AKEYCODE_SHIFT_RIGHT: dosboxKeycode = KBD_rightshift; break;

			case AKEYCODE_CTRL_LEFT: dosboxKeycode = KBD_leftctrl; break;
			case AKEYCODE_ALT_LEFT: dosboxKeycode = KBD_leftalt; break;
			case AKEYCODE_SHIFT_LEFT: dosboxKeycode = KBD_leftshift; break;

			case AKEYCODE_INSERT:		dosboxKeycode = KBD_insert;	break;
			case AKEYCODE_HOME:		dosboxKeycode = KBD_home;	break;
			case AKEYCODE_FORWARD_DEL:		dosboxKeycode = KBD_delete;	break;
			case AKEYCODE_END:		dosboxKeycode = KBD_end;	break;

			case AKEYCODE_AT:		dosboxKeycode = KBD_2; shift = 1;	break;
			case AKEYCODE_POUND:	dosboxKeycode = KBD_3; shift = 1;	break;
			case AKEYCODE_STAR:		dosboxKeycode = KBD_8; shift = 1;	break;
			case AKEYCODE_PLUS:		dosboxKeycode = KBD_equals; shift = 1;	break;

			case AKEYCODE_ESCAPE:	dosboxKeycode = KBD_esc;			break;
			case AKEYCODE_TAB:		dosboxKeycode = KBD_tab;			break;
			case AKEYCODE_DEL:		dosboxKeycode = KBD_backspace;		break;
			case AKEYCODE_ENTER:	dosboxKeycode = KBD_enter;			break;
			case AKEYCODE_SPACE:	dosboxKeycode = KBD_space;			break;

			case AKEYCODE_DPAD_LEFT:	dosboxKeycode = KBD_left;			break;
			case AKEYCODE_DPAD_UP:		dosboxKeycode = KBD_up;			break;
			case AKEYCODE_DPAD_DOWN:	dosboxKeycode = KBD_down;			break;
			case AKEYCODE_DPAD_RIGHT:	dosboxKeycode = KBD_right;			break;

			case AKEYCODE_GRAVE:	dosboxKeycode = KBD_grave;			break;
			case AKEYCODE_MINUS:	dosboxKeycode = KBD_minus;			break;
			case AKEYCODE_EQUALS:	dosboxKeycode = KBD_equals;			break;
			case AKEYCODE_BACKSLASH:	dosboxKeycode = KBD_backslash;			break;
			case AKEYCODE_LEFT_BRACKET:	dosboxKeycode = KBD_leftbracket;			break;
			case AKEYCODE_RIGHT_BRACKET:	dosboxKeycode = KBD_rightbracket;			break;
			case AKEYCODE_SEMICOLON:	dosboxKeycode = KBD_semicolon;			break;
			case AKEYCODE_APOSTROPHE:	dosboxKeycode = KBD_quote;			break;
			case AKEYCODE_PERIOD:	dosboxKeycode = KBD_period;			break;
			case AKEYCODE_COMMA:	dosboxKeycode = KBD_comma;			break;
			case AKEYCODE_SLASH:		dosboxKeycode = KBD_slash;			break;
			//case AKEYCODE_DPAD_RIGHT:	dosboxKeycode = KBD_extra_lt_gt;			break;

			case AKEYCODE_PAGE_UP:		dosboxKeycode = KBD_pageup; 	break;
			case AKEYCODE_PAGE_DOWN:	dosboxKeycode = KBD_pagedown; 	break;

			case AKEYCODE_A:		dosboxKeycode = KBD_a;			break;
			case AKEYCODE_B:		dosboxKeycode = KBD_b;			break;
			case AKEYCODE_C:		dosboxKeycode = KBD_c;			break;
			case AKEYCODE_D:		dosboxKeycode = KBD_d;			break;
			case AKEYCODE_E:		dosboxKeycode = KBD_e;			break;
			case AKEYCODE_F:		dosboxKeycode = KBD_f;			break;
			case AKEYCODE_G:		dosboxKeycode = KBD_g;			break;
			case AKEYCODE_H:		dosboxKeycode = KBD_h;			break;
			case AKEYCODE_I:		dosboxKeycode = KBD_i;			break;
			case AKEYCODE_J:		dosboxKeycode = KBD_j;			break;
			case AKEYCODE_K:		dosboxKeycode = KBD_k;			break;
			case AKEYCODE_L:		dosboxKeycode = KBD_l;			break;
			case AKEYCODE_M:		dosboxKeycode = KBD_m;			break;
			case AKEYCODE_N:		dosboxKeycode = KBD_n;			break;
			case AKEYCODE_O:		dosboxKeycode = KBD_o;			break;
			case AKEYCODE_P:		dosboxKeycode = KBD_p;			break;
			case AKEYCODE_Q:		dosboxKeycode = KBD_q;			break;
			case AKEYCODE_R:		dosboxKeycode = KBD_r;			break;
			case AKEYCODE_S:		dosboxKeycode = KBD_s;			break;
			case AKEYCODE_T:		dosboxKeycode = KBD_t;			break;
			case AKEYCODE_U:		dosboxKeycode = KBD_u;			break;
			case AKEYCODE_V:		dosboxKeycode = KBD_v;			break;
			case AKEYCODE_W:		dosboxKeycode = KBD_w;			break;
			case AKEYCODE_X:		dosboxKeycode = KBD_x;			break;
			case AKEYCODE_Y:		dosboxKeycode = KBD_y;			break;
			case AKEYCODE_Z:		dosboxKeycode = KBD_z;			break;

			case AKEYCODE_0:		dosboxKeycode = KBD_0;			break;
			case AKEYCODE_1:		dosboxKeycode = KBD_1;			break;
			case AKEYCODE_2:		dosboxKeycode = KBD_2;			break;
			case AKEYCODE_3:		dosboxKeycode = KBD_3;			break;
			case AKEYCODE_4:		dosboxKeycode = KBD_4;			break;
			case AKEYCODE_5:		dosboxKeycode = KBD_5;			break;
			case AKEYCODE_6:		dosboxKeycode = KBD_6;			break;
			case AKEYCODE_7:		dosboxKeycode = KBD_7;			break;
			case AKEYCODE_8:		dosboxKeycode = KBD_8;			break;
			case AKEYCODE_9:		dosboxKeycode = KBD_9;			break;

			case AKEYCODE_F1:		dosboxKeycode = KBD_f1;			break;
			case AKEYCODE_F2:		dosboxKeycode = KBD_f2;			break;
			case AKEYCODE_F3:		dosboxKeycode = KBD_f3;			break;
			case AKEYCODE_F4:		dosboxKeycode = KBD_f4;			break;
			case AKEYCODE_F5:		dosboxKeycode = KBD_f5;			break;
			case AKEYCODE_F6:		dosboxKeycode = KBD_f6;			break;
			case AKEYCODE_F7:		dosboxKeycode = KBD_f7;			break;
			case AKEYCODE_F8:		dosboxKeycode = KBD_f8;			break;
			case AKEYCODE_F9:		dosboxKeycode = KBD_f9;			break;
			case AKEYCODE_F10:		dosboxKeycode = KBD_f10;			break;
			case AKEYCODE_F11:		dosboxKeycode = KBD_f11;			break;
			case AKEYCODE_F12:		dosboxKeycode = KBD_f12;			break;

			//locnet, 2011-05-30, add more key support
			case AKEYCODE_NUM_LOCK:		dosboxKeycode = KBD_numlock;			break;

			case AKEYCODE_NUMPAD_0:		dosboxKeycode = KBD_kp0;			break;
			case AKEYCODE_NUMPAD_1:		dosboxKeycode = KBD_kp1;			break;
			case AKEYCODE_NUMPAD_2:		dosboxKeycode = KBD_kp2;			break;
			case AKEYCODE_NUMPAD_3:		dosboxKeycode = KBD_kp3;			break;
			case AKEYCODE_NUMPAD_4:		dosboxKeycode = KBD_kp4;			break;
			case AKEYCODE_NUMPAD_5:		dosboxKeycode = KBD_kp5;			break;
			case AKEYCODE_NUMPAD_6:		dosboxKeycode = KBD_kp6;			break;
			case AKEYCODE_NUMPAD_7:		dosboxKeycode = KBD_kp7;			break;
			case AKEYCODE_NUMPAD_8:		dosboxKeycode = KBD_kp8;			break;
			case AKEYCODE_NUMPAD_9:		dosboxKeycode = KBD_kp9;			break;

			case AKEYCODE_NUMPAD_DIVIDE:		dosboxKeycode = KBD_kpdivide;			break;
			case AKEYCODE_NUMPAD_MULTIPLY:		dosboxKeycode = KBD_kpmultiply;			break;
			case AKEYCODE_NUMPAD_SUBTRACT:		dosboxKeycode = KBD_kpminus;			break;
			case AKEYCODE_NUMPAD_ADD:		dosboxKeycode = KBD_kpplus;			break;
			case AKEYCODE_NUMPAD_DOT:		dosboxKeycode = KBD_kpperiod;			break;
			case AKEYCODE_NUMPAD_ENTER:		dosboxKeycode = KBD_kpenter;			break;

			default:
				break;
		}

		event.keycode = dosboxKeycode;
	}

	if (event.keycode != KBD_NONE) {
		int modifier = 0;

		if (ctrl)
			modifier |= KEYBOARD_CTRL_FLAG;
		if (alt)
			modifier |= KEYBOARD_ALT_FLAG;
		if (shift)
			modifier |= KEYBOARD_SHIFT_FLAG;

		//myLoader.eventType = (down)?SDL_KEYDOWN:SDL_KEYUP;
		//myLoader.modifier = modifier;
		//myLoader.keycode = dosboxKeycode;

		event.event_type = (down)?SDL_KEYDOWN:SDL_KEYUP;
		event.modifier = modifier;
        CBUF_PUT(event_buf, event);
		return true;
	}
	else {
		return false;
	}
}
