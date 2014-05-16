package com.helmetplusone.android.frua.tools;

import org.apache.commons.io.output.NullOutputStream;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.nio.charset.Charset;

import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;

/**
 * User: helmetplusone
 * Date: 1/27/13
 */
class Sha1OutputStream extends OutputStream {
    private static final Charset ASCII = Charset.forName("ASCII");

    private final Digest sha1 = new SHA1Digest();
    private byte[] digest;
    private final OutputStream target;

    Sha1OutputStream(OutputStream target) {
        this.target = target;
    }

    static String fileSha1(File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = openInputStream(file);
            Sha1OutputStream sha1 = new Sha1OutputStream(new NullOutputStream());
            copyLarge(fis, sha1);
            return sha1.digest();
        } finally {
            closeQuietly(fis);
        }
    }

    @Override
    public void write(int b) throws IOException {
        sha1.update((byte) b);
        target.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        sha1.update(b, off, len);
        target.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        target.flush();
    }

    @Override
    public void close() throws IOException {
        target.close();
    }

    public byte[] digestBytes() {
        if (null == digest) {
            digest = new byte[sha1.getDigestSize()];
            sha1.doFinal(digest, 0);
        }
        return digest;
    }

    public String digest() {
        byte[] dig = digestBytes();
        byte[] hex = Hex.encode(dig);
        return new String(hex, ASCII);
    }
}
