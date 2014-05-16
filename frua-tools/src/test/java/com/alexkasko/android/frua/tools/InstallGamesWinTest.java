package com.helmetplusone.android.frua.tools;

import org.junit.Test;

import java.io.IOException;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
public class InstallGamesWinTest {
    @Test
    public void test() throws IOException {
        new InstallCaller().install("src/test/nonfree/gameswin.biz/forgotten_ua.rar");
    }
}
