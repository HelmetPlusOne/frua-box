/*
 *  Copyright (C) 2011 Locnet (android.locnet@gmail.com)
 *  Copyright (C) 2013 Helmet (HelmetPlusOne@gmail.com)*
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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import com.helmetplusone.android.dosbox.DosboxAndroid;
import com.helmetplusone.android.dosbox.DosboxAndroidCallbacks;

import java.io.File;

/**
 * Main activity
 *
 * @author Locnet
 * @author helmetplusone
 */
public class DosBoxLauncher extends Activity implements DosboxAndroidCallbacks {
    public static final String TAG = "DOSBox";

	static {
		System.loadLibrary("frua-dosbox-native");
	}

    private final DosboxAndroid dosbox;
    private DosBoxView dosBoxView;
    private Audio audioDevice;
    private final Object audioDeviceLock = new Object();

    private Thread backgroundThread;

    public DosBoxLauncher() {
        this.dosbox = new DosboxAndroid(this);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
//        Log.e(TAG, "DosBoxLauncher.onCreate");
		super.onCreate(null);
        dosBoxView = new DosBoxView(this);
        registerForContextMenu(dosBoxView);
        setContentView(dosBoxView);
        Bundle bun = getIntent().getExtras();
        int cycles = bun.getInt("cycles");
        int frameskip = bun.getInt("frameskip");
        boolean sound = bun.getBoolean("sound");
        init(cycles, frameskip, sound);
	}

	@Override
	protected void onPause() {
        DosboxAndroid.nativePause();
        if (audioDevice != null) audioDevice.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
        DosboxAndroid.nativeResume();
	}

    private void destroy() {
        //        DosboxAndroid.nativeResume();//it won't die if not running
        //stop audio AFTER above
        //		if (audioDevice != null) audioDevice.pause();
        //		dosBoxView.stop();
        //        DosboxAndroid.nativeStop();

        if (null != backgroundThread) {
            backgroundThread.interrupt();
            Utils.join(backgroundThread);
            backgroundThread = null;
        }
        if (null != dosBoxView) {
            dosBoxView.stop();
            dosBoxView.joinVideo();
            dosBoxView.shutdown();
            dosBoxView = null;
        }
//        Log.e(TAG, "Background threads joined");
        if (audioDevice != null) {
            audioDevice.shutDownAudio();
            audioDevice = null;
        }
    }

    // native callbacks

    @Override
	public void callbackExit() {
        if (dosBoxView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(dosBoxView.getWindowToken(), 0);
            }
        }
        destroy();
//        Log.e(TAG, "Going to finish");
        super.finish();
        // dosbox does not tolerate process reuse
        android.os.Process.killProcess(android.os.Process.myPid());
	}

    @Override
	public void callbackVideoRedraw(int w, int h, int s, int e) {
        dosBoxView.getVideo().externalRedraw(w, h, s, e);
	}

    @Override
	public Bitmap callbackVideoSetMode(int w, int h) {
        return dosBoxView.getVideo().setMode(w, h);
	}
	
    @Override
    public void callbackAudioInit(int rate, int channels, int encoding, int bufSize) {
        synchronized (audioDeviceLock) {
            if(null == audioDevice) audioDevice = new Audio();
            try {
                audioDevice.initAudio(rate, channels, encoding, bufSize);
            } catch (Exception e) {
//                Log.e(TAG, "Audio init error", e);
                audioDevice = null;
            }
        }
    }

    @Override
	public void callbackAudioWriteBuffer(int size) {
        synchronized (audioDeviceLock) {
            if (null == audioDevice) return;
	        audioDevice.audioWriteBuffer(size);
        }
	}

    @Override
	public short[] callbackAudioGetBuffer() {
        synchronized (audioDeviceLock) {
            if (null == audioDevice) return null;
		    short[] res = audioDevice.getBuffer();
            Log.e(TAG, "Returning audio buffer, size: [" + res.length + "]");
            return res;
        }
	}


  	//return true to clear modifier
    /**
     * Sends key event to DosBox
     *
     * @param keyCode key code
     * @param down whether it's `down` event
     * @param ctrl control key modifier
     * @param alt alt key modifier
     * @param shift shift key modifier
     * @return where key
     */
	public static boolean sendNativeKey(int keyCode, boolean down, boolean ctrl, boolean alt, boolean shift) {
        boolean handled = DosboxAndroid.nativeKey(keyCode, boolToInt(down), ctrl, alt, shift);
//        Log.d(TAG, "Key handle result: [" + handled + "], keycode: [" + keyCode + "], down: [" + down + "]," +
//                        " ctrl: [" + ctrl + "], alt: [" + alt + "], shift: [" + shift + "]");
        return handled && !down;
	}

    // private methods

    static int boolToInt(boolean  bool) {
        return bool ? 1 : 0;
    }

    private void init(int cycles, int frameskip, boolean sound) {
        backgroundThread = new Thread(new BackgroundRunnable(cycles, frameskip, sound));
        backgroundThread.start();
        dosBoxView.start();
    }

    private class BackgroundRunnable implements Runnable {
        private final int cycles;
        private final int frameskip;
        private final boolean sound;

        private BackgroundRunnable(int cycles, int frameskip, boolean sound) {
            this.cycles = cycles;
            this.frameskip = frameskip;
            this.sound = sound;
        }

        @Override
        public void run() {
            Bitmap bitmap = dosBoxView.getVideo().getBitmap();
            File basedir = getExternalFilesDir(null);
            File conf = new File(basedir, "fruabox.conf");
//            Log.e(TAG, conf.getAbsolutePath() + " : " + conf.exists());
            dosbox.nativeStart(conf.getAbsolutePath(), bitmap, bitmap.getWidth(), bitmap.getHeight(), 4, frameskip, cycles, sound, true, true);
        }
    }
}

