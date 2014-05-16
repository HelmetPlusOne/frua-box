package com.helmetplusone.android.frua.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import static org.apache.commons.io.FileUtils.copyFile;

/**
 * User: helmetplusone
 * Date: 1/23/13
 */
class CaseInsensitiveIoUtils {
    static final Charset ASCII_CHARSET = Charset.forName("ASCII");

    static void copyFileCi(File from, File to) throws IOException {
        final File fromCi;
        if(from.exists() && from.isFile()) fromCi = from;
        else {
            File lower = new File(from.getParentFile(), from.getName().toLowerCase(Locale.US));
            File upper = new File(from.getParentFile(), from.getName().toUpperCase(Locale.US));
            if(lower.exists() && lower.isFile()) fromCi = lower;
            else if(upper.exists() && upper.isFile()) fromCi = upper;
            else throw new FileNotFoundException(from.getAbsolutePath());
        }
        final File toCi;
        if(to.exists() && to.isFile()) toCi = to;
        else {
            File lower = new File(to.getParentFile(), to.getName().toLowerCase(Locale.US));
            File upper = new File(to.getParentFile(), to.getName().toUpperCase(Locale.US));
            if(upper.exists() && upper.isFile()) toCi = upper;
            else toCi = lower;
        }
        copyFile(fromCi, toCi);
    }

    static File existingCi(File dir, String name) throws FileNotFoundException {
        File res = existingCiOrNull(dir, name);
        if(null == res) throw new FileNotFoundException(new File(dir, name).getAbsolutePath());
        return res;
    }

    static File existingCiOrNull(File dir, String name) {
        final File orig = new File(dir, name);
        if(orig.exists()) return orig;
        File lower = new File(dir, name.toLowerCase(Locale.US));
        if(lower.exists()) return lower;
        File upper = new File(dir, name.toUpperCase(Locale.US));
        if(upper.exists()) return upper;
        return null;
    }

}
