package com.helmetplusone.android.frua.tools;

import com.helmetplusone.android.frua.tools.Patcher;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.helmetplusone.android.frua.tools.CaseInsensitiveIoUtils.ASCII_CHARSET;
import static junit.framework.Assert.assertEquals;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

/**
 * User: helmetplusone
 * Date: 3/30/13
 */
public class PatcherTest {
    private static final byte[] SOURCE = {1, 42, 2, 43, 3};
    private static final byte[] TARGET = {1, 44, 2, 45, 3};

    @Test
    public void test() throws IOException {
        File source = File.createTempFile(PatcherTest.class.getName(), "source");
        source.deleteOnExit();
        File target = File.createTempFile(PatcherTest.class.getName(), "source");
        target.deleteOnExit();
        File table = File.createTempFile(PatcherTest.class.getName(), "table");
        table.deleteOnExit();
        table.delete();

        writeByteArrayToFile(source, SOURCE);
        writeByteArrayToFile(target, TARGET);

        Patcher patcher = new Patcher();
        patcher.createTable(source, target, table);
        String tableStr = FileUtils.readFileToString(table, ASCII_CHARSET);
        assertEquals("Create table fail", "1 2A 2C\r\n3 2B 2D\r\n0\r\n", tableStr);

        patcher.applyTable(table, source);
        byte[] patched = FileUtils.readFileToByteArray(source);
        Assert.assertArrayEquals("Apply table fail", TARGET, patched);
    }
}
