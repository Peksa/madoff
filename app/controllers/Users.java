package controllers;

import models.User;
import play.*;
import play.mvc.*;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
public class Users extends CRUD
{
	
	public static void register()
	{
		render();
	}
	
	public static void add(String email, String username, String password, String fullname) throws Throwable
	{
		// TODO(Peksa): why doesn't this work?
		if(validation.hasErrors())
		{
            flash.error("Fix params, bitch");
            params.flash();
            register();
		}
		if (User.count("username = ?", username) > 0)
		{
            flash.error("Username taken :(");
            params.flash();
            register();
		}
		else if (User.count("email = ?", email) > 0)
		{
            flash.error("E-mail taken :(");
            params.flash();
            register();
		}
		else
		{
			
			String hash = Security.sha512Hash(username, password);
			User user = new User(email, username, hash);
			user.fullname = fullname;
			user.save();
			
			flash.keep("url"); // TODO(peksa): wat?
			flash.success("User added, please login!");
			Secure.login();
		}
		
	}
}
