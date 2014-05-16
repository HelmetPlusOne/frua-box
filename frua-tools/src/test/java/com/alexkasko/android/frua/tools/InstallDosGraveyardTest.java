package com.helmetplusone.android.frua.tools;

import org.junit.Test;

import java.io.IOException;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
public class InstallDosGraveyardTest {

    @Test
    public void test() throws IOException {
        new InstallCaller().install("src/test/nonfree/dosgraveyard.com/unlimited.rar");
    }
}
