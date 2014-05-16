package com.helmetplusone.android.frua.tools;

import java.io.*;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
class FileOpenableResource implements OpenableResource {
    private final File file;

    public FileOpenableResource(File file) {
        if(null == file) throw new IllegalArgumentException("Provided file is null");
        this.file = file;
    }

    @Override
    public InputStream inputSteam() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public String name() {
        return file.getName();
    }
}
