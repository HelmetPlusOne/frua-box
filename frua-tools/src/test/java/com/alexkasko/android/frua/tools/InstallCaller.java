package com.helmetplusone.android.frua.tools;

import com.helmetplusone.android.frua.tools.FileOpenableResource;
import com.helmetplusone.android.frua.tools.Installer;
import com.helmetplusone.android.frua.tools.OpenableResource;

import java.io.File;
import java.io.IOException;

import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
public class InstallCaller {
    public void install(String path) throws IOException {
        File cwd = new File(getProperty("user.dir"));
        File child = new File(cwd, "frua-tools");
        File dir = child.exists() ? child : cwd;
        File source = new File(dir, path);
        File res = new File(dir, "src/main/res/raw");
        OpenableResource filesList = new FileOpenableResource(new File(res, "source_files.txt"));
        OpenableResource installedList = new FileOpenableResource(new File(res, "installed_files.txt"));
        File target = new File(dir, "src/test/nonfree/frua");
        deleteDirectory(target);
        OpenableResource patch12 = new FileOpenableResource(new File(res, "patch12.zip"));
        OpenableResource patch13 = new FileOpenableResource(new File(res, "patch13c.zip"));
        OpenableResource settings = new FileOpenableResource(new File(res, "settings.zip"));
        new Installer().install(source, target, filesList, patch12, patch13, settings, installedList);
    }
}
