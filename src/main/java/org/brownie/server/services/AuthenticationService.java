package org.brownie.server.services;

import java.io.Serializable;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6990196474584101196L;

	public boolean isValidUser(String user, String pass) {
		// TODO
		if (user.equals("a") && pass.equals("a")) {
			return true;	
		} else {
			return false;
		}
	}
}
