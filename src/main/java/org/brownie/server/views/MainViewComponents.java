package org.brownie.server.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import org.brownie.server.dialogs.PlayerDialog;
import org.brownie.server.dialogs.UploadsDialog;
import org.brownie.server.events.EventsManager;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.recoder.VideoDecoder;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class MainViewComponents {
    public static MenuBar createMenuBar(MainView mainView) {
        MenuBar menuBar = new MenuBar();

        MenuItem file = menuBar.addItem("File");
        file.getSubMenu().addItem("Uploads", e -> {
            UploadsDialog dialog = new UploadsDialog();
            dialog.open();
        });
        file.getSubMenu().addItem("Delete", e -> {
            if (mainView == null ||
                    mainView.getFilesGrid() == null ||
                    mainView.getFilesGrid().getSelectedItems().size() == 0) {
                return;
            }

            ConfirmDialog cd = new ConfirmDialog();
            cd.setHeader("Are you shure?");
            cd.setText("Selected files and folders will be deleted.");
            cd.addConfirmListener(event -> {
                if (mainView.getFilesGrid() != null) {
                    mainView.getFilesGrid().getSelectedItems().forEach(f -> {
                        if (f.exists()) {
                            if (f.isDirectory()) {
                                List.of(Objects.requireNonNull(f.listFiles())).forEach(ff -> {
                                    if (ff.exists()) ff.delete();
                                });
                            }
                            if (f.exists()) f.delete();
                        }
                    });
                }
                EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_SYSTEM_CHANGED, null);
                cd.close();
            });
            cd.addRejectListener(event -> cd.close());
            cd.open();
        });
        menuBar.addItem("Users");
        menuBar.addItem("About");

        return menuBar;
    }

    public static TreeGrid<File> createFilesTreeGrid() {
        TreeGrid<File> treeGrid = new TreeGrid<>();

        Grid.Column<?> fileNameColumn = treeGrid.addComponentHierarchyColumn(file -> {
            HorizontalLayout value;
            if (file.isDirectory()) {
                value = new HorizontalLayout(VaadinIcon.FOLDER.create(), new Label(file.getName()));
            } else {
                value = new HorizontalLayout(VaadinIcon.FILE.create(), new Label(file.getName()));
            }
            value.setPadding(false);
            value.setSpacing(true);

            return value;
        }).setHeader("Name");
        fileNameColumn.setId("file-name");
        fileNameColumn.setSortable(true);

        Grid.Column<?> playColumn = treeGrid.addComponentColumn(file -> {
            if (!file.isDirectory()) {
                Button playButton = new Button();
                playButton.setText("Play");
                playButton.setIcon(VaadinIcon.PLAY.create());
                playButton.setWidthFull();
                playButton.setDisableOnClick(true);
                playButton.addClickListener(playListener -> {
                    final PlayerDialog dialog = new PlayerDialog(file, null);
                    playButton.setEnabled(true);
                    dialog.open();
                });

                playButton.setEnabled(true);
                if (VideoDecoder.getDecoder().isEncoding(file)) {
                    playButton.setEnabled(false);
                    playButton.setText("Encoding...");
                }
                if(!file.getAbsolutePath().endsWith("." + VideoDecoder.OUTPUT_VIDEO_FORMAT)) {
                    playButton.setEnabled(false);
                    playButton.setText("Not supported");
                }

                return playButton;
            }

            return new Label();
        }).setHeader("Actions");
        playColumn.setId("file-play");
        playColumn.setSortable(false);

        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        final FileSystemDataProvider provider = new FileSystemDataProvider(treeGrid, MediaDirectories.mediaDirectory);
        treeGrid.addAttachListener(listener -> EventsManager.getManager().registerListener(provider));
        treeGrid.addDetachListener(listener -> EventsManager.getManager().unregisterListener(provider));
        treeGrid.setDataProvider(provider);

        return treeGrid;
    }
}
