package com.helmetplusone.android.dosbox;

import android.graphics.Bitmap;

/**
 * User: helmetplusone
 * Date: 4/11/13
 */
public interface DosboxAndroidCallbacks {
    public void callbackVideoRedraw(int width, int height, int startLine, int endLine);

    public Bitmap callbackVideoSetMode(int width, int height);

    public void callbackAudioInit(int rate, int channels, int encoding, int bufSize);

    public void callbackAudioWriteBuffer(int size);

    public short[] callbackAudioGetBuffer();

    public void callbackExit();
}
