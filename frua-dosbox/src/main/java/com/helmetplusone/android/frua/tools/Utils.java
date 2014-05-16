/*
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

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.io.*;

/**
 * Common utilities, mostly borrowed from apache commons-io
 *
 * @author helmetplusone
 * Date: 1/6/13
 */
public class Utils {
    /**
     * The default buffer size to use for
     * {@link #copy(InputStream, OutputStream)}
     * and
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Unconditionally close a <code>Closeable</code>.
     * <p/>
     * Equivalent to {@link java.io.Closeable#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p/>
     * Example code:
     * <pre>
     *   Closeable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @since Commons IO 2.0
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.3
     */
    public static long copy(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p/>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p/>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     *
     * @param file   the file to open for output, must not be <code>null</code>
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since Commons IO 2.1
     */
    public static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, false);
    }

    /**
     * Joins thread, converting interruption to boolean result
     *
     * @param thread thread to join with
     * @return true on successful join, false on interruption
     */
    public static boolean join(Thread thread) {
        try {
            thread.join();
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Calls {@code Thead.sleep()} ignoring interrupted exception
     *
     * @param millis time to sleep on millis
     */
    public static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public static String getPrefStr(PreferenceActivity ctx, int keyId) {
        String key = ctx.getResources().getString(keyId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        Object val = sp.getAll().get(key);
        if(null == val) return "";
        if (!(val instanceof String)) throw new IllegalArgumentException("Setting key: [" + key + "], " +
                " type is: [" + val.getClass().getName() + "], expected type: [String]");
        return (String) val;
    }

    public static void setPrefStr(PreferenceActivity ctx, int keyId, String val) {
        String key = ctx.getResources().getString(keyId);
        setPrefStr(ctx, key, val);
    }

    public static void setPrefStr(PreferenceActivity ctx, String key, String val) {
        if(null == val) throw new IllegalArgumentException("Provided values is null, key: [" + key + "]");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, val);
        editor.commit();
        Preference pref = ctx.findPreference(key);
        pref.setSummary(val);
    }

    public static boolean getPrefBool(PreferenceActivity ctx, int keyId) {
        String key = ctx.getResources().getString(keyId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        Object val = sp.getAll().get(key);
        if(null == val) return false;
        if (!(val instanceof Boolean)) throw new IllegalArgumentException("Setting key: [" + key + "], " +
                " type is: [" + val.getClass().getName() + "], expected type: [Boolean]");
        return (Boolean) val;
    }

    public static void setPrefBool(PreferenceActivity ctx, int keyId, boolean val) {
        String key = ctx.getResources().getString(keyId);
        setPrefBool(ctx, key, val);
    }

    public static void setPrefBool(PreferenceActivity ctx, String key, boolean val) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    public static Preference pref(PreferenceActivity ctx, int keyId) {
        String key = ctx.getResources().getString(keyId);
        return ctx.findPreference(key);
    }
}
