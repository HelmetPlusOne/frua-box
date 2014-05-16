package com.helmetplusone.android.frua.tools;

import org.junit.Test;

import java.io.IOException;


/**
 * User: helmetplusone
 * Date: 3/30/13
 */
public class InstallAbandoniaTest {

//    @Test
    public void dummy() {

    }

    @Test
    public void test() throws IOException {
        new InstallCaller().install("src/test/nonfree/abandonia.com/Forgotten Realms - Unlimited Adventures.zip");
    }
}
