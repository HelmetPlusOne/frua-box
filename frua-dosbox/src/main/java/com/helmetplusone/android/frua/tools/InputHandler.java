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

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import static com.helmetplusone.android.dosbox.DosboxAndroid.nativeMouse;
import static com.helmetplusone.android.frua.tools.DosBoxLauncher.sendNativeKey;
import static com.helmetplusone.android.frua.tools.DosBoxLauncher.TAG;
import static com.helmetplusone.android.frua.tools.DosBoxView.SPLASH_TIMEOUT_MESSAGE;

/**
 * User input handler
 *
 * @author Locnet
 * @author helmetplusone
 * Date: 1/7/13
 */
class InputHandler extends Handler {
    static final int MOUSE_ACTION_DOWN = 0;
    static final int MOUSE_ACTION_UP = 1;
    static final int MOUSE_ACTION_MOVE = 2;

    static final int BUTTON_TAP_DELAY = 200;

    static final int KEYCODE_ESCAPE = 111;
    static final int KEYCODE_HOME = 122;
    static final int KEYCODE_END = 123;
    static final int KEYCODE_PGUP = 92;
    static final int KEYCODE_PGDOWN = 93;
    static final int KEYCODE_V = 30258;
    static final int KEYCODE_E = 25889;

    //locnet, 2011-05-30, support more key
    static final int KEYCODE_NUM_LOCK = 143;

    private boolean ctrl = false;
    private boolean alt = false;
    private boolean shift = false;

    private final DosBoxView view;

    InputHandler(DosBoxView view) {
        this.view = view;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SPLASH_TIMEOUT_MESSAGE:
                view.setBackgroundResource(0);
                //locnet, 2011-05-30, initialize numlock to on
                sendNativeKey(KEYCODE_NUM_LOCK, true, ctrl, alt, shift);
                Utils.threadSleep(BUTTON_TAP_DELAY);
                sendNativeKey(KEYCODE_NUM_LOCK, false, ctrl, alt, shift);
                ctrl = false;
                alt = false;
                shift = false;
                break;
            default:
                sendKey(msg.what, false);
                break;
        }
    }

    boolean handleKey(int keyCode, final KeyEvent event) {
        boolean down = (event.getAction() == KeyEvent.ACTION_DOWN);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                sendKey(KEYCODE_ESCAPE, down);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                sendKey(KEYCODE_HOME, down);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                sendKey(KEYCODE_END, down);
                return true;
            case KeyEvent.KEYCODE_SEARCH:
                sendKey(KEYCODE_V, down);
                return true;
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_UNKNOWN:
                break;
            default:
                if (!down || (event.getRepeatCount() == 0)) {
                    int unicode = event.getUnicodeChar();
                    Log.d(TAG, "Unicode char: [" + (char) unicode + "]");
                    //locnet, 2012-01-23, filter system generated modifier key, but not hardware key
                    //Log.d("dosbox", "down:" + down + ",isAlt:" + event.isAltPressed() + ",isShift:" + event.isShiftPressed() + ",repeat:" + event.getRepeatCount() + ",unicode:" + event.getUnicodeChar() + ",keycode:" + keyCode + ",flags:"+event.getFlags());
                    if ((event.isAltPressed() || event.isShiftPressed()) && (unicode == 0) && ((event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) == 0)) {
                        break;
                    }
                    //fixed alt key problem for physical keyboard with only left alt
                    if (keyCode == KeyEvent.KEYCODE_ALT_LEFT) {
                        break;
                    }
                    if ((keyCode > 255) || (unicode > 255)) {
                        //unknown keys
                        break;
                    }
                    keyCode = keyCode | (unicode << 8);
                    sendKey(keyCode, down);
                }
                break;
        }
        return false;
    }

    void handleTrackball(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                nativeMouse(0, 0, -1, -1, MOUSE_ACTION_DOWN, 0);
                break;
            case MotionEvent.ACTION_UP:
                nativeMouse(0, 0, -1, -1, MOUSE_ACTION_UP, 0);
                break;
            case MotionEvent.ACTION_MOVE: {
                int cur_x = (int) (event.getX() * 10);
                int cur_y = (int) (event.getY() * 10);

                //trackball movement difference may be very small
                //if ((Math.abs(cur_x - down_x) > MOUSEMOVE_MIN) || (Math.abs(cur_y - down_y) > MOUSEMOVE_MIN)) {
//                if (mPrefMouseSensitivity != DEFAULT_MOUSE_SENSITIVITY) {
//                    //DosBoxControl.nativeMouse((int)(cur_x * mMouseScale), (int)(cur_y * mMouseScale), (int)(down_x * mMouseScale), (int)(down_y * mMouseScale), MOUSE_ACTION_MOVE, -1);
//                    float mouseScale = 1.0f;
//                    DosBoxControl.nativeMouse((int) (cur_x * mouseScale), (int) (cur_y * mouseScale), -1024, -1024, MOUSE_ACTION_MOVE, -1);
//                } else
                    nativeMouse(cur_x, cur_y, -1024, -1024, MOUSE_ACTION_MOVE, -1);
                //}
                Utils.threadSleep(100);
            }
            break;
        }
    }

    void handleTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean inScreen = handleAbsoluteMouseMove(event);
                if (inScreen) {
                    try {
                        Thread.sleep(BUTTON_TAP_DELAY);
                    } catch (InterruptedException e) {
                    }
                    nativeMouse(0, 0, -1, -1, MOUSE_ACTION_DOWN, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                long diff = event.getEventTime() - event.getDownTime();
                if (diff < BUTTON_TAP_DELAY) Utils.threadSleep(BUTTON_TAP_DELAY - diff);
                nativeMouse(0, 0, -1, -1, MOUSE_ACTION_UP, 0);
                break;
            case MotionEvent.ACTION_MOVE:
                handleAbsoluteMouseMove(event);
                Utils.threadSleep(100);
                break;
        }
    }

    void sendKeyClick(int keyCode) {
        sendKey(keyCode, true);
        sendKey(keyCode, false);
    }

    private void sendKey(int keyCode, boolean down) {
        if (down && hasMessages(keyCode)) {
            Log.d(TAG, "Ignoring repeated down key: [" + keyCode + "]");
        } else {
            boolean handled = sendNativeKey(keyCode, down, ctrl, alt, shift);
            if (handled) {
                ctrl = false;
                alt = false;
                shift = false;
            }
        }
    }

    private boolean handleAbsoluteMouseMove(MotionEvent event) {
        boolean inScreen = false;

        Rect rect = view.getVideo().getScreenRect();
        float abs_x = (event.getX() - rect.left) / rect.width();
        float abs_y = (event.getY() - rect.top) / rect.height();

   		/*if (abs_x < 0) abs_x = 0;
           if (abs_x > 1) abs_x = 1;

   		if (abs_y < 0) abs_y = 0;
   		if (abs_y > 1) abs_y = 1;*/

        if ((abs_x >= 0) && (abs_x <= 1) && (abs_y >= 0) && (abs_y <= 1)) {
            inScreen = true;
            nativeMouse(0, 0, (int) (abs_x * 1000), (int) (abs_y * 1000), MOUSE_ACTION_DOWN, -1);
        }
        return inScreen;
    }
}
