package com.helmetplusone.android.frua;

/**
 * User: helmetplusone
 * Date: 12/23/13
 */
// todo: assets
public class FruaBoxConf {
    public static String CONF = "[dosbox]\n" +
            "machine=svga_s3\n" +
            "\n" +
            "[render]\n" +
            "#frameskip=0\n" +
            "\n" +
            "[cpu]\n" +
            "core=dynamic\n" +
            "cputype=auto\n" +
            "#cycles=auto 1500 100% limit 3500\n" +
            "\n" +
            "[mixer]\n" +
            "blocksize=1024\n" +
            "prebuffer=10\n" +
            "rate=22050\n" +
            "\n" +
            "[midi]\n" +
            "mpu401=none\n" +
            "mididevice=none\n" +
            "midiconfig=\n" +
            "\n" +
            "[speaker]\n" +
            "pcspeaker=true\n" +
            "pcrate=11025\n" +
            "tandy=off\n" +
            "disney=false\n" +
            "\n" +
            "[sblaster]\n" +
            "sbtype=sb2\n" +
            "sbmixer=true\n" +
            "oplmode=opl2\n" +
            "oplemu=fast\n" +
            "oplrate=22050\n" +
            "\n" +
            "[autoexec]\n" +
            "@Echo Off\n" +
            "mount c: {{fruapath}}\n" +
            "c:\n" +
            "start\n";
}
