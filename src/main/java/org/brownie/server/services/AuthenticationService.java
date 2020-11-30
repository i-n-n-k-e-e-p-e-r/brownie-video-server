package org.brownie.server.services;

import java.io.Serializable;
import java.sql.SQLException;

import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;
import org.brownie.server.dialogs.UserEditDialog;
import org.brownie.server.security.SecurityFunctions;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6990196474584101196L;

	public User getValidUser(String userName, String password) {
		User authenticatedUser = null;
		
		try {
			if (DBConnectionProvider.getInstance().getOrmDaos().get(User.class).countOf() == 0) {
				UserEditDialog newUserDialog = new UserEditDialog(false);
				newUserDialog.setWidth(UserEditDialog.MIN_WIDTH);
				newUserDialog.open();
			} else {
				authenticatedUser = ((User)DBConnectionProvider
						.getInstance()
						.getOrmDaos()
						.get(User.class)
							.queryForEq("name", userName)
							.iterator()
							.next());

				String hashFromForm = SecurityFunctions.getSaltedPasswordHash(password, authenticatedUser.getRandom());

				if (!authenticatedUser.getPasswordHash().equals(hashFromForm)) {
					authenticatedUser = null;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return authenticatedUser;
	}
}
