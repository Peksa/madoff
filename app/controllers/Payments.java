package controllers;

import models.Payment;
import models.User;
import play.*;
import play.mvc.*;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for controller access
public class Payments extends CRUD
{
	public static void add(Long senderId, Long receiverId, int amount)
	{
		if (validation.hasErrors())
			error("params failed validation.");
		User sender = User.findById(senderId);
		if (Security.isAuthorized(sender))
		{
			User receiver = User.findById(receiverId);
			// TODO(peksa): den här borde haxxa runt med PaidAmounts för en massa kvitton också... antagligen samma för delete.
			Payment payment = new Payment(sender, receiver, amount).save();
		}
		else
		{
			error("Bad dog.");
		}
		
		// TODO(peksa): Return list of payments, or something?
	}
	
	public static void delete(Long id)
	{
		if (validation.hasErrors())
			error("params failed validation.");
		Payment payment = Payment.findById(id);
		if (Security.isAuthorized(payment.payer))
		{
			payment.delete();
		}
		else
		{
			error("Bad bad dog!");
		}
	}
}
