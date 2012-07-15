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
			// Add all users the current user has shared a receipt with
			Set<User> users = new HashSet<User>();
			List<Receipt> allReceipts = Receipt.find("order by created desc").fetch();
			for(Receipt r : allReceipts)
			{
				if(r.members.contains(user))
				{
					users.addAll(r.members);
				}
			}
			
			// TODO unique events 
			
			Set<Payment> payments = new HashSet<Payment>();
			Set<GraphEvent> events = new TreeSet<GraphEvent>();
			List<Payment> list = Payment.findAll();
			for(Payment p : list)
			{
				if(users.contains(p.payer) && users.contains(p.receiver))
				{
					payments.add(p);
					// Add an event when the payment was created, add the last receipt to that event
					// If the payment also was paid, add an event for that as well
					Receipt lastReceipt = p.receipts.get(p.receipts.size()-1);
					events.add(new GraphEvent(p.created.getTime(), lastReceipt.id, lastReceipt.title));
					if(p.paid != null && !p.deprecated) events.add(new GraphEvent(p.paid.getTime(), -1, ""));
				}
			}
			
			// Filter duplicate events for the same receipt
			Set<GraphEvent> filtered = new TreeSet<GraphEvent>();
			GraphEvent lastEvent = null;
			for(GraphEvent event : events) {
				// Only add the last event with matching receiptId to the set
				if(lastEvent != null && lastEvent.receiptId != event.receiptId) {
					filtered.add(lastEvent);
				}
				lastEvent = event;
			}
			// Add the final event to the filtered set since it will not be processed above
			if(lastEvent != null) filtered.add(lastEvent);
			events = filtered;

			render(user, users, payments, events);
		}
		else Security.reportToSanta();
	}
	
	public static class GraphEvent implements Comparable<GraphEvent>
	{
		public long time;
		public long receiptId;
		public String name;
		
		public GraphEvent(long time, long receiptId, String name) {
			super();
			this.time = time;
			this.receiptId = receiptId;
			this.name = name;
		}

		@Override
		public int compareTo(GraphEvent o) {
			return (int) Math.signum(time - o.time);
		}
	}
}
