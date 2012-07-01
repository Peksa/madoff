package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import models.Payment;
import models.Receipt;
import models.User;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class) // Require login for controller access
public class Graph extends Controller {
	public static void index() {
		User user = Security.connectedUser();
		if (user != null)
		{
			Set<User> users = new HashSet<User>();
			List<Receipt> allReceipts = Receipt.find("order by created desc").fetch();
			for(Receipt r : allReceipts)
			{
				if(r.members.contains(user))
				{
					users.addAll(r.members);
				}
			}
			
			Set<Payment> payments = new HashSet<Payment>();
			Set<Long> dates = new TreeSet<Long>();
			List<Payment> list = Payment.findAll();
			for(Payment p : list)
			{
				if(users.contains(p.payer) && users.contains(p.receiver))
				{
					payments.add(p);
					dates.add(p.created.getTime());
					if(p.paid != null) dates.add(p.paid.getTime());
				}
			}

			// TODO build epic shit!
			render(user, users, payments, dates);
		}
		else Security.reportToSanta();
	}
}
