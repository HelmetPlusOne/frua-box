package com.helmetplusone.android.frua.tools;

import com.helmetplusone.android.frua.tools.Switcher;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.lang.System.getProperty;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
public class SwitcherTest {
    @Test
    public void test() throws IOException {
        File cwd = new File(getProperty("user.dir"));
        File child = new File(cwd, "frua-tools");
        File dir = child.exists() ? child : cwd;
        File heirs = new File(dir, "src/test/nonfree/frua/HEIRS.DSN");
        File tutorial = new File(dir, "src/test/nonfree/frua/TUTORIAL.DSN");
        new Switcher().applyDesign(heirs);
        new Switcher().applyDesign(tutorial);
        new Switcher().applyDesign(heirs);
    }
}
