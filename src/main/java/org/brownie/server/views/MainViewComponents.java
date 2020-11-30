package org.brownie.server.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import org.brownie.server.dialogs.PlayerDialog;
import org.brownie.server.dialogs.UploadsDialog;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.providers.MediaDirectories;

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

        treeGrid.addComponentHierarchyColumn(file -> {
            HorizontalLayout value;
            if (file.isDirectory()) {
                value = new HorizontalLayout(VaadinIcon.FOLDER.create(), new Label(file.getName()));
            } else {
                value = new HorizontalLayout(VaadinIcon.FILE.create(), new Label(file.getName()));
            }
            value.setPadding(false);
            value.setSpacing(true);

            return value;
        }).setHeader("Name").setId("file-name");

        treeGrid.addComponentColumn(file -> {
            if (!file.isDirectory()) {
                Button playButton = new Button();
                playButton.setText("Play");
                playButton.setIcon(VaadinIcon.PLAY.create());
                playButton.setWidth("100px");
                playButton.setHeight("30px");
                playButton.setDisableOnClick(true);
                playButton.addClickListener(playListener -> {
                    final PlayerDialog dialog = new PlayerDialog(file, null);
                    dialog.setWidth("90%");
                    dialog.setHeight("90%");
                    playButton.setEnabled(true);
                    dialog.open();
                });

                return playButton;
            }

            return new Label();
        }).setHeader("...").setId("file-play");

        treeGrid.setDataProvider(new FileSystemDataProvider(MediaDirectories.mediaDirectory));

        return treeGrid;
    }
}
