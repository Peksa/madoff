package controllers;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import models.Payment;
import models.Picture;
import models.User;
import play.*;
import play.mvc.*;
import play.mvc.Router.ActionDefinition;
import play.vfs.VirtualFile;
import play.i18n.Messages;
import util.BCrypt;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
public class Users extends Controller
{
	public static void register()
	{
		boolean register = true;
		String title = "Register";
		ActionDefinition destination = Router.reverse("Users.add");
		
		render(register, title, destination);
	}
	
	public static void add(String email, String username, String password, String repeatPassword, String fullname, String bankName, String accountNo, String clearingNo, int idiotTest) throws Throwable
	{
		// Quick fix
		if(fullname == null || fullname.length() == 0) fullname = username;
		if(email == null) email = "";
		
		// Remove all crap from account no:s
		accountNo = accountNo.replaceAll("[^0-9]", "");
		clearingNo = clearingNo.replaceAll("[^0-9]", "");
		
		String regex = "[A-Za-z0-9_]+";
		
		String error = null;
		
		if (username.length() < 3) error = Messages.get("errors.shortUsername");
		else if (!username.matches(regex)) error = "Username must match regex " + regex;
		else if (User.count("byUsernameIlike", username) > 0) error = (Messages.get("errors.TakenUsername"));
		else if (User.count("email = ?", email) > 0) error = Messages.get("errors.TakenEmail");
		else if (idiotTest != 4711) error = Messages.get("errors.idiot");
		else if (password == null) error = Messages.get("errors.shortPass");
		else error = commonValidation(email, password, repeatPassword, fullname, bankName, accountNo, clearingNo);
	
		if(error != null) {
			flash.error(error);
			params.flash();
            register();
		}
		else
		{
			String hash = Security.hash(password);
			
			User user = new User(email, username, hash);
			user.fullname = fullname;
			user.bankName = bankName;
			user.accountNo = accountNo;
			user.clearingNo = clearingNo;
			user.save();
			
			flash.keep("url"); // TODO(peksa): wat?
			flash.success(Messages.get("controllers.Users.success"));
			Secure.login();
		}
		
		
	}
	
	private static String commonValidation(String email, String password, String repeatPassword, String fullname, String bankName, String accountNo, String clearingNo) {
		if(validation.hasErrors()) return Messages.get("errors.Parameters");
		
		if(password != null) { // Do not validate a null password, may be a update without changing password
			if (password.length() < 8) return Messages.get("errors.shortPass");
			if (!password.equals(repeatPassword)) return "Passwords does not match";
		}
		if (fullname == null || fullname.length() < 3) return "Full name required";
		if (bankName == null || bankName.length() < 3) return "Bank name required";
		if (accountNo == null || accountNo.length() < 4) return "Account no (numeric) required";
		if (clearingNo == null || clearingNo.length() < 4) return "Clearing no (numeric) required";
		
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			return "Email is invalid";
		}

		return null;
	}
	
	public static void index() {
		show(Security.connectedUser().username);
	}
	
	public static void show(String username) {
		User user = User.find("username = ?", username).first();
		
		boolean showBankAccount = false;
		User currentUser = Security.connectedUser();
		if(currentUser.id == user.id) showBankAccount = true;
		else {
			int paymentCount = Payment.find("deprecated = false AND payer = ? AND receiver = ? AND paid = null", currentUser, user).fetch().size();
			if(paymentCount > 0) showBankAccount = true;
		}
		
		boolean showEdit = currentUser.id == user.id || currentUser.admin;
		boolean showEmail = currentUser.id == user.id || currentUser.admin || showBankAccount;
		
		render(user, showBankAccount, showEdit, showEmail);
	}
	
	public static void edit(String username) {
		User currentUser = Security.connectedUser();
		User user = User.find("username = ?", username).first();
		if(!user.id.equals(currentUser.id) && !currentUser.admin) Security.reportToSanta();
		
		flash.put("email", user.email);
		flash.put("fullname", user.fullname);
		flash.put("bankName", user.bankName);
		flash.put("accountNo", user.accountNo);
		flash.put("clearingNo", user.clearingNo);
		
		renderEdit(user);
	}
	
	private static void renderEdit(User user) {
		boolean register = false;
		String title = "Edit user";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("username", user.username);
		ActionDefinition destination = Router.reverse("Users.update", args);
		
		render("Users/register.html", register, title, destination);
	}
	
	public static void update(String username, String ownPassword, String email, String password, String repeatPassword, String fullname, String bankName, String accountNo, String clearingNo, int idiotTest) {
		User currentUser = Security.connectedUser();
		User user = User.find("username = ?", username).first();
		if(!user.id.equals(currentUser.id) && !currentUser.admin) Security.reportToSanta();
		
		if(password != null && password.length() == 0) password = null; // Does not change password, and validate as empty
		accountNo = accountNo.replaceAll("[^0-9]", "");
		clearingNo = clearingNo.replaceAll("[^0-9]", "");

		String error = null;
		if(!Security.authenticate(currentUser.username, ownPassword)) error = "Current password did not match";
		else {
			error = commonValidation(email, password, repeatPassword, fullname, bankName, accountNo, clearingNo);
		}
			
		if(error != null) {
			flash.error(error);
			params.flash();
			renderEdit(user);
		}
		else
		{
			if(password != null) {
				user.password = Security.hash(password);
			}
			
			user.email = email;
			user.fullname = fullname;
			user.bankName = bankName;
			user.accountNo = accountNo;
			user.clearingNo = clearingNo;
			user.save();
			show(username);
		}
		// TODO change password if changes
			
		
	}
}
