package no.valg.eva.admin.util;

import no.valg.eva.admin.test.BaseTakeTimeTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class IOUtilTest extends BaseTakeTimeTest {

    @Test
    public void containingDirectoryShouldBeDeleted() throws IOException {
        // Create directory
        File directory = IOUtil.createTemporaryDirectory();

        // Create file in directory
        File file = new File(directory.getAbsolutePath() + File.separator + "foobar");
        file.createNewFile();

        Assert.assertTrue(directory.exists());
        Assert.assertTrue(file.exists());

        IOUtil.deleteContainingFolder(file);

        Assert.assertFalse(directory.exists());
        Assert.assertFalse(file.exists());
        Assert.assertTrue(directory.getParentFile().exists());
    }

    @Test
    public void directoryShouldBeDeleted() throws IOException {
        // Create directory
        File directory = IOUtil.createTemporaryDirectory();
        Assert.assertTrue(directory.exists());
        IOUtil.deleteDirectory(directory);
        Assert.assertFalse(directory.exists());
    }

    @Test
    public void shouldCreateFileWithFullPath() throws IOException {
        long time = System.currentTimeMillis();
        File file = IOUtil.makeFileWithFullPath(new byte[]{}, System.getProperty("java.io.tmpdir") + File.separator + "test" + time);
        Assert.assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void shouldSupportNordicCharacters() throws IOException {
        String text = "blåbærsyltetøy";
        Charset utf8 = Charset.forName("utf-8");
        File file = IOUtil.makeFileWithFullPath(text.getBytes(utf8), System.getProperty("java.io.tmpdir") + File.separator + "test" + System.currentTimeMillis());
        byte[] bytes = IOUtil.getBytes(file);
        assertEquals(new String(bytes, utf8), text);
        file.delete();
    }

    @Test
    public void testUnzipDoesEscapeRelativePaths_inArchiveEntries() throws IOException {

        final List<String> filenames = asList("vdir-payroll.txt", "my-big-fat-greek-evil-plan.bat", "my-big-fat-greek-evil-plan-2__down-by-the-pier.dll", "fregatt-paa-land.jpg");

        File zipArchive = zipToArchive("archive.zip",
                createTmpFileWithContent(filenames.get(0), "Det kan du vel tro, at det finnes ikke noen Gruffalo."),
                createTmpFileWithContent(filenames.get(1), "Tralala"),
                createTmpFileWithContent(filenames.get(2), "The longer tralala"),
                createTmpFileWithContent(filenames.get(3), "Her burde det vel vært noe bildedata..")
        );

        File destinationDir = IOUtil.createTemporaryDirectory();
        IOUtil.unZip(destinationDir, zipArchive);

        final List<String> actualFilenames = asList(destinationDir.list());
        assertEquals(filenames.stream()
                        .filter(actualFilenames::contains)
                        .collect(Collectors.toList())
                        .size(),
                filenames.size());
    }

    private File zipToArchive(String archiveName, File... files) throws IOException {

        final File zipArchive = createTmpFile(archiveName);
        final FileOutputStream fos = new FileOutputStream(zipArchive);
        final ZipOutputStream zipStream = new ZipOutputStream(fos);

        int parentLevel = 0;
        for (File file : files) {
            writeToZipStream(file, zipStream, parentLevel++);
        }
        zipStream.close();
        fos.close();

        return zipArchive;
    }

    private File createTmpFileWithContent(String filename, String content) throws IOException {
        File file = createTmpFile(filename);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes(), 0, content.length());
        fos.close();
        return file;
    }

    private File createTmpFile(String filename) {
        return new File(IOUtil.createTemporaryDirectory().getAbsolutePath() + File.separator + filename);
    }

    private void writeToZipStream(File file, ZipOutputStream zipStream, int levelRelativeToParent) throws IOException {

        String relativePath = "";
        for (int i = 0; i < levelRelativeToParent; i++) {
            relativePath += "../";
        }

        FileInputStream fis = new FileInputStream(file);
        ZipEntry ze = new ZipEntry(relativePath + file.getName());
        zipStream.putNextEntry(ze);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipStream.write(bytes, 0, length);
        }
        zipStream.closeEntry();
        fis.close();
    }
}
