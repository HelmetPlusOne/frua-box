/*
 *  Copyright (C) 2011 Locnet (android.locnet@gmail.com)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

#ifndef	ANDROID_DOSBOX_H
#define ANDROID_DOSBOX_H

#include "android_iface.h"

#ifdef __cplusplus
extern "C" {
#endif

// called by dosbox
int android_set_video_mode(int width, int height, int depth);

int android_lock_surface(unsigned char** buffer_p);

int android_unlock_surface(int start_line, int end_line);

int android_reset_screen(void);

int android_open_audio(android_mixer_callback_t mixer_callback, int rate, int channels, int encoding, int buf_size);

int android_audio_write_buffer(void);

int android_poll_event(android_event_t* event);

#ifdef __cplusplus
}
#endif

#endif // ANDROID_DOSBOX_H
