package com.helmetplusone.android.dosbox;

import android.graphics.Bitmap;

/**
 * User: helmetplusone
 * Date: 4/11/13
 */
public class DosboxAndroid implements DosboxAndroidCallbacks {
    private final DosboxAndroidCallbacks callbacks;

    public DosboxAndroid(DosboxAndroidCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public static native void nativeSetSoundEnabled(boolean enabled);

    public static native void nativeSetMemorySize(int value);

    public static native void nativeSetFrameskip(int value);

    public static native void nativeSetCycleHackEnabled(boolean enabled);

    public static native void nativeSetRefreshHackEnabled(boolean enabled);

    public native void nativeStart(String confpath, Bitmap bitmap, int width, int height, int memsize, int frameskip,
                                   int cycles, boolean soundEnable, boolean cycleHack, boolean refreshHack);

    public static native void nativePause();

    public static native void nativeResume();

    public static native void nativeStop();

    public static native void nativeMouse(int x, int y, int downX, int downY, int action, int button);

    public static native boolean nativeKey(int keyCode, int down, boolean ctrl, boolean alt, boolean shift);

    @Override
    public void callbackVideoRedraw(int width, int height, int startLine, int endLine) {
        callbacks.callbackVideoRedraw(width, height, startLine, endLine);
    }

    @Override
    public Bitmap callbackVideoSetMode(int width, int height) {
        return callbacks.callbackVideoSetMode(width, height);
    }

    @Override
    public void callbackAudioInit(int rate, int channels, int encoding, int bufSize) {
        callbacks.callbackAudioInit(rate, channels, encoding, bufSize);
    }

    @Override
    public void callbackAudioWriteBuffer(int size) {
        callbacks.callbackAudioWriteBuffer(size);
    }

    @Override
    public short[] callbackAudioGetBuffer() {
        return callbacks.callbackAudioGetBuffer();
    }

    @Override
    public void callbackExit() {
        callbacks.callbackExit();
    }
}
