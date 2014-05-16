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

import android.graphics.*;
import android.view.SurfaceHolder;

import static java.lang.System.currentTimeMillis;

/**
 * Video thread
 *
 * @author Locnet
 * @author helmetplusone
 * Date: 1/7/13
 */
class DosBoxVideoThread extends Thread {
    static final int DEFAULT_WIDTH = 640;//800;
    static final int DEFAULT_HEIGHT = 400;//600;

    static final int UPDATE_INTERVAL = 40;
    static final int UPDATE_INTERVAL_MIN = 20;
    static final int RESET_INTERVAL = 100;

    private int srcWidth;
    private int srcHeight;

    private int dirtyCount;

    private boolean dirty;
    private final Object dirtyLock = new Object();
    private int startLine;
    private int endLine;

    private long startTime = 0;
    private int frameCount = 0;
    private boolean running = false;

    private Bitmap bitmap;

    private final DosBoxView view;

    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final Rect dirtyRect = new Rect();
    private final Rect screenRect = new Rect();
    private final Paint bitmapPaint;
    private final Object surfaceHolderLock = new Object();

    DosBoxVideoThread(DosBoxView view) {
        this.view = view;

        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);

        bitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.RGB_565);
    }

    void stopVideo() {
        this.running = false;
    }

    public void run() {
        running = true;
        while (running) {
            if (!view.isRunning()) {
                frameCount = 0;
                Utils.threadSleep(1000);
                continue;
            }
            long curTime = currentTimeMillis();
            if (frameCount > RESET_INTERVAL) frameCount = 0;
            if (0 == frameCount) startTime = curTime - UPDATE_INTERVAL;
            frameCount += 1;
//            Log.e(TAG, "fps:" + 1000 * frameCount / (curTime - startTime));
            synchronized (dirtyLock) {
                if (dirty) {
                    videoRedraw(srcWidth, srcHeight, startLine, endLine);
                    dirty = false;
                }
            }
            long nextUpdateTime = startTime + (frameCount + 1) * UPDATE_INTERVAL;
            long sleepTime = nextUpdateTime - currentTimeMillis();
            Utils.threadSleep(Math.max(sleepTime, UPDATE_INTERVAL_MIN));
        }
    }

    void videoRedraw(int src_width, int src_height, int startLine, int endLine) {
        if (!view.isRunning() || (bitmap == null) || (src_width <= 0) || (src_height <= 0)) return;
        SurfaceHolder surfaceHolder = view.getHolder();
        Canvas canvas = null;
        try {
            synchronized (surfaceHolderLock) {
                int dst_width = view.getWidth();
                int dst_height = view.getHeight();
                boolean isDirty = false;

                if (dirtyCount < 3) {
                    dirtyCount++;
                    isDirty = true;
                    startLine = 0;
                    endLine = src_height;
                }

                int tmp = src_width * dst_height / src_height;

                if (tmp < dst_width) {
                    dst_width = tmp;
                } else if (tmp > dst_width) {
                    dst_height = src_height * dst_width / src_width;
                }

                tmp = (view.getWidth() - dst_width) / 2;
                srcRect.set(0, 0, src_width, src_height);
                dstRect.set(0, 0, dst_width, dst_height);
                dstRect.offset(tmp, 0);

                dirtyRect.set(0, startLine * dst_height / src_height, dst_width, endLine * dst_height / src_height + 1);

                //locnet, 2011-04-21, a strip on right side not updated
                dirtyRect.offset(tmp, 0);

                //locnet, 2011-06-10, for absolute mouse
                screenRect.set(dstRect);

                if (isDirty) {
                    canvas = surfaceHolder.lockCanvas(null);
                    canvas.drawColor(0xff202020);
                } else {
                    canvas = surfaceHolder.lockCanvas(dirtyRect);
                }

                //locnet, 2011-04-28, support 2.1 or below
//                if (buffer != null) {
//                    buffer.position(0);
//                    //locnet, 2012-01-23, ensure buffer is correct
//                    if (bitmap.getWidth() * bitmap.getHeight() * 2 == buffer.remaining()) {
//                        bitmap.copyPixelsFromBuffer(buffer);
//                    } else {
//                        //Toast.makeText(mParent, "Invalid buffer", Toast.LENGTH_SHORT).show();
//                        //Log.d("dosbox", "bitmap vs buffer: "+bitmap.getWidth()+"x"+bitmap.getHeight()+":"+mVideoBuffer.remaining());
//                    }
//                }
//                Paint bmp = view.getConfig().isSoapVideo() ? bitmapPaint : null;
                canvas.drawBitmap(bitmap, srcRect, dstRect, null);
            }
        } finally {
            if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void setDirty() {
        dirtyCount = 0;
    }

    void resetScreen(boolean redraw) {
        setDirty();
        if (redraw) forceRedraw();
    }

    void forceRedraw() {
        setDirty();
        videoRedraw(srcWidth, srcHeight, 0, srcHeight);
    }

    void shutdown() {
        bitmap = null;
    }

    Bitmap setMode(int w, int h) {
        srcWidth = w;
        srcHeight = h;
        resetScreen(false);
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        if (newBitmap != null) {
            bitmap = null;
            bitmap = newBitmap;
            return bitmap;
        }
        return null;
    }

    void externalRedraw(int w, int h, int s, int e) {
        srcWidth = w;
        srcHeight = h;
        synchronized (dirtyLock) {
            if (dirty) {
                startLine = Math.min(startLine, s);
                endLine = Math.max(endLine, e);
            } else {
                startLine = s;
                endLine = e;
            }
            dirty = true;
        }
    }

    Bitmap getBitmap() { return bitmap; }

    Rect getScreenRect() { return screenRect; }
}