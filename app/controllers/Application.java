package controllers;

import play.*;
import play.mvc.*;
import play.i18n.*;

import java.util.*;

import models.*;

@With(Secure.class) // Require login for controller access
public class Application extends Controller
{	
	/**
	 * Show index-page
	 */
	public static void index()
	{
		List<Receipt> receipts = Receipt.find("order by created asc").fetch();
		User authed = Security.connectedUser();
		render(receipts, authed);
	}
}