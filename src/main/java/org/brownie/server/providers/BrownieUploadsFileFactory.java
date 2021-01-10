package org.brownie.server.providers;

import com.vaadin.flow.component.upload.receivers.FileFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrownieUploadsFileFactory implements FileFactory {
    private final List<File> tempFiles = Collections.synchronizedList(new ArrayList<>());

    @Override
    public File createFile(String s) throws IOException {
        String tempFileName = FileSystemDataProvider.TEMP_UPLOADED_FILE_PREFIX + s + "_" + System.currentTimeMillis();
        File tempFile = File.createTempFile(tempFileName, null);
        tempFiles.add(tempFile);
        return tempFile;
    }

    public List<File> getTempFiles() {
        return this.tempFiles;
    }
}
