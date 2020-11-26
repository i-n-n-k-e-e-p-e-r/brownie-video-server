package org.brownie.server.services;

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.catalina.User;
import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.dialogs.UserEditDialog;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6990196474584101196L;

	public boolean isValidUser(String user, String pass) {
		boolean result = false;
		
		try {
			if (DBConnectionProvider.getInstance().getOrmDaos().get(User.class.getClass()).countOf() == 0) {
				result = false;
				UserEditDialog newUserDialog = new UserEditDialog(false);
				newUserDialog.setWidth(UserEditDialog.MIN_WIDTH);
				newUserDialog.setHeight(UserEditDialog.MIN_HEIGHT);
				newUserDialog.open();
			} else {
				String passFromDB = ((User)DBConnectionProvider
						.getInstance()
						.getOrmDaos()
						.get(User.class.getClass())
							.queryForEq("name", user)
							.iterator()
							.next()).getPassword();
				if (passFromDB.equals(pass)) {
					result = true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
