package com.helmetplusone.android.frua.tools;

import android.content.Context;
import com.helmetplusone.android.frua.R;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.helmetplusone.android.frua.tools.ArchiveUtils.unrarToFileReturnSha1;
import static com.helmetplusone.android.frua.tools.ArchiveUtils.unzipToDir;
import static com.helmetplusone.android.frua.tools.ArchiveUtils.writeToFileReturnSha1;
import static com.helmetplusone.android.frua.tools.CaseInsensitiveIoUtils.existingCi;
import static java.util.Locale.US;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

/**
* User: helmetplusone
* Date: 3/30/13
*/
public class Installer {
    private static final Logger logger = LoggerFactory.getLogger(Switcher.class);
    private static final String ANY_SHA1 = "__ANY__";
    private static final Pattern MODULE_NAME_REGEX = Pattern.compile("^.+\\.dsn$", Pattern.CASE_INSENSITIVE);
    public static final int MODULE_1_ID = R.raw.alliance_1_;
    public static final int MODULE_2_ID = R.raw.hos1;

    public void install(Context ctx, File source, File targetDir) throws IOException {
        install(source, targetDir, new RawOpenableResource(ctx, R.raw.source_files),
                new RawOpenableResource(ctx, R.raw.patch12), new RawOpenableResource(ctx, R.raw.patch13c),
                new RawOpenableResource(ctx, R.raw.settings), new RawOpenableResource(ctx, R.raw.installed_files));
    }

    public void installModule(Context context, int moduleId, File targetDir) throws IOException {
        installModule(new RawOpenableResource(context, moduleId), targetDir);
    }

    public void installModule(File module, File targetDir) throws IOException {
        if(!(module.exists() && module.isFile())) throw new IOException("Invalid module file: [" + module.getAbsolutePath() + "]");
        if(!(targetDir.exists() && targetDir.isDirectory())) throw new IOException("Invalid target directory: [" + targetDir.getAbsolutePath() + "]");
        logger.info("Installing module: [" + module.getAbsolutePath() + "]");
        installModule(new FileOpenableResource(module), targetDir);
    }

    void installModule(OpenableResource module, File targetDir) throws IOException {
        String longname = removeExtension(module.name());
        String name = longname.length() > 8 ? longname.substring(0, 8) : longname;
        File moduleDir = new File(targetDir, name + ".dsn");
        if(moduleDir.exists()) throw new IOException("Module directory: [" + moduleDir.getAbsolutePath() + "] already exists");
        unzipToDir(module, moduleDir);
        // check additional dirs
        for(File dir : moduleDir.listFiles((FileFilter) DIRECTORY)) {
            if(MODULE_NAME_REGEX.matcher(dir.getName()).matches()) {
                logger.info("Moving directory to upper level: [" + dir.getAbsolutePath() + "]");
                for(File fi : dir.listFiles((FileFilter) TRUE)) {
                    logger.debug("Moving file to upper level: [" + fi.getAbsolutePath() + "]");
                    boolean res = fi.renameTo(new File(moduleDir, fi.getName()));
                    if(!res) throw new IOException("Cannot move module file to upper directory: [" + fi.getAbsolutePath() + "]");
                }
                dir.delete();
            }
        }
    }

    void install(File source, File targetDir, OpenableResource filesList, OpenableResource patch12,
                        OpenableResource patch13, OpenableResource settings, OpenableResource installedList) throws IOException {
        if(!(source.exists() && source.isFile())) throw new IOException("Invalid input file: [" + source.getAbsolutePath() + "]");
        if(targetDir.exists()) {
            if(targetDir.isFile()) throw new IOException("Invalid target directory: [" + targetDir.getAbsolutePath() + "]");
            File[] children = targetDir.listFiles();
            if(null == children) throw new IOException("Invalid target directory: [" + targetDir.getAbsolutePath() + "]");
            if(children.length > 0) throw new IOException("Target directory: [" + targetDir.getAbsolutePath() + "] is not empty");
        }
        logger.info("Installing FRUA, source: [{}], target: [{}]", source.getAbsolutePath(), targetDir.getAbsolutePath());
        unpackFrua(source, targetDir, filesList);
        logger.info("FRUA unpacked");
        applyPatches(patch12, patch13, settings, targetDir);
        logger.info("Patches applied");
        validateInstall(targetDir, installedList);
        logger.info("Install validated");
        new Switcher().applyDesign(new File(targetDir, "HEIRS.DSN"));
        if(Thread.interrupted()) throw new IOException("Installation cancelled");
        logger.info("Install finished");
    }

    private void applyPatches(OpenableResource patch12, OpenableResource patch13, OpenableResource settings, File targetDir) throws IOException {
        unzipToDir(patch12, targetDir);
        unzipToDir(patch13, targetDir);
        File table = existingCi(targetDir, "diff.tbl");
        File ckit = existingCi(targetDir, "ckit.exe");
        new Patcher().applyTable(table, ckit);
        unzipToDir(settings, targetDir);
    }

    private void validateInstall(File dir, OpenableResource installedList) throws IOException {
        Map<String, String> files = filesList(installedList);
        for(Map.Entry<String, String> en : files.entrySet()) {
            if(Thread.interrupted()) throw new IOException("Installation cancelled");
            String name = en.getKey();
            String origSha1 = en.getValue();
            File file = existingCi(dir, name);
            String sha1 = Sha1OutputStream.fileSha1(file);
            if(!ANY_SHA1.equals(origSha1) && !sha1.equals(origSha1)) throw new IOException("Installed file: [" + file.getAbsolutePath() + "]," +
                    " sha1: [" + sha1 + "] differs from expected sha1: [" + origSha1 + "]");
        }
    }

