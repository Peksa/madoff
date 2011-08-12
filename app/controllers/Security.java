package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

import com.sun.corba.se.spi.orbutil.fsm.Input;

import play.libs.Crypto;
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
		return User.find("byUsername", Security.connected()).first();
	}
	
	static boolean authenticate(String username, String password)
	{
		User user = User.find("byUsername", username).first();
		if (user == null || user.password == null)
			return false;

		try
		{
			// FIXME(Peksa): Play only has support for shitty md5, patch at:
			// https://github.com/hhandoko/play/commit/51622677ca9cd8312cd994ae5a3b03524eb3fdc0
			// but it's not yet in an official version of play.
			// String hash = Crypto.passwordHash(password);
			
			String hash = new String(Base64.encodeBase64(MessageDigest.getInstance("SHA-512").digest((username + password).getBytes())));
			if (hash.equals(user.password))
				return true;
		}
		catch (NoSuchAlgorithmException e)
		{
			// FIXME(Peksa): Yea whatever. :p Should throw exception or something, throw error..
			error("Didn't find SHA-512");
			return false;
		}
		return false;
		
	}
}
