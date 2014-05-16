/*
 *  Copyright (C) 2011 Locnet (android.locnet@gmail.com)
 *  Copyright (C) 2013 Helmet (HelmetPlusOne@gmail.com)
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

package com.helmetplusone.android.frua.tools;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Audio callbacks implementation, Locnet's code left almost untouched
 *
 * @author Locnet
 * @author helmetplusone
 */
class Audio {
    private boolean running = true;
    private AudioTrack audioTrack;
    private long lastWriteBufferTime = 0;
    private int audioMinUpdateInterval = 10;
    private short[] buffer;

    int initAudio(int rate, int channels, int encoding, int bufSize) {
        if (audioTrack == null) {
            int bufSize2 = bufSize;

            channels = (channels == 1) ? AudioFormat.CHANNEL_CONFIGURATION_MONO : AudioFormat.CHANNEL_CONFIGURATION_STEREO;
            encoding = (encoding == 1) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;

            if (AudioTrack.getMinBufferSize(rate, channels, encoding) > bufSize) {
                //bufSize = AudioTrack.getMinBufferSize( rate, channels, encoding );
                bufSize2 = AudioTrack.getMinBufferSize(rate, channels, encoding);
                bufSize2 = Math.max(bufSize2, bufSize * 2);
            }

//            audioMinUpdateInterval = 1000 * (bufSize >> 1) / ((channels == AudioFormat.CHANNEL_CONFIGURATION_MONO) ? 1 : 2) / rate;

            buffer = new short[bufSize >> 1];
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    rate,
                    channels,
                    encoding,
                    //bufSize,
                    bufSize2,
                    AudioTrack.MODE_STREAM);
            audioTrack.pause();
            return bufSize;
        }
        return 0;
    }

    void shutDownAudio() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
        buffer = null;
    }

    void audioWriteBuffer(int size) {
        if ((buffer != null) && running) {
            long now = System.currentTimeMillis();
            if ((now - lastWriteBufferTime) > audioMinUpdateInterval) {
                if (size > 0)
                    writeSamples(buffer, (size << 1));
                lastWriteBufferTime = now;
            }
        }
    }

    void setRunning() {
        running = !running;
        if (!running) audioTrack.pause();
    }

    void writeSamples(short[] samples, int size) {
        if (running) {
            if (audioTrack != null) {
                audioTrack.write(samples, 0, size);
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
                    play();
            }
        }
    }

    void play() {
        if (null == audioTrack) return;
        audioTrack.play();
    }

    void pause() {
        if (null == audioTrack) return;
        audioTrack.pause();
    }

    short[] getBuffer() { return buffer; }
}