    private void unpackFrua(File source, File targetDir, OpenableResource filesList) throws IOException {
        Map<String, String> files = filesList(filesList);
        String sha1 = Sha1OutputStream.fileSha1(source);
        if ("a07aa610923a8dc25740530252f124da556aca00".equals(sha1)) {
            // abandonia.com
            logger.debug("Installing FRUA from abandonia.com");
            unarchiveZip(source, targetDir, files, new Stripper.Abandonia());
        } else if ("68dad12d4ff54a74a92a887903d3458dd0c5a954".equals(sha1)) {
            // dosgraveyard.com
            logger.debug("Installing FRUA from dosgraveyard.com");
            unarchiveRar(source, targetDir, files, new Stripper.DosGraveyard());
//        } else if ("4ab09923f0876b15a68ac081c84b39edf0a495fd".equals(sha1)) {
////                emuparadise.me
        } else if ("cc9259b6ac7e6bb3ce751b26563cf721a54dc08c".equals(sha1)) {
            //  gameswin.biz
            logger.debug("Installing FRUA from gameswin.biz");
            unarchiveRar(source, targetDir, files, new Stripper.GamesWin());
//        } else if ("4d9392532aece4ed2ca6252e9ab51e99f9d4160c".equals(sha1)) {
////                myabandonware.com
        } else if ("a07aa610923a8dc25740530252f124da556aca00".equals(sha1)) {
            // oldschoolapps.com
            logger.debug("Installing FRUA from oldschoolapps.com");
            unarchiveZip(source, targetDir, files, new Stripper.OldSchoolApps());
        } else if ("7b95a310b6d49ff9f96af784b57c310eac443edd".equals(sha1)) {
            // xtcabandonware.com
            logger.debug("Installing FRUA from xtcabandonware.com");
            unarchiveZip(source, targetDir, files, new Stripper.XtcAbandonware());
        } else throw new IOException("Unsupported archive: [" + source.getAbsolutePath() + "], " +
                "supported abandonware sources are:\n" +
                " - abandonia.com\n" +
                " - dosgraveyard.com\n" +
//                " - emuparadise.me\n" +
                " - gameswin.biz\n" +
//                " - myabandonware.com\n" +
                " - oldschoolapps.com\n" +
                " - xtcabandonware.com\n" +
                "You also may install other copy of FRUA manually and specify its directory"
        );
    }

    private void unarchiveZip(File zip, File targetDir, Map<String, String> files, Stripper stripper) throws IOException {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(openInputStream(zip));
            ZipEntry en;
            while (null != (en = zis.getNextEntry())) {
                if(Thread.interrupted()) throw new IOException("Installation cancelled");
                if(en.isDirectory()) continue;
                String name = stripper.strip(en.getName()).toUpperCase(US);
                String sha1 = files.get(name);
                if(null == sha1) {
                    if(logger.isTraceEnabled()) logger.trace("Skipping archive entry: [{}]", name);
                    continue;
                }
                File target = new File(targetDir, name);
                String writtenSha1 = writeToFileReturnSha1(zis, target);
                target.setLastModified(en.getTime());
                if(!ANY_SHA1.equals(sha1) && !writtenSha1.equals(sha1)) throw new IOException("Error writing file: [" + target.getAbsolutePath() + "], sha1 sum differs");
                if(logger.isTraceEnabled()) logger.trace("File unpacked: [{}]", name);
            }
        } finally {
            closeQuietly(zis);
        }
    }

    private void unarchiveRar(File rar, File targetDir, Map<String, String> files, Stripper stripper) throws IOException {
        Archive arch = null;
        try {
            arch = new Archive(rar);
            FileHeader fh;
            while (null != (fh = arch.nextFileHeader())) {
                if(Thread.interrupted()) throw new IOException("Installation cancelled");
                if (fh.isDirectory()) continue;
                String name = stripper.strip(fh.getFileNameString()).toUpperCase(US).replace("\\", "/");
                String sha1 = files.get(name);
                if(null == sha1) {
                    if(logger.isTraceEnabled()) logger.trace("Skipping archive entry: [{}]", name);
                    ArchiveUtils.unrarSkip(arch, fh);
                    continue;
                }
                File target = new File(targetDir, name);
                String writtenSha1 = unrarToFileReturnSha1(arch, fh, target);
                target.setLastModified(fh.getMTime().getTime());
                if(!ANY_SHA1.equals(sha1) && !writtenSha1.equals(sha1)) throw new IOException("Error writing file: [" + target.getAbsolutePath() + "], sha1 sum differs");
                if(logger.isTraceEnabled()) logger.trace("File unpacked: [{}]", name);
            }
        } catch (RarException e) {
            throw new IOException(e);
        } finally {
            if (null != arch) try {arch.close();} catch (Exception e) {}
        }
    }

    private Map<String, String> filesList(OpenableResource filesList) throws IOException {
        LineIterator li = null;
        try {
            InputStream is = filesList.inputSteam();
            li = new LineIterator(new InputStreamReader(is));
            Map<String, String> res = new LinkedHashMap<String, String>();
            while (li.hasNext()) {
                if(Thread.interrupted()) throw new IOException("Installation cancelled");
                String line = li.next();
                String[] parts = line.split("  \\./");
                if(2 != parts.length) throw new IOException("Cannot read install files list");
                res.put(parts[1], parts[0]);
            }
            return res;
        } finally {
            LineIterator.closeQuietly(li);
        }
    }
}
