package org.brownie.server.providers;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SizeFileFilter;
import org.brownie.server.events.EventsManager;
import org.brownie.server.views.MainViewComponents;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemDataProviderTest {
    public static final String TEMP_FILES_DIR = System.getProperty("java.io.tmpdir");
    public static final String TEST_ROOT_DIR_NAME = "test_dir";

    private static File root;
    private static File folder1;
    private static File folder2;

    @BeforeAll
    @Test
    static void setup() {
        assertDoesNotThrow(() -> MediaDirectories.initDirectories(null));

        assertNotNull(TEMP_FILES_DIR);
        root = Paths.get(System.getProperty("java.io.tmpdir"), TEST_ROOT_DIR_NAME).toFile();

        assertDoesNotThrow(() -> root.mkdirs());
        assertTrue(root.exists());

        folder1 = Paths.get(root.getAbsolutePath(), "_Папка 1 (_Test-!) (O.O)").toFile();
        assertDoesNotThrow(() -> folder1.mkdirs());
        assertTrue(folder1.exists());

        folder2 = Paths.get(root.getAbsolutePath(), "_Folder 1 (_Тест-?) o_O").toFile();
        assertDoesNotThrow(() -> folder2.mkdirs());
        assertTrue(folder2.exists());

        for (int i = 0; i < 4; i++) {
            File file = Paths.get(root.getAbsolutePath(), "root_(O.0) файл " + i + ".test").toFile();
            assertDoesNotThrow(file::createNewFile);
            assertTrue(file.exists());
        }

        for (int i = 0; i < 4; i++) {
            File file = Paths.get(folder1.getAbsolutePath(), "folder_(O.0) файл" + i + ".test").toFile();
            assertDoesNotThrow(file::createNewFile);
            assertTrue(file.exists());
        }

        for (int i = 0; i < 4; i++) {
            File file = Paths.get(folder2.getAbsolutePath(), "(O_0) файл в папке_" + i).toFile();
            assertDoesNotThrow(file::createNewFile);
            assertTrue(file.exists());
        }
        
    }

    @Test
    public void testConstructor() {
        assertSame((new FileSystemDataProvider(new TreeGrid<>(), root)).getRoot(), root);
    }

    @Test
    public void testConstructor2() {
        TreeGrid<File> grid = new TreeGrid<>(File.class);
        assertSame((new FileSystemDataProvider(grid, root)).getRoot(), root);
    }

    @Test
    public void testGetChildCount() {
        FileSystemDataProvider fileSystemDataProvider = new FileSystemDataProvider(new TreeGrid<>(), root);
        assertEquals(Objects.requireNonNull(root.listFiles()).length,
                fileSystemDataProvider.getChildCount(new HierarchicalQuery<>(null, root)));
        assertEquals(Objects.requireNonNull(folder1.listFiles()).length,
                fileSystemDataProvider.getChildCount(new HierarchicalQuery<>(null, folder1)));
        assertEquals(Objects.requireNonNull(folder2.listFiles()).length,
                fileSystemDataProvider.getChildCount(new HierarchicalQuery<>(null, folder2)));
    }

    @Test
    public void testGetChildCountFilters() {
        FileSystemDataProvider fileSystemDataProvider = new FileSystemDataProvider(new TreeGrid<>(), root);
        IOFileFilter filter1 = FileFilterUtils.ageFileFilter(1L);
        AndFileFilter filter = new AndFileFilter(filter1, FileFilterUtils.ageFileFilter(1L));
        assertEquals(0, fileSystemDataProvider.getChildCount(new HierarchicalQuery<>(filter, root)));

        SizeFileFilter sizeFilter = new SizeFileFilter(0);
        assertEquals(Objects.requireNonNull(root.listFiles()).length,
                fileSystemDataProvider.getChildCount(new HierarchicalQuery<>(sizeFilter, root)));
    }

    @Test
    public void testFetchChildrenFromBackEnd() {
        FileSystemDataProvider fileSystemDataProvider = new FileSystemDataProvider(new TreeGrid<>(), root);
        assertEquals(Objects.requireNonNull(root.listFiles()).length, fileSystemDataProvider
                .fetchChildrenFromBackEnd(new HierarchicalQuery<>(null, root)).count());
    }

    @Test
    public void testHasChildren() {
        FileSystemDataProvider fileSystemDataProvider = new FileSystemDataProvider(new TreeGrid<>(), root);
        assertTrue(fileSystemDataProvider.hasChildren(root));
        assertTrue(fileSystemDataProvider.hasChildren(folder1));
        assertTrue(fileSystemDataProvider.hasChildren(folder2));
    }

    @Test
    public void testGetAllGridItems() {
        if (!MediaDirectories.mediaDirectory.exists())
            assertDoesNotThrow(MediaDirectories.mediaDirectory::mkdirs);
        File testFile = Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                "test_getALLGridItems.test").toFile();
        if (!testFile.exists())
            assertDoesNotThrow(testFile::createNewFile);

        assertTrue(new FileSystemDataProvider(MainViewComponents.createFilesTreeGrid(
                null),
                MediaDirectories.mediaDirectory).getAllGridItems().size() > 0);

        if (testFile.exists())
            assertDoesNotThrow(testFile::delete);
    }

    @Test
    public void testProcessEvent() {
        FileSystemDataProvider provider =
                new FileSystemDataProvider(MainViewComponents.createFilesTreeGrid(null), root);

        assertDoesNotThrow(() -> provider.processEvent(null,
                EventsManager.EVENT_TYPE.FILE_CREATED,
                provider.getAllGridItems(),
                Arrays.stream(Objects.requireNonNull(root.listFiles())).iterator().next()));

        assertDoesNotThrow(() -> provider.processEvent(null,
                EventsManager.EVENT_TYPE.FILE_CREATED,
                provider.getAllGridItems(),
                Arrays.stream(Objects.requireNonNull(folder1.listFiles())).iterator().next()));

        assertDoesNotThrow(() -> provider.processEvent(null,
                EventsManager.EVENT_TYPE.ENCODING_FINISHED,
                provider.getAllGridItems(),
                Arrays.stream(Objects.requireNonNull(root.listFiles())).iterator().next()));

        assertDoesNotThrow(() -> provider.processEvent(null,
                EventsManager.EVENT_TYPE.ENCODING_FINISHED,
                provider.getAllGridItems(),
                Arrays.stream(Objects.requireNonNull(folder1.listFiles())).iterator().next()));

        assertDoesNotThrow(() -> provider.processEvent(null,
                EventsManager.EVENT_TYPE.FILE_CREATED,
                null,
                Arrays.stream(Objects.requireNonNull(root.listFiles())).iterator().next()));

        assertDoesNotThrow(() -> provider.processEvent(null,
                EventsManager.EVENT_TYPE.FILE_CREATED,
                null));
    }

    @Test
    public void testGetEventTypes() {
        List<EventsManager.EVENT_TYPE> actualEventTypes = (new FileSystemDataProvider(new TreeGrid<>(), root))
                .getEventTypes();
        assertEquals(6, actualEventTypes.size());
        assertEquals(EventsManager.EVENT_TYPE.FILE_MOVED, actualEventTypes.get(0));
        assertEquals(EventsManager.EVENT_TYPE.FILE_RENAMED, actualEventTypes.get(1));
        assertEquals(EventsManager.EVENT_TYPE.FILE_DELETED, actualEventTypes.get(2));
        assertEquals(EventsManager.EVENT_TYPE.FILE_CREATED, actualEventTypes.get(3));
        assertEquals(EventsManager.EVENT_TYPE.ENCODING_STARTED, actualEventTypes.get(4));
        assertEquals(EventsManager.EVENT_TYPE.ENCODING_FINISHED, actualEventTypes.get(5));
    }

    @Test
    public void testGetUniqueFileName() {
        String fileName = "root_(O.0) файл";
        String fileExtension = "ttest";
        File file = Paths.get(root.getAbsolutePath(),
                fileName + "." + fileExtension).toFile();

        assertEquals(fileName + "." + fileExtension,
                FileSystemDataProvider.getUniqueFileName(file).getName());

        assertDoesNotThrow(file::createNewFile);

        assertEquals(fileName + " copy." + fileExtension,
                FileSystemDataProvider.getUniqueFileName(file).getName());

        File fileCopy = Paths.get(root.getAbsolutePath(),
                fileName + " copy." + fileExtension).toFile();
        assertDoesNotThrow(fileCopy::createNewFile);

        assertEquals(fileName + " copy copy." + fileExtension,
                FileSystemDataProvider.getUniqueFileName(fileCopy).getName());

        if (file.exists()) assertDoesNotThrow(file::delete);
        if (fileCopy.exists()) assertDoesNotThrow(fileCopy::delete);
    }

    @Test
    public void testGetExtension() {
        assertEquals("test",
                FileSystemDataProvider.getExtension(Arrays.stream(Objects.requireNonNull(folder1.listFiles())).iterator().next()));

        assertEquals("",
                FileSystemDataProvider.getExtension(Paths.get(System.getProperty("java.io.tmpdir"), "More").toFile()));
    }

    @Test
    public void testApply() {
        TreeGrid<File> grid = new TreeGrid<>(File.class);
        assertEquals(1, new FileSystemDataProvider(grid, root).apply(SortDirection.ASCENDING).count());
    }

    @Test
    public void testGetMIMETypeFromURLConnections() {
        assertEquals("text/plain", FileSystemDataProvider
                .getMIMETypeFromURLConnections(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertEquals("", FileSystemDataProvider
                .getMIMETypeFromURLConnections(Paths.get(System.getProperty("java.io.tmpdir"), "More").toFile()));
    }

    @Test
    public void testGetMIMETypeFromFiles() {
        assertEquals("text/plain", FileSystemDataProvider
                .getMIMETypeFromFiles(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertEquals("",
                FileSystemDataProvider.getMIMETypeFromFiles(Paths.get(System.getProperty("java.io.tmpdir"), "More").toFile()));
    }

    @Test
    public void testIsVideo() {
        assertFalse(FileSystemDataProvider.isVideo(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertFalse(FileSystemDataProvider.isVideo(Paths.get(System.getProperty("java.io.tmpdir"), "video").toFile()));
    }

    @Test
    public void testIsImage() {
        assertFalse(FileSystemDataProvider.isImage(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertFalse(FileSystemDataProvider.isImage(Paths.get(System.getProperty("java.io.tmpdir"), "image").toFile()));
    }

    @Test
    public void testIsAudio() {
        assertFalse(FileSystemDataProvider.isAudio(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertFalse(FileSystemDataProvider.isAudio(Paths.get(System.getProperty("java.io.tmpdir"), "audio").toFile()));
    }

    @Test
    public void testIsText() {
        assertTrue(FileSystemDataProvider.isText(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertFalse(FileSystemDataProvider.isText(Paths.get(System.getProperty("java.io.tmpdir"), "text").toFile()));
    }

    @Test
    public void testIsDataFile() {
        assertFalse(
                FileSystemDataProvider.isDataFile(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertTrue(FileSystemDataProvider.isDataFile(Paths.get(System.getProperty("java.io.tmpdir"), "video").toFile()));
    }

    @Test
    public void testCopyUploadedFile() {
        if (!MediaDirectories.mediaDirectory.exists())
            assertDoesNotThrow(MediaDirectories.mediaDirectory::mkdirs);
        if (!MediaDirectories.uploadsDirectory.exists())
            assertDoesNotThrow(MediaDirectories.uploadsDirectory::mkdirs);

        File subFolderInUploads = Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(),
                "unit_tests_folder").toFile();
        if (!subFolderInUploads.exists())
            assertDoesNotThrow(subFolderInUploads::mkdirs);

        File fileInUploads = Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(),
                subFolderInUploads.getName(),
                "test_file.m4v").toFile();

        if (!fileInUploads.exists())
            assertDoesNotThrow(fileInUploads::createNewFile);

        assertDoesNotThrow(() -> FileSystemDataProvider
                .copyUploadedFile(subFolderInUploads.getName(), fileInUploads));

        assertFalse(fileInUploads::exists);
        assertFalse(subFolderInUploads::exists);

        assertTrue(Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                subFolderInUploads.getName()).toFile().exists());
        assertTrue(Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                subFolderInUploads.getName(), fileInUploads.getName()).toFile().exists());

        assertDoesNotThrow(() -> Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                subFolderInUploads.getName(), fileInUploads.getName()).toFile().delete());
        assertDoesNotThrow(() -> Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                subFolderInUploads.getName()).toFile().delete());
        assertDoesNotThrow(MediaDirectories.mediaDirectory::delete);
        assertDoesNotThrow(MediaDirectories.uploadsDirectory::delete);
    }

    @Test
    public void testGetImageSize() {
        assertNull(
                FileSystemDataProvider.getImageSize(Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile()));
        assertNull(FileSystemDataProvider.getImageSize(Paths.get(System.getProperty("java.io.tmpdir"), "").toFile()));
    }

    @Test
    public void testDeleteFileOrDirectory() {
        assertTrue(folder2.exists());
        FileSystemDataProvider.deleteFileOrDirectory(folder2);
        assertFalse(folder2.exists());
    }

    @Test
    public void testClearTempDirectory() {
        var factory = new BrownieUploadsFileFactory();
        assertDoesNotThrow(() -> factory.createFile("kuku_1_" + System.currentTimeMillis() + ".tmp"));
        assertDoesNotThrow(() -> factory.createFile("kuku_2_" + System.currentTimeMillis() + ".tmp"));
        assertDoesNotThrow(() -> factory.createFile("kuku_3_" + System.currentTimeMillis() + ".tmp"));
        assertDoesNotThrow(() -> FileSystemDataProvider.clearTempDirectory(factory));
        AtomicBoolean allDeleted = new AtomicBoolean(true);
        factory.getTempFiles().forEach(file -> { if (file.exists()) allDeleted.set(false); });
        assertTrue(allDeleted.get());
    }

    @AfterAll
    @Test
    static void clear() {
        if (folder1.exists()) {
            for (var f : Objects.requireNonNull(folder1.listFiles())) {
                if (f.exists()) assertTrue(f.delete());
            }
            assertDoesNotThrow(folder1::delete);
        }
        if (folder2.exists()) {
            for (var f : Objects.requireNonNull(folder2.listFiles())) {
                if (f.exists()) assertTrue(f.delete());
            }
            assertDoesNotThrow(folder2::delete);
        }
        if (root.exists()) {
            for (var f : Objects.requireNonNull(root.listFiles())) {
                if (f.exists()) assertTrue(f.delete());
            }
            assertDoesNotThrow(root::delete);
        }

        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(root.exists());

        assertDoesNotThrow(() -> FileSystemDataProvider.deleteFileOrDirectory(MediaDirectories.mediaDirectory));
        assertFalse(MediaDirectories.mediaDirectory.exists());
        assertDoesNotThrow(() -> FileSystemDataProvider.deleteFileOrDirectory(MediaDirectories.uploadsDirectory));
        assertFalse(MediaDirectories.uploadsDirectory.exists());
    }
}

