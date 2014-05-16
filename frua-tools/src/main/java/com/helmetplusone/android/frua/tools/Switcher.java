package com.helmetplusone.android.frua.tools;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.List;

import static com.helmetplusone.android.frua.tools.CaseInsensitiveIoUtils.*;
import static java.util.Locale.US;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.filefilter.FalseFileFilter.FALSE;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
public class Switcher {
    private static final Logger logger = LoggerFactory.getLogger(Switcher.class);

    private static final Pattern DQK_XMI_PATTERN = Pattern.compile("^dqk(?<num>\\d)\\.xmi$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final Pattern ITEM_UAT_PATTERN = Pattern.compile("^item\\.uat$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final Pattern ITEMS_UAT_PATTERN = Pattern.compile("^items\\.uat$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final Pattern CBODY_UAT_PATTERN = Pattern.compile("^cbody\\.uat$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final IOFileFilter DISK1_FILES = new RegexFileFilter("^" +
            "instr\\.ad|" +
            "addq\\d\\.xmi|" +
            "pcdq\\d\\.xmi|" +
            "rodq\\d\\.xmi|" +
            "item\\.dat|" +
            "item\\.uat|" +
            "items\\.dat|" +
            "items\\.uat|" +
            "game\\.fon|" +
            "sfxdq\\.voc|" +
            "dqk\\d\\.xmi|" +
            "dqk\\d\\.xmi|" +
            "always.tlb|" +
            "title.tlb" +
            "$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final IOFileFilter DISK2_FILES = new RegexFileFilter("^" +
            "8x8d[bc]\\.tlb|" +
            "back\\.tlb|" +
            "bigpi[cx]\\.tlb|" +
            "comspr\\.tlb|" +
            "cpic\\.tlb|" +
            "dungcom\\.tlb|" +
            "monst\\.glb|" +
            "pic[a-f]\\.tlb|" +
            "sprit\\.tlb|" +
            "topview\\.tlb|" +
            "wildcom\\.tlb" +
            "$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final IOFileFilter DISK3_FILES = new RegexFileFilter("^" +
            "cbody\\.tlb|" +
            "cbody\\.uat|" +
            "frame\\.tlb|" +
            "game\\.glb|" +
            "gen.tlb|" +
            "geo\\.glb|" +
            "menu\\.tlb|" +
            "script\\.glb|" +
            "strg\\.glb" +
            "$", java.util.regex.Pattern.CASE_INSENSITIVE);

    public void applyDesign(File designDsn) throws IOException {
        if (!(designDsn.exists() && designDsn.isDirectory())) throw new IOException(
                "Invalid design designDsn: [" + designDsn.getAbsolutePath() + "]");
        File fruaRoot = designDsn.getParentFile();
        // check consistency
        File ckitExe = existingCi(fruaRoot, "ckit.exe");
        File disk1 = existingCi(fruaRoot, "disk1");
        File disk2 = existingCi(fruaRoot, "disk2");
        File disk3 = existingCi(fruaRoot, "disk3");
        // check default.dsn exists, create it if not
        File defaultDsnOrNull = existingCiOrNull(fruaRoot, "default.dsn");
        File defaultDsn = null != defaultDsnOrNull ? defaultDsnOrNull : createDefaultDsn(fruaRoot, ckitExe, disk1, disk2, disk3);
        // apply default first
        if(designDsn.equals(defaultDsn)) {
            logger.debug("Copying panic.xxx");
            copyFile(existingCi(defaultDsn, "panic.xxx"), ckitExe);
        } else {
            applyDesign(defaultDsn);
        }
        logger.info("Applying design: [{}], FRUA root: [{}]", designDsn, fruaRoot.getAbsolutePath());
        // apply table
        File diffTbl = existingCiOrNull(designDsn, "diff.tbl");
        if (null != diffTbl && diffTbl.isFile()) new Patcher().applyTable(diffTbl, ckitExe);
        // disk1
        copyDisk1(designDsn, disk1);
        // disk2
        copyDisk2(designDsn, disk2);
        // disk3
        copyDisk3(designDsn, disk3);
        // start.dat
        writeStartDat(fruaRoot, designDsn.getName());
        // check save dir
        checkSaveDir(designDsn);
        logger.info("Finished");
    }

    private void checkSaveDir(File designDsn) throws IOException {
        File saveExists = existingCiOrNull(designDsn, "save");
        if (null == saveExists) {
            File save = new File(designDsn, "save");
            logger.info("Creating directory: [" + save.getAbsolutePath() + "]");
            boolean success = save.mkdir();
            if(!success) throw new IOException("Cannot create dir: [" + save.getAbsolutePath() + "]");
        }
    }

    private File createDefaultDsn(File fruaRoot, File ckitExe, File disk1, File disk2, File disk3) throws IOException {
        logger.info("Creating default.dsn");
        File defaultDsn = new File(fruaRoot, "default.dsn");
        logger.info("Copying ckit.exe to default.dsn");
        copyFile(ckitExe, new File(defaultDsn, "panic.xxx"));
        logger.info("Copying disk1 contents to default.dsn");
        copyDirectory(disk1, defaultDsn);
        logger.info("Copying disk2 contents to default.dsn");
        copyDirectory(disk2, defaultDsn);
        logger.info("Copying disk3 contents to default.dsn");
        copyDirectory(disk3, defaultDsn);
        if(Thread.interrupted()) throw new IOException("Installation cancelled");
        return defaultDsn;
    }

    @SuppressWarnings("unchecked") // listFiles API
    private void copyDisk1(File designDsn, File disk1) throws IOException {
        logger.info("Copying files to disk1");
        List<File> disk1List = (List) listFiles(designDsn, DISK1_FILES, FALSE);
        Collections.sort(disk1List);
        for (File fi : disk1List) {
            if(Thread.interrupted()) throw new IOException("Installation cancelled");
            Matcher dqkMatcher = DQK_XMI_PATTERN.matcher(fi.getName());
            Matcher itemMatcher = ITEM_UAT_PATTERN.matcher(fi.getName());
            Matcher itemsMatcher = ITEMS_UAT_PATTERN.matcher(fi.getName());
            if (dqkMatcher.matches()) {
                int num = Integer.parseInt(dqkMatcher.group("num"));
                File target1 = existingCi(disk1, "addq" + num + ".xmi");
                logger.debug("Copying file: [{}] to : [{}]", fi, target1);
                copyFileCi(fi, target1);
                File target2 = existingCi(disk1, "pcdq" + num + ".xmi");
                logger.debug("Copying file: [{}] to : [{}]", fi, target2);
                copyFileCi(fi, target2);
                File target3 = existingCi(disk1, "rodq" + num + ".xmi");
                logger.debug("Copying file: [{}] to : [{}]", fi, target3);
                copyFileCi(fi, target3);
            } else if (itemMatcher.matches()) {
                File target = existingCi(disk1, "item.dat");
                logger.debug("Copying file: [{}] to : [{}]", fi, target);
                copyFileCi(fi, target);
            } else if (itemsMatcher.matches()) {
                File target = existingCi(disk1, "items.dat");
                logger.debug("Copying file: [{}] to : [{}]", fi, target);
                copyFileCi(fi, target);
            } else {
                File target = existingCi(disk1, fi.getName());
                logger.debug("Copying file: [{}] to : [{}]", fi, target);
                copyFileCi(fi, target);
            }
        }
    }

    @SuppressWarnings("unchecked") // listFiles API
    private void copyDisk2(File designDsn, File disk2) throws IOException {
        logger.info("Copying files to disk2");
        List<File> disk2List = (List) listFiles(designDsn, DISK2_FILES, FALSE);
        Collections.sort(disk2List);
        for (File fi : disk2List) {
            if(Thread.interrupted()) throw new IOException("Installation cancelled");
            File target = existingCi(disk2, fi.getName());
            logger.debug("Copying file: [{}] to : [{}]", fi, target);
            copyFileCi(fi, target);
        }
    }

    @SuppressWarnings("unchecked") // listFiles API
    private void copyDisk3(File designDsn, File disk3) throws IOException {
        logger.info("Copying files to disk3");
        List<File> disk3List = (List) listFiles(designDsn, DISK3_FILES, FALSE);
        Collections.sort(disk3List);
        for (File fi : disk3List) {
            if(Thread.interrupted()) throw new IOException("Installation cancelled");
            Matcher cbodyMatcher = CBODY_UAT_PATTERN.matcher(fi.getName());
            String name = cbodyMatcher.matches() ? "cbody.tlb" : fi.getName();
            File target = existingCi(disk3, name);
            logger.debug("Copying file: [{}] to : [{}]", fi, target);
            copyFile(fi, target);
        }
    }

    private void writeStartDat(File fruaRoot, String designName) throws IOException {
        logger.info("Updating start.dat");
        RandomAccessFile raf = null;
        try {
            String name = designName.toUpperCase(US) + "\0";
            File startDat = existingCi(fruaRoot, "start.dat");
            raf = new RandomAccessFile(startDat, "rw");
            raf.write(name.getBytes(ASCII_CHARSET));
        } finally {
            closeQuietly(raf);
        }
    }
}
