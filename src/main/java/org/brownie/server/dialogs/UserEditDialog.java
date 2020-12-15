package org.brownie.server.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.brownie.server.Application;
import org.brownie.server.db.User;
import org.brownie.server.security.SecurityFunctions;

import java.sql.SQLException;

public class UserEditDialog extends Dialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5620752939841369016L;
	
	public static final String MIN_WIDTH = "360px";
	public static final String MIN_HEIGHT = "320px";

	private boolean edit;
	private User user = null;

	public UserEditDialog(User user, boolean edit) {
		super();

		setUser(user);
		setEdit(edit);

		init();

		setModal(true);
		setDraggable(false);
		setResizable(false);
		setCloseOnOutsideClick(true);
		setCloseOnEsc(true);

		setMinWidth(MIN_WIDTH);
		setMinHeight(MIN_HEIGHT);
		setWidth(MIN_WIDTH);
		setHeight("-1");
	}

	public UserEditDialog(boolean edit) {
		super();

		setEdit(edit);

		init();

		setModal(true);
		setDraggable(false);
		setResizable(false);
		setCloseOnOutsideClick(true);
		setCloseOnEsc(true);

		setMinWidth(MIN_WIDTH);
		setMinHeight(MIN_HEIGHT);
		setWidth(MIN_WIDTH);
		setHeight("-1");
	}

	private void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	private void init() {
		FormLayout mainLayout = new FormLayout();
		mainLayout.setSizeFull();

		Label title = new Label();
		title.setText("Edit user");
		title.getStyle().set("font-weight", "bold");
		title.setWidthFull();
		if (!isEdit()) {
			title.setText("New user registration");
		}

		TextField userName = new TextField();
		userName.setWidth("100%");
		userName.setLabel("User name:");

		PasswordField password = new PasswordField();
		password.setWidth("100%");
		password.setLabel("Password:");

		PasswordField newPassword = new PasswordField();
		newPassword.setWidth("100%");
		newPassword.setLabel("New password:");

		PasswordField repeatPassword = new PasswordField();
		repeatPassword.setWidth("100%");
		repeatPassword.setLabel("Repeat password:");

		Checkbox isAdmin = new Checkbox();
		isAdmin.setLabel("Administrator");
		isAdmin.setWidth("100%");
		isAdmin.setValue(true);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("100%");

		Button save = new Button();
		save.setText("Save");
		save.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
		save.setWidth("100%");
		save.setDisableOnClick(true);
		save.addClickListener(e -> {
			if (!checkInput(userName, password, newPassword, repeatPassword)) {
				save.setEnabled(true);
				return;
			}
			if (isEdit()) {
				if (getUser() != null) saveUser(newPassword.getValue(), isAdmin.getValue());
			} else {
				createNewUser(userName.getValue(), password.getValue(), isAdmin.getValue());
			}
			save.setEnabled(true);
			close();
		});

		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.setIcon(VaadinIcon.CLOSE_CIRCLE_O.create());
		cancel.setWidth("100%");
		cancel.setDisableOnClick(true);
		cancel.addClickListener(e -> close());

		buttonsLayout.add(cancel, save);

		mainLayout.add(title);
		mainLayout.add(userName);
		mainLayout.add(isAdmin);
		mainLayout.add(password);
		if (isEdit()) {
			userName.setEnabled(false);
			mainLayout.add(newPassword);
		}
		mainLayout.add(repeatPassword);
		mainLayout.add(buttonsLayout);

		add(mainLayout);

		if (getUser() != null) {
			userName.setValue(getUser().getName());
			isAdmin.setValue(getUser().getGroup() == User.GROUP.ADMIN.ordinal());
		}

		if (User.getAdminsCount() == 0) {
			isAdmin.setValue(true);
			isAdmin.setEnabled(false);
		}
	}

	private boolean checkInput(TextField userName,
							   PasswordField password,
							   PasswordField newPassword,
							   PasswordField repeatPassword) {

		if (userName.getValue().trim().length() == 0) {
			Notification.show("Enter user name");
			return false;
		}

		if (isEdit()) {
			if (newPassword.getValue().length() == 0 || repeatPassword.getValue().length() == 0) {
				Notification.show("Check the passwords");
				return false;
			}

			if (!getUser().getPasswordHash().equals(SecurityFunctions.getSaltedPasswordHash(password.getValue(),
					getUser().getRandom()))) {
				Notification.show("Invalid old password");
				return false;
			}

			if (!newPassword.getValue().equals(repeatPassword.getValue())) {
				Notification.show("New passwords are not equals");
				return false;
			}
		} else {
			if (password.getValue().length() == 0 || repeatPassword.getValue().length() == 0) {
				Notification.show("Check the passwords");
				return false;
			}

			if (!User.isUserNameFreeToUse(userName.getValue().trim())) {
				Notification.show("User name is not allowed");
				return false;
			}

			if (!password.getValue().equals(repeatPassword.getValue())) {
				Notification.show("Passwords are not equals");
				return false;
			}
		}
		return true;
	}

	private void createNewUser(String userName, String password, boolean isAdmin) {
		String randomSalt = SecurityFunctions.getRandomUUIDString();
		String saltedHash = SecurityFunctions.getSaltedPasswordHash(password, randomSalt);

		Application.LOGGER.log(System.Logger.Level.INFO,
				"Creating new user '" + userName + "'");

		User user = new User(userName,
				saltedHash,
				randomSalt,
				(isAdmin ? User.GROUP.ADMIN.ordinal() : User.GROUP.USER.ordinal()));
		try {
			user.updateUserDBData();
		} catch (SQLException e1) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Error while creating user '" + userName + "'",
					e1);
			e1.printStackTrace();
		}
	}

	private void saveUser(String newPassword, boolean isAdmin) {
		getUser().setGroup(isAdmin ? User.GROUP.ADMIN.ordinal() : User.GROUP.USER.ordinal());
		String saltedHash = SecurityFunctions.getSaltedPasswordHash(newPassword, getUser().getRandom());
		getUser().setPasswordHash(saltedHash);

		Application.LOGGER.log(System.Logger.Level.INFO,
				"Updating user '" + user.getName() + "'");

		try {
			user.updateUserDBData();
		} catch (SQLException e1) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Error while saving user '" + user.getName() + "'",
					e1);
			e1.printStackTrace();
		}
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	
}
