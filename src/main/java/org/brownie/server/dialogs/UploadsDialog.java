package org.brownie.server.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import org.brownie.server.Application;
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
        upload.addFinishedListener(e -> processFile(subDir.getValue(), e.getFileName(), multiFileBuffer));

        Button close = new Button("Close", e -> {
            EventsManager.getManager().unregisterListener(this);
            close();
        });
        close.setWidthFull();
        mainLayout.add(upload, close);

        add(mainLayout);
        EventsManager.getManager().registerListener(this);

        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
        this.setDraggable(false);
        this.setModal(true);
        this.setSizeUndefined();
    }

    private void processFile(String subDir, String uploadedFileName, MultiFileBuffer multiFileBuffer) {
        try {
            Path pathWithSubfolder = Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath());
            if (subDir.trim().length() > 0) {
                pathWithSubfolder = Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(),
                        subDir.trim());
                if (!pathWithSubfolder.toFile().exists() && !pathWithSubfolder.toFile().mkdir()) {
                    Application.LOGGER.log(System.Logger.Level.ERROR,
                            "Can't locate or create file for upload '" + pathWithSubfolder.toFile() + "'");
                    return;
                }
            }

            Application.LOGGER.log(System.Logger.Level.INFO,
                    "Uploading to '" + pathWithSubfolder.toFile().getAbsolutePath() + "'");

            File newFile = FileSystemDataProvider.getUniqueFileName(
                    Paths.get(pathWithSubfolder.toFile().getAbsolutePath(), uploadedFileName).toFile());

            if (newFile.createNewFile()) {
                InputStream in = multiFileBuffer.getInputStream(uploadedFileName);
                OutputStream out = new FileOutputStream(newFile);

                byte[] data = new byte[BUFFER_SIZE];
                while(in.read(data) > 0) {
                    out.write(data);
                }
                in.close();
                out.close();
                Application.LOGGER.log(System.Logger.Level.INFO,
                        "New file uploaded '" + newFile.getAbsolutePath() + "'");

                VideoDecoder.getDecoder().addFileToQueue(subDir.trim(), newFile);
            } else {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Can't create new file '" + newFile.getAbsolutePath() + "'");
            }
        } catch (IOException ioException) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    "Error while uploading or processing files", ioException);
            ioException.printStackTrace();
        }
    }

    public void updateDiscCapacity() {
        long total = new File("/").getTotalSpace() / (1024 * 1024);
        long free = new File("/").getUsableSpace() / (1024 * 1024);
        String discCapacityText = "Free " + free + "MB of " + total + "MB";
        this.discCapacity.setText(discCapacityText);

        Application.LOGGER.log(System.Logger.Level.DEBUG,
                "Updated disc capacity '" + discCapacityText + "'");
    }

    @Override
    public void update(EventsManager.EVENT_TYPE eventType, Object[] params) {
        var ui = this.getUI().isPresent() ? this.getUI().get() : null;
        if (ui != null) ui.getSession().access(() -> updateDiscCapacity());
    }

    @Override
    public List<EventsManager.EVENT_TYPE> getEventTypes() {
        ArrayList<EventsManager.EVENT_TYPE> types = new ArrayList<>();

        types.add(EventsManager.EVENT_TYPE.FILE_SYSTEM_CHANGED);

        return types;
    }
}
