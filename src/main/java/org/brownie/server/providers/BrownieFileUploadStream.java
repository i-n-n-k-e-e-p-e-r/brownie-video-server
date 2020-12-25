package org.brownie.server.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

class BrownieUploadOutputStream extends FileOutputStream implements Serializable {
    private final File file;

    BrownieUploadOutputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    File getFile() {
        return this.file;
    }
}
