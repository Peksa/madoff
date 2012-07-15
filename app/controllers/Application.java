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
	public static void index(boolean all)
	{
		List<Receipt> allReceipts = Receipt.find("deleted != true order by created desc").fetch();

		User user = Security.connectedUser();

		int receiptHidden = 0;

		// Moar opt to do with DB query, but KISS
		List<Receipt> receipts = new ArrayList<Receipt>();
		for(Receipt r : allReceipts)
		{
			if(r.members.contains(user))
			{
				if(all == false && r.isFinished()) receiptHidden++;
				else receipts.add(r);
			}
		}

		render(receipts, user, receiptHidden);
	}
}