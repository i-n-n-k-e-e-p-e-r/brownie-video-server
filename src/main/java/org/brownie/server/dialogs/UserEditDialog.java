package org.brownie.server.dialogs;

import java.sql.SQLException;

import org.brownie.server.db.User;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.brownie.server.security.SecurityFunctions;

public class UserEditDialog extends Dialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5620752939841369016L;
	
	public static final String MIN_WIDTH = "320px";
	
	
	private boolean edit;
	
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
		save.setWidth("100%");
		save.setDisableOnClick(true);
		save.addClickListener(e -> {
			if (userName.getValue().trim().length() == 0 
					|| !User.isUserNameFreeToUse(userName.getValue().trim())) {
				
				save.setEnabled(true);
				return;
			}
			
//			if (isEdit()) {
//				//TODO
//				return;
//			}
			
			if (!isEdit() && password.getValue().equals(repeatPassword.getValue())) {
				String randomSalt = SecurityFunctions.getRandomUUIDString();
				String saltedHash = SecurityFunctions.getSaltedPasswordHash(password.getValue(), randomSalt);

				User user = new User(userName.getValue(),
						saltedHash,
						randomSalt,
						(isAdmin.getValue() ? User.GROUP.ADMIN : User.GROUP.USER));
				try {
					user.updateUserDBData();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
			close();
			save.setEnabled(true);
		});
		
		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.setWidth("100%");
		cancel.setDisableOnClick(true);
		cancel.addClickListener(e -> close());
		
		buttonsLayout.add(cancel, save);
		
		mainLayout.add(title);
		mainLayout.add(userName);
		mainLayout.add(isAdmin);
		mainLayout.add(password);
		if (isEdit()) {
			mainLayout.add(newPassword);
		}
		mainLayout.add(repeatPassword);
		mainLayout.add(buttonsLayout);
		
		add(mainLayout);
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	
}
