package controllers;

import models.Comment;
import models.Receipt;
import models.User;
import play.*;
import play.mvc.*;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for contoller access
public class Receipts extends CRUD
{
	/**
	 * Show a receipt
	 * 
	 * @param id of receipt
	 */
	public static void show(Long id)
	{
		Receipt receipt = Receipt.findById(id);
		User connectedUser = Security.connectedUser();
		render(receipt, connectedUser);
	}

}
