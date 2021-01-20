package org.brownie.server.providers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class MediaDirectoriesTest {

    @BeforeAll
    @Test
    static void setup() {
        assertDoesNotThrow(() -> MediaDirectories.initDirectories(null));
        assertTrue(MediaDirectories.mediaDirectory.exists());
        assertTrue(MediaDirectories.uploadsDirectory.exists());
    }

    @Test
    void testInitDirectories() {
        assertDoesNotThrow(() -> MediaDirectories.initDirectories(null));
        assertDoesNotThrow(() -> FileSystemDataProvider.deleteFileOrDirectory(MediaDirectories.mediaDirectory));
        assertDoesNotThrow(() -> FileSystemDataProvider.deleteFileOrDirectory(MediaDirectories.uploadsDirectory));
        assertDoesNotThrow(() -> MediaDirectories.initDirectories(null));
        assertTrue(MediaDirectories.mediaDirectory.exists());
        assertTrue(MediaDirectories.uploadsDirectory.exists());
    }

    @Test
    void mediaDirectoriesTest() {
        String subFolder1Name = "test_folder_1";
        String subFolder2Name = "test_folder_2";
        File testFile = Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                "test_file.ttt").toFile();

        assertNotNull(MediaDirectories.uploadsDirectory.listFiles());
        assertEquals(0, Objects.requireNonNull(MediaDirectories.uploadsDirectory.listFiles()).length);

        assertNotNull(MediaDirectories.mediaDirectory.listFiles());
        assertEquals(0, Objects.requireNonNull(MediaDirectories.mediaDirectory.listFiles()).length);

        assertDoesNotThrow(() ->
                MediaDirectories.createSubFolder(MediaDirectories.uploadsDirectory, subFolder1Name));

        assertEquals(Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), subFolder2Name),
                MediaDirectories.createSubFolder(MediaDirectories.uploadsDirectory, subFolder2Name));

        assertEquals(Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), ""),
                MediaDirectories.createSubFolder(MediaDirectories.uploadsDirectory, null));

        assertEquals(Objects.requireNonNull(MediaDirectories.uploadsDirectory.listFiles()).length, 2);

        assertDoesNotThrow(() ->
                MediaDirectories.createSubFolder(MediaDirectories.mediaDirectory, subFolder1Name));

        assertEquals(1, Objects.requireNonNull(MediaDirectories.mediaDirectory.listFiles()).length);

        assertDoesNotThrow(testFile::createNewFile);
        assertEquals(1, MediaDirectories.getFoldersInMedia().size());

        assertDoesNotThrow(() -> FileSystemDataProvider.deleteFileOrDirectory(testFile));
        assertDoesNotThrow(() ->
                FileSystemDataProvider.deleteFileOrDirectory(Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                        subFolder1Name).toFile()));
        assertDoesNotThrow(() ->
                FileSystemDataProvider.deleteFileOrDirectory(Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(),
                        subFolder1Name).toFile()));
        assertDoesNotThrow(() ->
                FileSystemDataProvider.deleteFileOrDirectory(Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(),
                        subFolder2Name).toFile()));

        assertEquals(0, Objects.requireNonNull(MediaDirectories.mediaDirectory.listFiles()).length);
        assertEquals(0, Objects.requireNonNull(MediaDirectories.uploadsDirectory.listFiles()).length);
    }

    @AfterAll
    @Test
    static void clear() {
        assertTrue(MediaDirectories.mediaDirectory.exists());
        assertTrue(MediaDirectories.uploadsDirectory.exists());
        assertEquals(0, Objects.requireNonNull(MediaDirectories.mediaDirectory.listFiles()).length);
        assertEquals(0, Objects.requireNonNull(MediaDirectories.uploadsDirectory.listFiles()).length);

        assertDoesNotThrow(() -> FileSystemDataProvider.deleteFileOrDirectory(MediaDirectories.mediaDirectory));
        assertDoesNotThrow(() -> FileSystemDataProvider.deleteFileOrDirectory(MediaDirectories.uploadsDirectory));

        assertFalse(MediaDirectories.mediaDirectory.exists());
        assertFalse(MediaDirectories.uploadsDirectory.exists());
    }
}
