package com.helmetplusone.android.frua.tools;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.IOUtils.copyLarge;

/**
 * User: helmetplusone
 * Date: 1/27/13
 */
class ArchiveUtils {
    static void writeToFile(InputStream st, File fi) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = openOutputStream(fi);
            copyLarge(st, fos);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    static String writeToFileReturnSha1(InputStream st, File fi) throws IOException {
        Sha1OutputStream sha1 = null;
        try {
            FileOutputStream fos = openOutputStream(fi);
            sha1 = new Sha1OutputStream(fos);
            copyLarge(st, sha1);
            return sha1.digest();
        } finally {
            IOUtils.closeQuietly(sha1);
        }
    }

    static void unzipToDir(OpenableResource res, File dir) throws IOException {
        ZipInputStream zis = null;
        try {
            ZipEntry en;
            zis = new ZipInputStream(res.inputSteam());
            while (null != (en = zis.getNextEntry())) {
                if(Thread.interrupted()) throw new IOException("Installation cancelled");
                if (en.isDirectory()) continue;
                File target = new File(dir, en.getName());
                writeToFile(zis, target);
                target.setLastModified(en.getTime());
            }
        } finally {
            IOUtils.closeQuietly(zis);
        }
    }

//    static void unrarToDir(File rar, File dir) throws IOException, RarException {
//        Archive arch = null;
//        try {
//            arch = new Archive(rar);
//            unrarToDir(arch, dir);
//        } finally {
//            if (null != arch) try {arch.close();} catch (Exception e) {}
//        }
//    }
//
//    static void unrarToDir(Archive arch, File dir) throws IOException, RarException {
//        FileHeader fh;
//        while (null != (fh = arch.nextFileHeader())) {
//            if (fh.isDirectory()) continue;
//            File target = new File(dir, fh.getFileNameString());
//            unrarToFile(arch, fh, target);
//        }
//    }

    static void unrarSkip(Archive arch, FileHeader fh) throws IOException, RarException {
        arch.extractFile(fh, new NullOutputStream());
    }

    static String unrarToFileReturnSha1(Archive arch, FileHeader fh, File target) throws IOException, RarException {
        Sha1OutputStream sha1 = null;
        try {
            FileOutputStream fos = openOutputStream(target);
            sha1 = new Sha1OutputStream(fos);
            arch.extractFile(fh, sha1);
            return sha1.digest();
        } finally {
            IOUtils.closeQuietly(sha1);
        }
    }
}
