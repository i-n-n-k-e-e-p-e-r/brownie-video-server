package org.brownie.server.dialogs;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import org.brownie.server.providers.MediaDirectories;

import java.io.*;
import java.nio.file.Paths;

public class UploadsDialog extends Dialog {

    public static final int BUFFER_SIZE = 32 * 1024;

    public UploadsDialog() {
        super();

        MultiFileBuffer multiFileBuffer = new MultiFileBuffer();

        Upload upload = new Upload(multiFileBuffer);
        upload.addFinishedListener(e -> {
            multiFileBuffer.getFiles().stream().forEach(fileName -> {
                File newFile = Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), fileName).toFile();
                try {
                    if (newFile.exists()) newFile.delete();
                    newFile.createNewFile();

                    InputStream in = multiFileBuffer.getInputStream(e.getFileName());
                    OutputStream out = new FileOutputStream(newFile);

                    byte[] data = new byte[BUFFER_SIZE];
                    while(in.read(data) > 0) {
                        out.write(data);
                    }
                    in.close();
                    out.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
        });
        upload.setWidth("99%");
        add(upload);

        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
        this.setDraggable(false);
        this.setModal(true);

        this.setSizeFull();
    }

}
