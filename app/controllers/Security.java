package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

import com.sun.corba.se.spi.orbutil.fsm.Input;

import play.libs.Crypto;
import play.i18n.Messages;
import util.BCrypt;
import models.User;

/**
 * Temporary extension to make referencing easier
 * May later be extended with new features etc
 * 
 * @author dschlyter
 */
public class Security extends Secure.Security
{
	
	// To make fetching easier
	public static User connectedUser()
	{
		return User.find("byUsernameIlike", Security.connected()).first();
	}
	
	/**
	 * Check if any of the arguments is the connected user
	 * @param users list of users that are authorized
	 * @return true if connected user is one of the arguments, otherwise false.
	 */
	public static boolean isAuthorized(User ... users)
	{
		User connected = connectedUser();
		if (connected == null)
			return false;
		
		for (User user : users)
			if (connected.equals(user))
				return true;
				
		return false;
	}
	
	protected static String legacyHash(String username, String password) throws NoSuchAlgorithmException
	{
		return new String(Base64.encodeBase64(MessageDigest.getInstance("SHA-512").digest((username + password).getBytes())));
	}
	
	protected static String hash(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(12));
	}
	
	protected static boolean authenticateHash(String password, String savedHash) {
		return BCrypt.checkpw(password, savedHash);
	}
	
	static boolean authenticate(String username, String password)
	{
		if (validation.hasErrors())
			error(Messages.get("paramsFail"));
		User user = User.find("byUsernameIlike", username).first();
		if (user == null || user.password == null)
			return false;

		try
		{
			String legacyHash = legacyHash(user.username, password);
			
			// Check against legacy hashed password, and upgrade to bcrypt on match
			if (legacyHash.equals(user.password)) {
				user.password = hash(password);
				user.save();
				System.out.println("Upgraded!");
				return true;
			} 
			// Check password with new hash
			else if(authenticateHash(password, user.password)) {
				return true;
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			error("Couldn't find SHA-512");
			return false;
		}
		return false;
		
	}
	
	static void reportToSanta()
	{
		error(Messages.get("controllers.Payments.validate.unauthorized"));
	}
}
