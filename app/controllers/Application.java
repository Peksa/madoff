package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@With(Secure.class) // Require login for contoller access
public class Application extends Controller
{
	/**
	 * Show index-page
	 */
	public static void index()
	{
		List<Receipt> receipts = Receipt.find("order by created asc").fetch();
		render(receipts);
	}

	/**
	 * Show a receipt
	 * 
	 * @param id of receipt
	 */
	public static void receipt(Long id)
	{
		Receipt receipt = Receipt.findById(id);
		User connectedUser = Security.connectedUser();
		render(receipt, connectedUser);
	}
}