package org.brownie.server.views;

import com.vaadin.flow.component.button.Button;
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

public class MainViewComponents {
    public static MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        MenuItem file = menuBar.addItem("File");
        file.getSubMenu().addItem("Uploads", e -> {
            UploadsDialog dialog = new UploadsDialog();
            dialog.open();
        });
        menuBar.addItem("Users");
        menuBar.addItem("About");

        return menuBar;
    }

    public static TreeGrid createFilesTreeGrid() {
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
        treeGrid.addAttachListener(listener -> {
            EventsManager.getManager().registerListener(provider);
        });
        treeGrid.addDetachListener(listener -> {
            EventsManager.getManager().unregisterListener(provider);
        });
        treeGrid.setDataProvider(provider);

        return treeGrid;
    }
}
