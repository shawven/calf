package com.github.shawven.calf.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 压缩工具
 *
 * @author xw
 * @date 2023/3/7
 */
public class CompressUtils {

    public static byte[] zip(File[] sourceFiles) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        compress(sourceFiles, new ZipArchiveOutputStream(bos));
        return bos.toByteArray();
    }

    public static File zip(File[] sourceFiles, String targetName) throws IOException {
        File targetFile = new File(targetName);
        compress(sourceFiles, new ZipArchiveOutputStream(targetFile));
        return targetFile;
    }

    public static void unzip(String targetDir, InputStream is) throws IOException {
        uncompress(targetDir, new ZipArchiveInputStream(is));
    }

    public static void unzip(String targetDir, File file) throws IOException {
        uncompress(targetDir, new ZipArchiveInputStream(Files.newInputStream(file.toPath())));
    }

    private static void compress(File[] files, ArchiveOutputStream out) throws IOException {
        try {
            for (File file : files) {
                String basePath = file.getParent();
                if (file.isDirectory()) {
                    compress(file, basePath, out);
                } else {
                    compressFile(file, basePath, out);
                }
            }
            out.closeArchiveEntry();
            out.finish();
        } finally {
            out.close();
        }
    }

    private static void compress(File file, String basePath, ArchiveOutputStream out) throws IOException {
        if (file.isDirectory()) {
            String name = getName(file, basePath);
            out.putArchiveEntry(out.createArchiveEntry(file, name));
            File[] fs = file.listFiles();
            for (File f : fs) {
                compress(f, basePath, out);
            }
        } else {
            compressFile(file, basePath, out);
        }
    }

    private static void compressFile(File file, String basePath, ArchiveOutputStream out) throws IOException {
        String name = getName(file, basePath);
        out.putArchiveEntry(out.createArchiveEntry(file, name));
        try (InputStream i = Files.newInputStream(file.toPath())) {
            IOUtils.copy(i, out);
        }
    }

    private static String getName(File file, String basePath) {
        String name = StringUtils.substringAfter(file.getPath(), basePath);
        name = StringUtils.stripStart(name, "/\\");
        return name;
    }

    private static void uncompress(String targetDir, ArchiveInputStream ais) throws IOException {
        try  {
            ArchiveEntry entry = null;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) {
                    // log something?
                    continue;
                }
                File file = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        throw new IOException("failed to create directory " + file);
                    }
                } else {
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(ais));
                }
            }
        } finally {
            ais.close();
        }
    }
}
