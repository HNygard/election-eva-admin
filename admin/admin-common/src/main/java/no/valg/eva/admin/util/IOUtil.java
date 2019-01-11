package no.valg.eva.admin.util;

import no.evote.exception.EvoteException;
import no.evote.util.EvoteProperties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class IOUtil {

    private static final Logger LOGGER = Logger.getLogger(IOUtil.class);
    private static final int MAX_TOKEN_SIZE = Integer.parseInt(EvoteProperties.getProperty("token.max_size", "1000"));
    private static final int KILO_BYTE = 1024;
    private static final String RELATIVE_PATH = "../";


    private IOUtil() {
    }

    public static byte[] getBytes(final InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;

        byte[] data = new byte[16384];


        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] bytes = buffer.toByteArray();

        buffer.close();

        return bytes;
    }

    public static byte[] getBytes(final File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = getBytes(inputStream);
        inputStream.close();
        return bytes;
    }

    public static File makeFile(final String path, final byte[] bytes, final String name) throws IOException {
        final File file = new File(path + File.separator + name);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            fos.flush();
            return file;
        }
    }

    public static File makeFileInTemporaryDirectory(final String name) {
        return new File(createTemporaryDirectory() + File.separator + name);
    }

    public static File makeFile(final byte[] bytes, final String name) throws IOException {
        File file = new File(createTemporaryDirectory() + File.separator + name);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            fos.flush();
        }

        return file;
    }

    public static File makeFileWithFullPath(final byte[] bytes, final String name) throws IOException {
        File file = new File(name);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            fos.flush();
        }

        return file;
    }

    public static Map<String, InputStream> unzipTokenAndCheckSize(InputStream zipStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(zipStream);
        Map<String, InputStream> fileStreams = new HashMap<>();
        ZipEntry zipEntry;

        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            LOGGER.debug("size zip-entry er " + zipEntry.getSize());
            InputStream inputStream = makeTokenInputStream(zipEntry, zipInputStream);
            LOGGER.debug("size zip-entry etter makeTokenInputStream-kall " + zipEntry.getSize());
            if (inputStream != null) {
                fileStreams.put(zipEntry.getName().replace("\\", "/"), inputStream);
            }
        }
        return fileStreams;
    }

    public static Map<String, File> unZip(final File temporaryDirectory, final File zipFile) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(zipFile)) {
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            Map<String, File> files = new HashMap<>();
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File file = makeFile(temporaryDirectory, zipEntry, zipInputStream);
                if (file != null) {
                    files.put(escapeRelativePath(zipEntry.getName()).replace("\\", "/"), file);
                }
            }
            return files;
        }
    }

    public static File createTemporaryDirectory() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        Random rand = new Random();
        int randomInt = 1 + rand.nextInt();

        File tempDir = new File(baseTempPath + File.separator + "tempDir" + randomInt);
        if (!tempDir.exists() && !tempDir.mkdir()) {
            throw new EvoteException("Unable to create temporary directory.");
        }

        tempDir.deleteOnExit();

        LOGGER.debug("Created directory " + tempDir.getAbsolutePath());

        return tempDir;
    }

    private static InputStream makeTokenInputStream(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
        if (zipEntry.isDirectory()) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(out)) {

            byte[] buffer = new byte[KILO_BYTE];

            int read;
            int total = 0;
            while ((read = zipInputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, read);
                total += read;
                if (total > MAX_TOKEN_SIZE * KILO_BYTE) {
                    throw new IllegalStateException("File being unzipped is too big.");
                }
            }
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static File makeFile(final File tempDirectory, final ZipEntry zipEntry, final ZipInputStream zipInputStream) throws IOException {
        File file = null;
        String prefix = tempDirectory.getAbsolutePath() + File.separator;

        final String zipEntryName = escapeRelativePath(zipEntry.getName());

        if (zipEntry.isDirectory()) {
            if (!(new File(prefix + zipEntryName)).mkdir()) {
                LOGGER.error("failed to make " + zipEntryName);
            }
        } else {
            file = new File(prefix + zipEntryName);
            File parent = file.getParentFile();
            while (parent != null) {
                if (!parent.exists() && !parent.mkdir()) {
                    LOGGER.error("failed to make " + parent.getAbsolutePath());
                }
                parent = parent.getParentFile();
            }

            try (OutputStream outStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[KILO_BYTE];

                int read;
                while ((read = zipInputStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, read);
                }
            }
        }
        return file;
    }

    private static String escapeRelativePath(String path) {
        if (path.contains(RELATIVE_PATH)) {
            final String escaped = path.replaceAll(RELATIVE_PATH, "");
            LOGGER.warn("File name contained illegal relative path: " + path);
            LOGGER.warn("Using escaped path: " + escaped);
            return escaped;
        }
        return path;
    }

    public static void deleteContainingFolder(final File file) {
        if (file == null) {
            return;
        }

        deleteDirectory(file.getParentFile());
    }

    public static void deleteDirectory(final File directory) {
        if (directory == null) {
            return;
        }

        try {
            FileUtils.deleteDirectory(directory);
            LOGGER.debug("Deleted directory " + directory.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.warn("Unable to delete " + directory.getParentFile(), e);
        }
    }

    public static byte[] getBytes(final String fileName) throws IOException {
        return getBytes(new File(fileName));
    }
}
