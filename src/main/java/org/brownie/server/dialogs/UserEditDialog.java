package org.brownie.server.dialogs;

import java.sql.SQLException;

import org.brownie.server.db.User;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

public class UserEditDialog extends Dialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5620752939841369016L;
	
	public static final String MIN_HEIGHT = "500px";
	public static final String MIN_WIDTH = "400px";
	
	
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
		
		setMinHeight(MIN_HEIGHT);
		setMinWidth(MIN_WIDTH);
	}
	
	private void init() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		
		Label title = new Label();
		title.setText("Edit user");
		title.setWidthFull();
		if (!isEdit()) {
			title.setText("New user registration");
		}
		mainLayout.add(title);
		
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
		
		mainLayout.add(userName);
		mainLayout.add(password);
		if (isEdit()) {
			mainLayout.add(newPassword);
		}
		mainLayout.add(repeatPassword);
		
		Checkbox isAdmin = new Checkbox();
		isAdmin.setLabel("Administrator");
		isAdmin.setWidth("100%");
		isAdmin.setValue(true);
		mainLayout.add(isAdmin);
		
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
				return;
			}
			
//			if (isEdit()) {
//				//TODO
//				return;
//			}
			
			if (!isEdit() && password.getValue().equals(repeatPassword.getValue())) {
				User user = new User(userName.getValue(), 
						password.getValue(), 
						(isAdmin.getValue() ? User.GROUP.ADMIN : User.GROUP.USER));
				try {
					user.updateUserDBData();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});
		
		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.setWidth("100%");
		cancel.setDisableOnClick(true);
		cancel.addClickListener(e -> {
			close();
		});
		
		buttonsLayout.add(cancel, save);
		
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
