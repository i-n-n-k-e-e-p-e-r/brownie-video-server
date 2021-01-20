package org.brownie.server.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.brownie.server.Application;
import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;
import org.brownie.server.db.UserToFileState;
import org.brownie.server.events.EventsManager;
import org.brownie.server.recoder.VideoDecoder;
import org.brownie.server.views.CommonComponents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class RenameFileDialog extends Dialog {

    private final static String TITLE_STRING = "Renaming file or directory";
    private File fileToRename;

    public RenameFileDialog(File file, User user) {
        super();
        this.setFileToRename(file);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.START);

        Label title = new Label(TITLE_STRING);
        title.getStyle().set("font-weight", "bold");

        TextField newNameField = new TextField("New name");
        newNameField.setPlaceholder("Enter new file or directory name");
        newNameField.setWidthFull();
        newNameField.setRequired(true);
        newNameField.setValueChangeMode(ValueChangeMode.EAGER);
        newNameField.setValue(getFileToRename().getName());

        Button createButton = CommonComponents.createButton("Rename",
                VaadinIcon.CHECK_CIRCLE_O.create(),
                e -> {
                    if (!isInputValid(newNameField)) return;

                    File renamedFile = rename(getFileToRename(), newNameField.getValue().trim());
                    if (renamedFile != null) {
                        Application.LOGGER.log(System.Logger.Level.INFO,
                                "Renamed from '" + getFileToRename().getName() + "' to '" + renamedFile.getName() + "'");

                        EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_RENAMED,
                                user, renamedFile);

                        // Renamed file is new for viewed status
                        UserToFileState.getEntry(DBConnectionProvider.getInstance(), getFileToRename(), user)
                                .forEach(state -> state.deleteEntry(DBConnectionProvider.getInstance()));
                    } else {
                        Application.LOGGER.log(System.Logger.Level.INFO,
                                "Can't rename '" + getFileToRename().getName() + "'");
                    }
                    close();
                });

        Button closeButton = CommonComponents.createButton("Close",
                VaadinIcon.CLOSE_CIRCLE.create(),
                e -> close());

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");

        buttonsLayout.add(createButton, closeButton);

        mainLayout.add(title, newNameField, buttonsLayout);

        add(mainLayout);
    }

    protected boolean isInputValid(TextField newNameField) {
        if (newNameField.getValue() == null) return false;
        if (newNameField.getValue().trim().length() == 0) return false;
        if (getFileToRename().isDirectory()) {
            if (Arrays.stream(Objects.requireNonNull(getFileToRename().getParentFile().listFiles()))
                    .anyMatch(f -> f.getName().equals(newNameField.getValue()) && f.isDirectory())) {
                Notification.show("Folder already exists!");
                return false;
            }
            if (Arrays.stream(Objects.requireNonNull(getFileToRename().listFiles()))
                    .anyMatch(f -> VideoDecoder.getDecoder().isEncoding(f))) {
                Notification.show("Folder has encoding files. Please, try again later!");
                return false;
            }
        } else {
            if (VideoDecoder.getDecoder().isEncoding(getFileToRename())) {
                Notification.show("File encoding. Please, try again later!");
                return false;
            }
            if (Arrays.stream(Objects.requireNonNull(getFileToRename().getParentFile().listFiles()))
                    .anyMatch(f -> f.getName().equals(newNameField.getValue()) && !f.isDirectory())) {
                Notification.show("File already exists!");
                return false;
            }
        }

        return true;
    }

    protected File rename(File file, String newName) {
        try{
            // rename a file in the same directory
            return Files.move(file.toPath(), file.toPath().resolveSibling(newName)).toFile();
        } catch (IOException e) {
            Application.LOGGER.log(System.Logger.Level.ERROR, "Can't rename file", e);
            e.printStackTrace();
            return null;
        }
    }

    public static RenameFileDialog showDialog(File file, User user) {
        RenameFileDialog dialog = new RenameFileDialog(file, user);
        dialog.setMinWidth("300px");
        dialog.setMaxWidth("500px");
        dialog.setWidth("95%");
        dialog.setHeight("-1");

        dialog.setResizable(false);
        dialog.setDraggable(false);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setModal(true);
        dialog.open();

        return dialog;
    }

    public File getFileToRename() {
        return fileToRename;
    }

    public void setFileToRename(File fileToRename) {
        this.fileToRename = fileToRename;
    }
}
