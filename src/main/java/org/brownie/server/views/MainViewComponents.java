package org.brownie.server.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.SortDirection;
import org.brownie.server.db.User;
import org.brownie.server.dialogs.PlayerDialog;
import org.brownie.server.dialogs.SystemLoadDialog;
import org.brownie.server.dialogs.UploadsDialog;
import org.brownie.server.dialogs.UsersDialog;
import org.brownie.server.events.EventsManager;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.recoder.VideoDecoder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MainViewComponents {
    public static MenuBar createMenuBar(MainView mainView) {
        MenuBar menuBar = new MenuBar();

        if (mainView.getCurrentUser().getGroup() == User.GROUP.ADMIN.ordinal()) {
            MenuItem file = menuBar.addItem("File");
            file.addComponentAsFirst(VaadinIcon.CLIPBOARD_TEXT.create());

            file.getSubMenu().addItem("Uploads", e -> UploadsDialog.showUploadsDialog());

            file.getSubMenu().addItem("Delete", e -> {
                if (mainView.getFilesGrid() == null
                        || mainView.getFilesGrid().getSelectedItems().size() == 0) {
                    return;
                }

                ConfirmDialog cd = new ConfirmDialog();
                cd.setRejectable(true);
                cd.addRejectListener(rEvent -> cd.close());
                cd.setHeader("Are you shure?");
                cd.setText("Selected files and folders will be deleted.");
                cd.addConfirmListener(event -> {
                    Object[] toDelete = null;
                    if (mainView.getFilesGrid() != null) {
                        toDelete = new Object[mainView.getFilesGrid().getSelectedItems().size()];
                        mainView.getFilesGrid().getSelectedItems().forEach(
                                FileSystemDataProvider::deleteFileOrDirectory);
                    }
                    EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_DELETED, toDelete);
                    cd.close();
                });
                cd.addRejectListener(event -> cd.close());
                cd.open();
            });

            MenuItem users = menuBar.addItem("Users", e -> {
                UsersDialog usersDialog = new UsersDialog();
                usersDialog.open();
            });
            users.addComponentAsFirst(VaadinIcon.GROUP.create());
        }

        MenuItem systemInformation = menuBar.addItem("System information",
                e -> SystemLoadDialog.showSystemLoadDialog());
        systemInformation.addComponentAsFirst(VaadinIcon.INFO_CIRCLE_O.create());

        MenuItem exit = menuBar.addItem("Exit",
                e -> { if (menuBar.getUI().isPresent()) menuBar.getUI().get().getPage().reload(); });
        exit.addComponentAsFirst(VaadinIcon.EXIT_O.create());

        return menuBar;
    }

    private static Icon getIconForFile(File file) {
        if (file.isDirectory()) {
            return VaadinIcon.RECORDS.create();
        }
        if (FileSystemDataProvider.isVideo(file)) {
            return VaadinIcon.FILM.create();
        }
        if (FileSystemDataProvider.isAudio(file)) {
            return VaadinIcon.HEADPHONES.create();
        }
        if (FileSystemDataProvider.isImage(file)) {
            return VaadinIcon.PICTURE.create();
        }
        if (FileSystemDataProvider.isText(file)) {
            return VaadinIcon.TEXT_LABEL.create();
        }

        return VaadinIcon.FILE_O.create();
    }

    public static TreeGrid<File> createFilesTreeGrid(MainView mainView) {
        TreeGrid<File> treeGrid = new TreeGrid<>();

        Grid.Column<?> fileNameColumn = treeGrid.addComponentHierarchyColumn(file -> {
            HorizontalLayout value = new HorizontalLayout(getIconForFile(file), new Label(file.getName()));
            value.setPadding(false);
            value.setSpacing(true);

            return value;
        }).setHeader("Name");
        fileNameColumn.setId("file-name");
        fileNameColumn.setSortable(true);

        Grid.Column<?> playColumn = treeGrid.addComponentColumn(file ->
                MainViewComponents.getActionsLayout(mainView, file)).setHeader("Actions");
        playColumn.setId("file-play");
        playColumn.setSortable(false);

        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        if (mainView.getCurrentUser().getGroup() == User.GROUP.USER.ordinal())
            treeGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        final FileSystemDataProvider provider = new FileSystemDataProvider(treeGrid, MediaDirectories.mediaDirectory);
        // register for receiving updates from other UIs
        treeGrid.addAttachListener(listener -> EventsManager.getManager().registerListener(provider));
        treeGrid.addDetachListener(listener -> EventsManager.getManager().unregisterListener(provider));
        treeGrid.setDataProvider(provider);
        fileNameColumn.setSortOrderProvider(provider);
        treeGrid.sort(Collections.singletonList(new GridSortOrder<>(treeGrid.getColumns().get(0),
                SortDirection.ASCENDING)));

        return treeGrid;
    }

    private static Component getActionsLayout(MainView mainView, File file) {
        if (file.isDirectory()) return new Label();

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);
        actionsLayout.setWidthFull();

        Button playButton = new Button();
        if (FileSystemDataProvider.isVideo(file) || FileSystemDataProvider.isAudio(file)) {
            playButton.setText("Play");
            playButton.setIcon(VaadinIcon.PLAY.create());
        } else {
            if (FileSystemDataProvider.isImage(file) || FileSystemDataProvider.isText(file)) {
                playButton.setText("Show");
                playButton.setIcon(VaadinIcon.OPEN_BOOK.create());
            }
        }
        playButton.setWidthFull();
        playButton.setDisableOnClick(true);
        playButton.addClickListener(playListener -> {
            final PlayerDialog dialog = new PlayerDialog(file, null, mainView);
            playButton.setEnabled(true);
            dialog.open();
        });

        playButton.setEnabled(true);
        boolean encoding = false;
        if (VideoDecoder.getDecoder().isEncoding(file)) {
            encoding = true;
            playButton.setEnabled(false);
            playButton.setText("Encoding...");
            playButton.setIcon(VaadinIcon.COGS.create());
        }
        if (isNotSupported(file)) {
            playButton.setEnabled(false);
            playButton.setText("Not supported");
            playButton.setIcon(VaadinIcon.FROWN_O.create());
        }

        Map.Entry<Component, Button> wrapper = CommonComponents.getDownloadButtonWrapper("Download", file);

        if (encoding) wrapper.getValue().setEnabled(false);
        actionsLayout.add(
                playButton,
                wrapper.getKey()
        );

        return actionsLayout;
    }

    private static boolean isNotSupported(File file) {
        return (FileSystemDataProvider.isVideo(file) && !file.getName().endsWith(VideoDecoder.OUTPUT_VIDEO_FORMAT))
                || FileSystemDataProvider.isDataFile(file);
    }
}
