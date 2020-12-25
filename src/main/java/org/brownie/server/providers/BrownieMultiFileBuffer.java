package org.brownie.server.providers;

import com.vaadin.flow.component.upload.MultiFileReceiver;
import com.vaadin.flow.component.upload.receivers.AbstractFileBuffer;
import com.vaadin.flow.component.upload.receivers.FileData;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BrownieMultiFileBuffer extends AbstractFileBuffer implements MultiFileReceiver {
    private final BrownieUploadsFileFactory factory;
    private final Map<String, BrownieFileData> files = new HashMap<>();

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
            return new BrownieUploadOutputStream(this.factory.createFile(fileName));
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
}