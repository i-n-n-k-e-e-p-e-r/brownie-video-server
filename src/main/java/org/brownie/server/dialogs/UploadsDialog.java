package org.brownie.server.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import org.brownie.server.events.EventsManager;
import org.brownie.server.events.IEventListener;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.recoder.VideoDecoder;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UploadsDialog extends Dialog implements IEventListener {

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
            try {
                if (subDir.getValue().trim().length() > 0) {
                    Path pathWithSubfolder = Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(),
                            subDir.getValue().trim());
                    if (!pathWithSubfolder.toFile().exists() && !pathWithSubfolder.toFile().mkdir()) {
                        return;
                    }
                }

                File newFile = FileSystemDataProvider.getUniqueFileName(
                        Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), fileName).toFile());

                if (newFile.createNewFile()) {
                    InputStream in = multiFileBuffer.getInputStream(e.getFileName());
                    OutputStream out = new FileOutputStream(newFile);

                    byte[] data = new byte[BUFFER_SIZE];
                    while(in.read(data) > 0) {
                        out.write(data);
                    }
                    in.close();
                    out.close();

                    VideoDecoder.getDecoder().addFileToQueue(subDir.getValue().trim(), newFile);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
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

    @Override
    public void update(EventsManager.EVENT_TYPE eventType, Object[] params) {
        UI.getCurrent().getSession().access(() -> {
            updateDiscCapacity();
        });
    }

    @Override
    public List<EventsManager.EVENT_TYPE> getEventTypes() {
        ArrayList<EventsManager.EVENT_TYPE> types = new ArrayList<>();

        types.add(EventsManager.EVENT_TYPE.FILE_SYSTEM_CHANGED);

        return types;
    }
}
