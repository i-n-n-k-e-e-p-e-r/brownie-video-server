package org.brownie.server.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import org.brownie.server.Application;
import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UsersDialog extends Dialog {

    public static final String MIN_WIDTH = "380px";
    public static final String MIN_HEIGHT = "460px";
    private static final List<User> users = Collections.synchronizedList(new ArrayList<>());
    private static final List<Grid> gridsForUpdate = Collections.synchronizedList(new ArrayList<>());

    private Grid<User> usersGrid = new Grid<>();

    public UsersDialog() {
        super();

        updateUsers();
        init();

        this.setMinWidth(MIN_WIDTH);
        this.setMinHeight(MIN_HEIGHT);
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
        this.setResizable(true);
        this.setModal(false);
        this.setDraggable(true);

        this.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                gridsForUpdate.add(usersGrid);
            } else {
                gridsForUpdate.remove(usersGrid);
            }
        });
    }

    private void updateUsers() {
        try {
            users.clear();
            users.addAll((Collection<? extends User>) DBConnectionProvider.getInstance().getOrmDaos().get(User.class).queryForAll());
            gridsForUpdate.parallelStream().forEach(dp -> {
                if (usersGrid == null || usersGrid.getDataProvider() == null) return;
                var ui = usersGrid.getUI().isPresent() ? usersGrid.getUI().get() : null;
                if (ui != null) usersGrid.getDataProvider().refreshAll();
            });
        } catch (SQLException e) {
            e.printStackTrace();
            Application.LOGGER.log(System.Logger.Level.ERROR, "Error while loading users.", e);
        }
    }

    private void init() {
        VerticalLayout mainLayout = new VerticalLayout();

        Label title = new Label("Users manager");
        title.getStyle().set("font-weight", "bold");

        MenuBar menu = new MenuBar();
        menu.addItem("New", e ->  {
            UserEditDialog dialog = new UserEditDialog(false);
            dialog.setWidth(UserEditDialog.MIN_WIDTH);
            dialog.addOpenedChangeListener(newOpened -> {
                if (!newOpened.isOpened()) {
                    updateUsers();
                }
            });
            dialog.open();
        });
        menu.addItem("Edit", e -> {
            if (usersGrid.getSelectedItems().size() == 0) return;
            UserEditDialog dialog = new UserEditDialog(usersGrid.getSelectedItems().iterator().next(), true);
            dialog.setWidth(UserEditDialog.MIN_WIDTH);
            dialog.addOpenedChangeListener(editOpened -> {
                if (!editOpened.isOpened()) {
                    updateUsers();
                }
            });
            dialog.open();
        });
        menu.addItem("Delete", e -> {
            if (usersGrid.getSelectedItems().size() == 0) return;

            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Are you shure?");
            confirmDialog.setText("User will be deleted permanently");
            confirmDialog.addConfirmListener(confirmed -> {
                User user = usersGrid.getSelectedItems().iterator().next();
                user.deleteUserFromDB();
                updateUsers();
            });
            confirmDialog.open();
        });
        menu.addItem("Close", e -> close());
        menu.setSizeUndefined();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        usersGrid.setItems(users);
        usersGrid.addColumn(User::getName).setHeader("Name");
        usersGrid.setSizeFull();
        usersGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        mainLayout.add(title, menu, usersGrid);

        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        add(mainLayout);
    }
}
