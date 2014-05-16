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

import android.graphics.PixelFormat;
import android.view.*;

/**
 * SurfaceView that displays DosBox output
 *
 * @author Locnet
 * @author helmetplusone
 */
class DosBoxView extends SurfaceView implements SurfaceHolder.Callback {
    static final int SPLASH_TIMEOUT_MESSAGE = -1;

    private static final int ORIGINAL_WIDTH = 320;
    private static final int ORIGINAL_HEIGH = 200;

    private DosBoxVideoThread video;
    private InputHandler input;
    private boolean running = false;

    public DosBoxView(DosBoxLauncher context) {
        super(context);
        this.video = new DosBoxVideoThread(this);
        this.input = new InputHandler(this);
        // Receive keyboard events
        setFocusableInTouchMode(true);
        setFocusable(true);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.RGB_565);
//        getHolder().setKeepScreenOn(true);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        input.handleTrackball(event);
        return true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        input.handleTouch(event);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, final KeyEvent event) {
        return input.handleKey(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, final KeyEvent event) {
        return input.handleKey(keyCode, event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        video.resetScreen(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        running = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
    }

    void start() {
        video.start();
    }

    boolean isRunning() { return running; }

    void stop() {
        video.stopVideo();
    }

    void joinVideo() {
        Utils.join(video);
    }

    void shutdown() {
        video.shutdown();
        video = null;
        input = null;
    }

    private boolean isLandscape() {
        return (getWidth() > getHeight());
    }

    private int availableHeight() {
        int size1 = getWidth();
        int size2 = getHeight();
        int width = Math.min(size1, size2);
        int height = Math.max(size1, size2);
        float ratio = ((float) ORIGINAL_HEIGH)/ORIGINAL_WIDTH;
        return height - Math.round(width * ratio) - 1;
    }

    DosBoxVideoThread getVideo() { return video; }
}

