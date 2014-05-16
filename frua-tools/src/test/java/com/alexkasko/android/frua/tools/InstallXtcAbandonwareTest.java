package com.helmetplusone.android.frua.tools;

import org.junit.Test;

import java.io.IOException;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
public class InstallXtcAbandonwareTest {
    @Test
    public void test() throws IOException {
        new InstallCaller().install("src/test/nonfree/xtcabandonware.com/unladv.zip");
    }
}
