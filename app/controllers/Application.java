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
		List<Receipt> allReceipts = Receipt.find("order by created desc").fetch();
		
		User authed = Security.connectedUser();
		
		// Moar opt to do with DB query, but KISS
		List<Receipt> receipts = new ArrayList<Receipt>();
		for(Receipt r : allReceipts)
		{
			if(r.members.contains(authed)) receipts.add(r);
		}
		
		render(receipts, authed);
	}
}