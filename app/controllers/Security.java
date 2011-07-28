package controllers;

import models.User;

/**
 * Temporary extension to make referencing easier
 * May later be extended with new features etc
 * 
 * @author dschlyter
 */
public class Security extends Secure.Security {
	
	// To make fetching easier
	public static User connectedUser() {
		return User.find("byUsername", Security.connected()).first();
	}
}
