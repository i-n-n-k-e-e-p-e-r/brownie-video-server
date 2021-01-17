package org.brownie.server.providers;

import com.vaadin.flow.component.upload.MultiFileReceiver;
import com.vaadin.flow.component.upload.receivers.AbstractFileBuffer;
import com.vaadin.flow.component.upload.receivers.FileData;
import org.brownie.server.Application;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BrownieMultiFileBuffer extends AbstractFileBuffer implements MultiFileReceiver {
    private final BrownieUploadsFileFactory factory;
    private final Map<String, BrownieFileData> files = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, File> fileNamesToTempFiles = Collections.synchronizedMap(new HashMap<>());

    public BrownieMultiFileBuffer(BrownieUploadsFileFactory factory) {
        super(factory);
        this.factory = factory;
    }

    public OutputStream receiveUpload(String fileName, String MIMEType) {
        FileOutputStream outputBuffer = this.createFileOutputStream(fileName);
        this.files.put(fileName, new BrownieFileData(fileName, MIMEType, outputBuffer));
        return outputBuffer;
    }

    protected FileOutputStream createFileOutputStream(String fileName) {
        try {
            File tempFile = this.factory.createFile(fileName);
            this.fileNamesToTempFiles.putIfAbsent(fileName, tempFile);
            return new BrownieUploadOutputStream(tempFile);
        } catch (IOException var3) {
            this.getLogger().log(Level.SEVERE, "Failed to create file output stream for: '" + fileName + "'", var3);
            return null;
        }
    }

    public Set<String> getFiles() {
        return this.files.keySet();
    }

    public FileData getFileData(String fileName) {
        return this.files.get(fileName);
    }

    public FileDescriptor getFileDescriptor(String fileName) {
        if (this.files.containsKey(fileName)) {
            try {
                return ((FileOutputStream) this.files.get(fileName).getOutputBuffer()).getFD();
            } catch (IOException var3) {
                this.getLogger().log(Level.WARNING, "Failed to get file descriptor for: '" + fileName + "'", var3);
            }
        }

        return null;
    }

    public InputStream getInputStream(String fileName) {
        if (this.files.containsKey(fileName)) {
            try {
                return new FileInputStream(this.files.get(fileName).getFile());
            } catch (IOException var3) {
                this.getLogger().log(Level.WARNING, "Failed to create InputStream for: '" + fileName + "'", var3);
            }
        }

        return new ByteArrayInputStream(new byte[0]);
    }

    public File getTempFile(String fileName) {
        return this.fileNamesToTempFiles.get(fileName);
    }

    public void deleteTempFile(String uploadedFileName) {
        File tempFile = this.getTempFile(uploadedFileName);
        if (tempFile != null && tempFile.exists() && tempFile.delete()) {
            Application.LOGGER.log(System.Logger.Level.DEBUG,
                    "Temp file deleted'" + tempFile.getAbsolutePath() + "'");
        }

        this.factory.getTempFiles().remove(this.fileNamesToTempFiles.get(uploadedFileName));
    }

    public void removeFile(String fileName) {
        closeTempFileOutputBuffer(fileName);
        deleteTempFile(fileName);

        this.fileNamesToTempFiles.remove(fileName);
        this.files.remove(fileName);
    }

    public void closeTempFileOutputBuffer(String uploadedFileName) {
        try {
            this.getFileData(uploadedFileName).getOutputBuffer().close();
        } catch (IOException e) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    " '" + uploadedFileName + "'", e);
        }
    }

    public void flushTempFileOutputBuffer(String uploadedFileName) {
        try {
            this.getFileData(uploadedFileName).getOutputBuffer().flush();
        } catch (IOException e) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    " '" + uploadedFileName + "'", e);
        }
    }
}