package org.brownie.server.providers;

import com.vaadin.flow.component.upload.receivers.FileData;

import java.io.File;
import java.io.OutputStream;

public class BrownieFileData extends FileData {
    private final String fileName;
    private final String mimeType;
    private final OutputStream outputBuffer;

    public BrownieFileData(String fileName, String mimeType, OutputStream outputBuffer) {
        super(fileName, mimeType, outputBuffer);
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.outputBuffer = outputBuffer;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public String getFileName() {
        return this.fileName;
    }

    public OutputStream getOutputBuffer() {
        return this.outputBuffer;
    }

    public File getFile() {
        if (this.outputBuffer == null) {
            throw new NullPointerException("OutputBuffer is null");
        } else if (this.outputBuffer instanceof BrownieUploadOutputStream) {
            return ((BrownieUploadOutputStream)this.outputBuffer).getFile();
        } else {
            String MESSAGE = String.format("%s not supported. Use a UploadOutputStream", this.outputBuffer.getClass());
            throw new UnsupportedOperationException(MESSAGE);
        }
    }
}
