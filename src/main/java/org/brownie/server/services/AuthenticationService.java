package org.brownie.server.services;

import java.io.Serializable;
import java.sql.SQLException;

import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;
import org.brownie.server.dialogs.UserEditDialog;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6990196474584101196L;

	public User getValidUser(String user, String pass) {
		User result = null;
		
		try {
			if (DBConnectionProvider.getInstance().getOrmDaos().get(User.class.getClass()).countOf() == 0) {
				result = null;
				UserEditDialog newUserDialog = new UserEditDialog(false);
				newUserDialog.setWidth(UserEditDialog.MIN_WIDTH);
				newUserDialog.open();
			} else {
				result = ((User)DBConnectionProvider
						.getInstance()
						.getOrmDaos()
						.get(User.class.getClass())
							.queryForEq("name", user)
							.iterator()
							.next());
				
				if (!result.getPassword().equals(pass)) {
					result = null;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
