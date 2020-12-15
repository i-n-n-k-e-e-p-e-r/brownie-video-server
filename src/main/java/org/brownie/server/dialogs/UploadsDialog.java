package org.brownie.server.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import org.brownie.server.Application;
import org.brownie.server.events.EventsManager;
import org.brownie.server.events.IEventListener;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.recoder.VideoDecoder;
import org.brownie.server.views.CommonComponents;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UploadsDialog extends Dialog implements IEventListener {

    public static final int BUFFER_SIZE = 32 * 1024;

    private final Label discCapacity = new Label("");
    private final ComboBox<String> folders;
    private MultiFileBuffer multiFileBuffer = null;
    private Upload upload = null;
    private final Button closeButton = CommonComponents.createButton("Close",
            VaadinIcon.CLOSE_CIRCLE.create(),
            e -> {
        EventsManager.getManager().unregisterListener(this);
        close();
    });

    public UploadsDialog() {
        super();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.START);

        Label title = new Label("Media files upload");
        title.getStyle().set("font-weight", "bold");

        folders = new ComboBox<>("Sub directory for uploaded files");
        folders.setWidthFull();
        folders.setItems(getFolders());
        folders.setAllowCustomValue(true);
        folders.setPlaceholder("Type the name or choose");
        folders.addCustomValueSetListener(event -> {
            List<String> newValues = getFolders();
            newValues.add(event.getDetail());
            folders.setItems(newValues);
            folders.setValue(event.getDetail());
        });
        folders.setValue("");

        updateDiscCapacity();

        mainLayout.add(title, discCapacity, folders);

        add(mainLayout);
        EventsManager.getManager().registerListener(this);

        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
        this.setDraggable(false);
        this.setModal(true);
        this.setSizeUndefined();

        this.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                multiFileBuffer = new MultiFileBuffer();
                upload = new Upload(multiFileBuffer);
                folders.focus();
                upload.addFinishedListener(e -> processFile(folders.getValue(), e.getFileName(), multiFileBuffer));
                mainLayout.add(upload, closeButton);
            } else {
                mainLayout.remove(upload);
                mainLayout.remove(closeButton);
            }
        });
    }

    private List<String> getFolders() {
        return Arrays.stream(Objects.requireNonNull(MediaDirectories.mediaDirectory.listFiles()))
                .filter(File::isDirectory)
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    private void processFile(String subDir, String uploadedFileName, MultiFileBuffer multiFileBuffer) {
        try {
            if (subDir == null) subDir = "";

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

                if (FileSystemDataProvider.isVideo(newFile)) {
                    VideoDecoder.getDecoder().addFileToQueue(subDir.trim(), newFile);
                } else {
                    FileSystemDataProvider.copyUploadedFile(subDir, newFile);
                }
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

    public static UploadsDialog showUploadsDialog() {
        UploadsDialog dialog = new UploadsDialog();
        dialog.setMinWidth("340px");
        dialog.setMinHeight("320px");
        dialog.setWidth("340px");
        dialog.setHeight("-1");

        dialog.setResizable(false);
        dialog.setDraggable(false);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setModal(true);
        dialog.open();

        return dialog;
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
        if (ui != null) ui.getSession().access(() -> {
            updateDiscCapacity();
            folders.setItems(getFolders());
        });
    }

    @Override
    public List<EventsManager.EVENT_TYPE> getEventTypes() {
        ArrayList<EventsManager.EVENT_TYPE> types = new ArrayList<>();

        types.add(EventsManager.EVENT_TYPE.FILE_SYSTEM_CHANGED);

        return types;
    }
}
