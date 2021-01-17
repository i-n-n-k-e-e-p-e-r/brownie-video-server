package org.brownie.server.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import org.brownie.server.Application;
import org.brownie.server.events.EventsManager;
import org.brownie.server.events.IEventListener;
import org.brownie.server.providers.BrownieMultiFileBuffer;
import org.brownie.server.providers.BrownieUploadsFileFactory;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.recoder.VideoDecoder;
import org.brownie.server.views.CommonComponents;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UploadsDialog extends Dialog implements IEventListener {

    public static final int BUFFER_SIZE = 256 * 1024;
    private final static String TITLE_STRING = "Media files upload";

    private final Label discCapacity = new Label("");
    private final ComboBox<String> folders;
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

        Label title = new Label(TITLE_STRING);
        title.getStyle().set("font-weight", "bold");

        Checkbox convertVideo = new Checkbox();
        convertVideo.setLabel("Encode uploaded video files");
        convertVideo.setValue(true);
        convertVideo.setWidth("-1");

        folders = new ComboBox<>("Sub directory for uploaded files");
        folders.setWidthFull();
        folders.setItems(MediaDirectories.getFoldersInMedia());
        folders.setAllowCustomValue(true);
        folders.setPlaceholder("Type the name or choose");
        folders.addCustomValueSetListener(event -> {
            new Thread(() -> {
                List<String> newValues = MediaDirectories.getFoldersInMedia();
                newValues.add(event.getDetail());

                var ui = mainLayout.getUI().isPresent() ? mainLayout.getUI().get() : null;
                if (ui != null && !ui.isClosing()) {
                    ui.access(() -> {
                        if (ui.isClosing()) return;
                        folders.setItems(newValues);
                        folders.setValue(event.getDetail());
                    });
                }
            }).start();
        });
        folders.setValue("");

        updateDiscCapacity();

        mainLayout.add(title, discCapacity, folders, convertVideo);

        add(mainLayout);
        EventsManager.getManager().registerListener(this);

        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setDraggable(true);
        setResizable(true);
        setModal(true);
        setSizeUndefined();

        this.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                BrownieUploadsFileFactory tempFilesFactory = new BrownieUploadsFileFactory();
                BrownieMultiFileBuffer multiFileBuffer = new BrownieMultiFileBuffer(tempFilesFactory);

                upload = new Upload(multiFileBuffer);
                upload.getStyle().set("overflow", "auto");
                upload.setWidth("95%");
                upload.setHeight("100%");
                folders.focus();

                upload.addFailedListener(e -> Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Error while uploading file. Reason is '" + e.getReason() + "'."));
                upload.addFinishedListener(e -> new Thread(() -> {
                    final String subfolderName = getFoldersComboBoxValue(folders);
                    final Path subFolder = MediaDirectories.createSubFolder(MediaDirectories.uploadsDirectory,
                            subfolderName);

                    if (subFolder != null)
                        processFile(subfolderName,
                                subFolder,
                                e.getFileName(),
                                multiFileBuffer,
                                convertVideo.getValue());
                }).start());

                mainLayout.add(upload, closeButton);
            } else {
                mainLayout.remove(upload);
                mainLayout.remove(closeButton);
                upload = null;
            }
        });
    }

    public static String getFoldersComboBoxValue(ComboBox<String> comboBox) {
        if (comboBox.getValue() == null) {
            return "";
        } else {
            return comboBox.getValue().trim();
        }
    }

    protected boolean prepareUploadedFile(String uploadedFileName, File newFile, BrownieMultiFileBuffer filesBuffer) {
        filesBuffer.flushTempFileOutputBuffer(uploadedFileName);

        boolean fileReady = FileSystemDataProvider.createNewFile(newFile);

        if (!fileReady) {
            filesBuffer.removeFile(uploadedFileName);
            return false;
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = filesBuffer.getInputStream(uploadedFileName);
            out = new FileOutputStream(newFile);

            byte[] data = new byte[BUFFER_SIZE];
            while(in.read(data) > 0) {
                out.write(data);
            }

            fileReady = true;
        } catch (IOException ex) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    "Error while writing uploaded file '" + newFile.getAbsolutePath() + "'", ex);
            fileReady = false;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Error while closing streams '" + newFile.getAbsolutePath() + "'", e);
            } finally {
                filesBuffer.removeFile(uploadedFileName);

                if (!fileReady && newFile.exists() && newFile.delete()) {
                    Application.LOGGER.log(System.Logger.Level.ERROR,
                            "File deleted after creation error '" + newFile.getAbsolutePath() + "'");
                }
            }
        }

        return fileReady;
    }

    protected void processFile(String subfolderName,
                             Path subFolder,
                             String uploadedFileName,
                             BrownieMultiFileBuffer multiFileBuffer,
                             boolean encode) {

        Application.LOGGER.log(System.Logger.Level.INFO,
                "Processing file '" + uploadedFileName + " in " + subFolder.toFile().getAbsolutePath() + "'");

        Path pathToUploadedFile = Paths.get(subFolder.toFile().getAbsolutePath(), uploadedFileName);
        File newFile = FileSystemDataProvider.getUniqueFileName(pathToUploadedFile.toFile());
        if (prepareUploadedFile(uploadedFileName, newFile, multiFileBuffer)) {
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "Processing new file finished '" + newFile.getAbsolutePath() + "'");

            if (encode && FileSystemDataProvider.isVideo(newFile)) {
                VideoDecoder.getDecoder().addFileToQueue(subfolderName, newFile);
            } else {
                FileSystemDataProvider.copyUploadedFile(subfolderName, newFile);
            }
        } else {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    "Failed to process new file '" + newFile.getAbsolutePath() + "'");
        }
    }

    public static UploadsDialog showUploadsDialog() {
        UploadsDialog dialog = new UploadsDialog();
        dialog.setMinWidth("340px");
        dialog.setMaxWidth("800px");
        dialog.setMinHeight("320px");
        dialog.setWidth("95%");
        dialog.setHeight("95%");

        dialog.setResizable(true);
        dialog.setDraggable(false);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(true);
        dialog.open();

        return dialog;
    }

    protected void updateDiscCapacity() {
        long total = new File("/").getTotalSpace() / (1024 * 1024);
        long free = new File("/").getUsableSpace() / (1024 * 1024);
        String discCapacityText = "Free " + free + "MB of " + total + "MB";
        this.discCapacity.setText(discCapacityText);

        Application.LOGGER.log(System.Logger.Level.DEBUG,
                "Updated disc capacity '" + discCapacityText + "'");
    }

    @Override
    public boolean update(EventsManager.EVENT_TYPE eventType, Object... params) {
        var ui = this.getUI().isPresent() ? this.getUI().get() : null;
        if (ui != null && !ui.isClosing()) {
            ui.access(() -> {
                if (ui.isClosing()) return;

                updateDiscCapacity();
                var oldValue = folders.getValue();
                folders.setItems(MediaDirectories.getFoldersInMedia());
                folders.setValue(oldValue);
            });
            return true;
        }
        return false;
    }

    @Override
    public List<EventsManager.EVENT_TYPE> getEventTypes() {
        ArrayList<EventsManager.EVENT_TYPE> types = new ArrayList<>();

        types.add(EventsManager.EVENT_TYPE.FILE_CREATED);
        types.add(EventsManager.EVENT_TYPE.FILE_DELETED);
        types.add(EventsManager.EVENT_TYPE.ENCODING_FINISHED);

        return types;
    }

}
