package org.brownie.server.services;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.brownie.server.Application;
import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;
import org.brownie.server.dialogs.UserEditDialog;
import org.brownie.server.security.SecurityFunctions;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6990196474584101196L;

	public User getValidUser(String userName, String password) {
		User authenticatedUser = null;
		
		try {
			if (User.getAdminsCount(DBConnectionProvider.getInstance()) == 0) {
				Application.LOGGER.log(System.Logger.Level.WARNING,
						"Need to create very first admin user!");

				UserEditDialog newUserDialog = new UserEditDialog(false);
				newUserDialog.setWidth(UserEditDialog.MIN_WIDTH);
				newUserDialog.open();

				return null;
			}

			List<?> users = (DBConnectionProvider
					.getInstance()
					.getOrmDaos()
					.get(User.class)).queryForEq("name", userName);
			if (users != null && users.size() > 0) {
				authenticatedUser = (User)users.iterator().next();
				String hashFromForm = SecurityFunctions.getSaltedPasswordHash(password, authenticatedUser.getRandom());

				if (!authenticatedUser.getPasswordHash().equals(hashFromForm)) {
					authenticatedUser = null;
				}
			}
		} catch (SQLException e) {
			Application.LOGGER.log(System.Logger.Level.ERROR, "Error while authenticating user " + userName, e);
			e.printStackTrace();
		}

		if (authenticatedUser == null) {
			Application.LOGGER.log(System.Logger.Level.WARNING,
					"Authentication FAILED for user '" + userName + "'");
		} else {
			Application.LOGGER.log(System.Logger.Level.INFO,
					"Authentication SUCCEED for user '" + userName + "'");
		}
		return authenticatedUser;
	}
}
