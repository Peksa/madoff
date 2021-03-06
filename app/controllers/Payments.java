package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import models.Payment;
import models.User;
import models.Receipt;
import play.*;
import play.mvc.*;
import play.i18n.Messages;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for controller access
public class Payments extends Controller
{
	public static void index() {
		User user = Security.connectedUser();
		if (user != null)
		{	
			List<Payment> settled = Payment.find("deprecated = false AND accepted != null AND (payer = ? OR receiver = ?)", user, user).fetch();
			
			// Incomming
			List<Payment> pending = Payment.find("deprecated = false AND payer = ? AND paid != null AND accepted = null", user).fetch();
			List<Payment> liabilities = Payment.find("deprecated = false AND payer = ? AND paid = null", user).fetch();
			
			// Outgoing
			List<Payment> accept = Payment.find("deprecated = false AND receiver = ? AND paid != null AND accepted = null", user).fetch();
			List<Payment> securities = Payment.find("deprecated = false AND receiver = ? AND paid = null", user).fetch();
			
			double liabilitiesTotal = 0.0, securitiesTotal = 0.0;
			for(Payment p : liabilities) liabilitiesTotal += p.getAmount();
			for(Payment p : securities) securitiesTotal += p.getAmount();
			
			render(liabilities, pending, securities, accept, settled, user, liabilitiesTotal, securitiesTotal);
		}
		else Security.reportToSanta();
	}
	
	public static void pay(Long id)
	{
		validation.required(id);
		Payment payment = Payment.findById(id);
		if(payment == null) Security.reportToSanta();
		if(!validate(payment.payer.id)) return;
		if(payment.paid != null) Security.reportToSanta();
		
		if(payment.deprecated)
		{
			flash.error(Messages.get("error.deprecatedPayment"));
		}
		else
		{
			payment.paid = new Date();
			payment.save();
		}
		
		index();
	}

	/**
	 * Marks a payment as accepted
	 * @param userId
	 * @param id
	 */
	public static void accept(Long id) {
		validation.required(id);
		Payment payment = Payment.findById(id);
		if(payment == null) Security.reportToSanta();
		if(!validate(payment.receiver.id)) return;
		if(payment.paid == null || payment.accepted != null) Security.reportToSanta();

		payment.accepted = new Date();
		payment.save();

		index();
	}

	/**
	 * Verify that user ID is same as authorized ID and input has no errors
	 * @param userId Id of user
	 * @return true iff validation is successful
	 */
	static boolean validate(Long userId) {
		if (validation.hasErrors()) {
			error(Messages.get("validateFail"));
			return false;
		}

		User user = User.findById(userId);
		if (user == null || !Security.isAuthorized(user)) {
			Security.reportToSanta();
			return false;
		}

		return true;
	}
}
