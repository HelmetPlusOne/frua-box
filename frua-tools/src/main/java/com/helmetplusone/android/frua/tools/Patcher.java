package com.helmetplusone.android.frua.tools;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static com.helmetplusone.android.frua.tools.CaseInsensitiveIoUtils.ASCII_CHARSET;
import static java.util.Locale.US;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
class Patcher {
    private final Logger logger = LoggerFactory.getLogger(Patcher.class);
    private static final Pattern TLD_LINE_PATTERN = Pattern.compile("^(?<offset>[0-9a-fA-F]+) [0-9a-fA-F]{1,2} (?<value>[0-9a-fA-F]{1,2})$");

    public void applyTable(File tbl, File target) throws IOException {
        logger.info("Applying tableto file: [{}]", target.getAbsolutePath());
        LineIterator li = null;
        RandomAccessFile raf = null;
        try {
            li = new LineIterator(new InputStreamReader(openInputStream(tbl), ASCII_CHARSET));
            raf = new RandomAccessFile(target, "rw");
            int lc = 0;
            while (li.hasNext()) {
                if(Thread.interrupted()) throw new IOException("Installation cancelled");
                String line = li.next();
                if ("0".equals(line)) break;
                Matcher ma = TLD_LINE_PATTERN.matcher(line);
                if (!ma.matches()) {
                    logger.warn("Warning: line ignored: [{}]", line);
                    continue;
                }
                int offset = Integer.parseInt(ma.group("offset"), 16);
                int value = Integer.parseInt(ma.group("value"), 16);
                raf.seek(offset);
                raf.write(value);
                lc += 1;
            }
            logger.info("Table was applied successfully to file: [{}], changes count: [{}]", target.getAbsolutePath(), lc);
        } finally {
            LineIterator.closeQuietly(li);
            closeQuietly(raf);
        }
    }

    public void createTable(File origFile, File changedFile, File tblFile) throws IOException {
        logger.info("Writing tbl: [{}], original: [{}], changed: [{}]",
                new Object[]{tblFile.getAbsolutePath(), origFile.getAbsolutePath(), changedFile.getAbsolutePath()});
        if (origFile.length() != changedFile.length()) {
            logger.error("Cannot create diff.tbl for original: [{}], changed: [{}] - sizes are different",
                    origFile.getAbsolutePath(), changedFile.getAbsolutePath());
            return;
        }
        if (tblFile.exists()) {
            logger.error("Table file: [" + tblFile.getAbsolutePath() + "] is already exist");
            return;
        }
        InputStream orig = null;
        InputStream changed = null;
        Writer tbl = null;
        try {
            orig = new BufferedInputStream(openInputStream(origFile));
            changed = new BufferedInputStream(openInputStream(changedFile));
            tbl = new BufferedWriter(new OutputStreamWriter(openOutputStream(tblFile), ASCII_CHARSET));
            int counter = 0;
            for (int i = 0; i < origFile.length(); i++) {
                if(Thread.interrupted()) throw new IOException("Installation cancelled");
                int origByte = orig.read();
                int changedByte = changed.read();
                if (origByte != changedByte) {
                    tbl.write(Integer.toHexString(i).toUpperCase(US));
                    tbl.write(" ");
                    tbl.write(Integer.toHexString(origByte).toUpperCase(US));
                    tbl.write(" ");
                    tbl.write(Integer.toHexString(changedByte).toUpperCase(US));
                    tbl.write("\r\n");
                    counter += 1;
                }
            }
            tbl.write("0\r\n");
            logger.info("Tbl written: [{}], changes count: [{}]", tblFile.getAbsolutePath(), counter);
        } finally {
            closeQuietly(orig);
            closeQuietly(changed);
            closeQuietly(tbl);
        }
    }
}
