package org.brownie.server.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.brownie.server.Application;
import org.brownie.server.db.User;
import org.brownie.server.events.EventsManager;
import org.brownie.server.events.IEventListener;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.recoder.VideoDecoder;
import org.brownie.server.views.CommonComponents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MoveFileDialog extends Dialog implements IEventListener {

    private final static String TITLE_STRING = "Moving files";

    private final ComboBox<String> folders;

    private Set<File> filesToMove;
    private User user;

    public MoveFileDialog(Set<File> filesToMove, User user) {
        super();
        this.filesToMove = filesToMove;
        this.user = user;

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.START);

        Label title = new Label(TITLE_STRING);
        title.getStyle().set("font-weight", "bold");

        folders = new ComboBox<>("Directory to move files ('root' if it's empty)");
        folders.setWidthFull();
        folders.setItems(getFolders());
        folders.setAllowCustomValue(false);
        folders.setValue("");

        Button createButton = CommonComponents.createButton("Move",
                VaadinIcon.CHECK_CIRCLE_O.create(), e -> {
                    if (folders.getValue() == null) { return; }

                    moveFiles(getFilesToMove(), folders.getValue());
                    close();
                });

        Button closeButton = CommonComponents.createButton("Close",
                VaadinIcon.CLOSE_CIRCLE.create(),
                e -> close());

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");

        buttonsLayout.add(createButton, closeButton);

        mainLayout.add(title, folders, buttonsLayout);

        add(mainLayout);

        this.addOpenedChangeListener(e -> {
           if (e.isOpened()) {
               EventsManager.getManager().registerListener(this);
           } else {
               EventsManager.getManager().unregisterListener(this);
           }
        });
    }

    private List<String> getFolders() {
        List<String> result = new LinkedList<>(MediaDirectories.getFoldersInMedia());
        result.add(0, "");
        return result;
    }

    protected void moveFiles(Set<File> dirtyFiles, String folderName) {
        Set<File> cleanFiles = getValidFiles(dirtyFiles);
        if (cleanFiles == null || cleanFiles.size() == 0) return;

        cleanFiles.forEach(f -> {
            if (moveFile(f, getFolderFile(folderName)) != null) {
                Application.LOGGER.log(System.Logger.Level.INFO,
                        "File '" + f.getName() + "' moved to '" + folderName + "' folder");
            }
        });

        EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_MOVED, getUser());
    }

    public static Path moveFile(File file, File folder) {
        try{
            return Files.move(file.toPath(), folder.toPath().resolve(file.getName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    "Error while moving file '" + file.getName() + "' to folder '" + folder.getName() + "'", e);
            e.printStackTrace();
            return null;
        }
    }

    public static File getFolderFile(String folderName) {
        if (folderName == null || folderName.trim().length() == 0) {
            return MediaDirectories.mediaDirectory;
        }

        Path folderPath = Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(), folderName);
        if (!folderPath.toFile().exists())
            return MediaDirectories.mediaDirectory;

        return folderPath.toFile();
    }

    public static Set<File> getValidFiles(Set<File> filesToCheck) {
        return filesToCheck.stream()
                .filter(f -> f.exists()
                            && !f.isDirectory()
                            && !VideoDecoder.getDecoder().isEncoding(f))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean update(EventsManager.EVENT_TYPE eventType, User user, Object... params) {
        var ui = this.getUI().isPresent() ? this.getUI().get() : null;
        if (ui != null && !ui.isClosing()) {
            ui.access(() -> {
                if (ui.isClosing()) return;

                var oldValue = folders.getValue();
                folders.setItems(getFolders());
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

    public static MoveFileDialog showDialog(Set<File> files, User user) {
        MoveFileDialog dialog = new MoveFileDialog(files, user);
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

    public Set<File> getFilesToMove() {
        return filesToMove;
    }

    public void setFilesToMove(Set<File> filesToMove) {
        this.filesToMove = filesToMove;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

