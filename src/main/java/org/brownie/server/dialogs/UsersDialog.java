package org.brownie.server.dialogs;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.Query;
import org.brownie.server.Application;
import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;
import org.brownie.server.views.CommonComponents;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class UsersDialog extends Dialog {

    public static final String MIN_WIDTH = "420px";
    public static final String MIN_HEIGHT = "340px";
    private static final List<User> users = Collections.synchronizedList(new ArrayList<>());
    private static final List<Grid<User>> gridsForUpdate = Collections.synchronizedList(new ArrayList<>());

    private final Grid<User> usersGrid = new Grid<>();

    public UsersDialog() {
        super();

        updateUsers();
        init();

        this.setMinWidth(MIN_WIDTH);
        this.setMinHeight(MIN_HEIGHT);
        this.setWidth("95%");
        this.setHeight("95%");
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
        this.setResizable(true);
        this.setModal(true);
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
            new Thread(() -> gridsForUpdate.parallelStream().forEach(grid -> {
                if (grid == null || grid.getDataProvider() == null) return;

                var ui = grid.getUI().isPresent() ? grid.getUI().get() : null;
                if (ui != null) ui.access(() -> {
                    Set<?> selected = grid.getSelectedItems();
                    grid.getDataProvider().refreshAll();

                    grid.getDataProvider()
                            .fetch(new Query<>())
                            .collect(Collectors.toSet())
                            .forEach(item -> {
                                if (selected.stream().anyMatch(
                                        u -> ((User) u).getUserId().intValue() == item.getUserId().intValue())) {
                                    grid.select(item);
                                }
                    });
                });
            })).start();
        } catch (SQLException e) {
            e.printStackTrace();
            Application.LOGGER.log(System.Logger.Level.ERROR, "Error while loading users.", e);
        }
    }

    private void init() {
        VerticalLayout mainLayout = new VerticalLayout();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidthFull();
        titleLayout.setAlignItems(FlexComponent.Alignment.START);
        Label title = new Label("Users manager");
        title.getStyle().set("font-weight", "bold");
        titleLayout.add(title);

        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.setWidthFull();
        menuLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        menuLayout.add(
                CommonComponents.createButton("New", VaadinIcon.PLUS_CIRCLE_O.create(),
                        e -> openNewDialog()),
                CommonComponents.createButton("Edit", VaadinIcon.ELLIPSIS_CIRCLE_O.create(),
                        e -> openEditDialog(usersGrid)),
                CommonComponents.createButton("Delete", VaadinIcon.MINUS_CIRCLE_O.create(),
                        e -> openDeleteDialog(usersGrid))
        );

        usersGrid.setItems(users);
        Grid.Column<?> usersColumn = usersGrid.addColumn(User::getName);
        usersColumn.setHeader("Name");
        usersColumn.setSortable(true);
        Grid.Column<?> statusColumn = usersGrid.addComponentColumn(user -> {
            if (user.getGroup() == User.GROUP.ADMIN.ordinal()) return new Label("Administrators");
            return new Label("Users");
        });
        statusColumn.setHeader("Group");

        usersGrid.setSizeFull();
        usersGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        mainLayout.add(
                titleLayout,
                menuLayout,
                usersGrid,
                CommonComponents.createButton("Close", VaadinIcon.CLOSE_CIRCLE.create(), e -> close()));

        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        add(mainLayout);
    }

    private void openNewDialog() {
        UserEditDialog dialog = new UserEditDialog(false);

        dialog.addOpenedChangeListener(newOpened -> {
            if (!newOpened.isOpened()) {
                updateUsers();
            }
        });

        dialog.open();
    }

    private void openEditDialog(Grid<User> usersGrid) {
        if (usersGrid.getSelectedItems().size() == 0) return;
        UserEditDialog dialog = new UserEditDialog(usersGrid.getSelectedItems().iterator().next(), true);

        dialog.addOpenedChangeListener(editOpened -> {
            if (!editOpened.isOpened()) {
                updateUsers();
            }
        });

        dialog.open();
    }

    private void openDeleteDialog(Grid<User> usersGrid) {
        if (usersGrid.getSelectedItems().size() == 0) return;

        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Are you shure?");
        confirmDialog.setText("User will be deleted permanently");
        confirmDialog.setRejectable(true);
        confirmDialog.addRejectListener(rEvent -> confirmDialog.close());
        confirmDialog.addConfirmListener(confirmed -> {
            User user = usersGrid.getSelectedItems().iterator().next();
            user.deleteUserFromDB(DBConnectionProvider.getInstance());
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "Deleted user '" + user.getName() + "'");
            updateUsers();
        });
        confirmDialog.open();
    }
}
