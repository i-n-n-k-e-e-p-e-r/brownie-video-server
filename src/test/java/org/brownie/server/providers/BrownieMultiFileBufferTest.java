package org.brownie.server.providers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class BrownieMultiFileBufferTest {

    @BeforeAll
    @Test
    static void testConstructor() {
        BrownieMultiFileBuffer buffer = new BrownieMultiFileBuffer(new BrownieUploadsFileFactory());
        assertNotNull(buffer);
        assertNotNull(buffer.getTempFilesFactory());
    }

    @Test
    public void testCreateAndDeleteTempFile() {
        //Test creation
        BrownieMultiFileBuffer buffer = new BrownieMultiFileBuffer(new BrownieUploadsFileFactory());
        String fileName = "foo.txt";

        assertNotNull(buffer.receiveUpload(fileName, "text/plain"));
        assertEquals(1, buffer.getFiles().size());
        assertEquals(1, buffer.getTempFilesFactory().getTempFiles().size());
        assertNotNull(buffer.getTempFile(fileName));
        assertTrue(buffer.getTempFile(fileName).exists());

        // Tests file data, descriptor and stream
        assertNotNull(buffer.getFileDescriptor(fileName));
        assertNotNull(buffer.getFileData(fileName));
        InputStream stream = buffer.getInputStream(fileName);
        assertNotNull(stream);
        assertTrue(stream instanceof java.io.FileInputStream);

        // Test deletion
        try {
           stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assertDoesNotThrow(() -> buffer.removeFile(fileName));
            assertTrue(buffer.getFiles().isEmpty());
            assertTrue(buffer.getTempFilesFactory().getTempFiles().isEmpty());
            assertNull(buffer.getTempFile(fileName));
        }
    }

    @Test
    public void testDeleteTempFile2() {
        BrownieMultiFileBuffer buffer = new BrownieMultiFileBuffer(new BrownieUploadsFileFactory());
        assertDoesNotThrow(() -> buffer.deleteTempFile(null));
    }

    @Test
    public void testGetFileData() {
        assertNull((new BrownieMultiFileBuffer(new BrownieUploadsFileFactory())).getFileData("foo.txt"));
    }

    @Test
    public void testGetFileDescriptor() {
        assertNull((new BrownieMultiFileBuffer(new BrownieUploadsFileFactory())).getFileDescriptor("foo.txt"));
    }

    @Test
    public void testGetFiles() {
        assertTrue((new BrownieMultiFileBuffer(new BrownieUploadsFileFactory())).getFiles().isEmpty());
    }

    @Test
    public void testGetInputStream() {
        assertTrue((new BrownieMultiFileBuffer(new BrownieUploadsFileFactory()))
                .getInputStream("foo.txt") instanceof java.io.ByteArrayInputStream);
    }
}

