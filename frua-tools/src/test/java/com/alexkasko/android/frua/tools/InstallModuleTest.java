package com.helmetplusone.android.frua.tools;

import com.helmetplusone.android.frua.tools.Installer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * User: helmetplusone
 * Date: 3/31/13
 */
public class InstallModuleTest {
//    @Test
    public void testPlain() throws IOException {
        File dir = dir();
        File module = new File(dir, "src/main/res/raw/alliance_1_.zip");
        File target = new File(dir, "src/test/nonfree/frua");
        deleteDirectory(new File(target, "alliance.dsn"));
        new Installer().installModule(module, target);
    }

    @Test
    public void testInner() throws IOException {
        File dir = dir();
        File module = new File(dir, "src/main/res/raw/hos1.zip");
        File target = new File(dir, "src/test/nonfree/frua");
        deleteDirectory(new File(target, "hos1.dsn"));
        new Installer().installModule(module, target);
    }

    private File dir() {
        File cwd = new File(getProperty("user.dir"));
        File child = new File(cwd, "frua-tools");
        return child.exists() ? child : cwd;
    }
}
