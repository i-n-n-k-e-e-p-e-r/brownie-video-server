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
import org.brownie.server.db.User;
import org.brownie.server.events.EventsManager;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.views.CommonComponents;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class CreateFolderDialog extends Dialog {

    private final static String TITLE_STRING = "New directory creation";

    public CreateFolderDialog(User user) {
        super();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.START);

        Label title = new Label(TITLE_STRING);
        title.getStyle().set("font-weight", "bold");

        TextField folderNameField = new TextField("Sub directory in root folder");
        folderNameField.setPlaceholder("Enter directory name");
        folderNameField.setWidthFull();
        folderNameField.setRequired(true);
        folderNameField.setValueChangeMode(ValueChangeMode.EAGER);
        folderNameField.setValue("");

        Button createButton = CommonComponents.createButton("Create",
                VaadinIcon.CHECK_CIRCLE_O.create(),
                e -> {
                    if (folderNameField.getValue() == null) return;
                    if (folderNameField.getValue().trim().length() == 0) return;
                    if (Arrays.stream(Objects.requireNonNull(MediaDirectories.mediaDirectory.listFiles()))
                            .anyMatch(f -> f.getName().equals(folderNameField.getValue()) && f.isDirectory())) {
                        Notification.show("Folder already exists!");

                        return;
                    }

                    if (Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(),
                            folderNameField.getValue().trim()).toFile().mkdirs()) {
                        Application.LOGGER.log(System.Logger.Level.INFO,
                                "New folder created '" + folderNameField.getValue() + "'");
                        EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_CREATED, user);
                    } else {
                        Application.LOGGER.log(System.Logger.Level.ERROR,
                                "Can't create folder '" +folderNameField.getValue() + "'' in media directory");
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

        mainLayout.add(title, folderNameField, buttonsLayout);

        add(mainLayout);
    }

    public static CreateFolderDialog showDialog(User user) {
        CreateFolderDialog dialog = new CreateFolderDialog(user);
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
}
