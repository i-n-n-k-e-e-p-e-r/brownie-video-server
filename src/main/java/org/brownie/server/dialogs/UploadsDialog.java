package org.brownie.server.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.recoder.VideoDecoder;

import java.io.*;
import java.nio.file.Paths;

public class UploadsDialog extends Dialog {

    public static final int BUFFER_SIZE = 32 * 1024;
    public static final String MIN_DIALOG_WIDTH = "320px";

    private final Label discCapacity = new Label("");

    public UploadsDialog() {
        super();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.START);

        Label title = new Label("Media files upload");
        title.getStyle().set("font-weight", "bold");
        TextField subDir = new TextField("Sub directory to move uploaded files");
        subDir.setPlaceholder("Just one level directory");
        subDir.setTitle("Sub directory to move uploaded files");
        subDir.setValue("");
        subDir.setWidthFull();

        updateDiscCapacity();

        mainLayout.add(title, discCapacity, subDir);

        MultiFileBuffer multiFileBuffer = new MultiFileBuffer();

        Upload upload = new Upload(multiFileBuffer);
        upload.addFinishedListener(e -> multiFileBuffer.getFiles().forEach(fileName -> {
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

                // FIXME events and handlers for update main table needed
                VideoDecoder.encodeFile(newFile, new File(MediaDirectories.mediaDirectory.getAbsolutePath() +
                        File.separator + newFile.getName()));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                //FIXME events nad handlers for capacity change needed
                updateDiscCapacity();
            }
        }));

        Button close = new Button("Close", e -> close());
        close.setWidthFull();
        mainLayout.add(upload, close);

        add(mainLayout);

        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
        this.setDraggable(false);
        this.setModal(true);
        this.setSizeUndefined();
    }

    public void updateDiscCapacity() {
        long total = new File("/").getTotalSpace() / (1024 * 1024);
        long free = new File("/").getUsableSpace() / (1024 * 1024);
        this.discCapacity.setText("Free " + free + "MB of " + total + "MB");
    }

}
