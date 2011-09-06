package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.Payment;
import models.User;
import models.Receipt;
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
	// Helper for index
	static void increment(HashMap<Long,Integer> map, Long key, Integer value) {
		if(map.containsKey(key)) map.put(key, map.get(key) + value);
		else map.put(key, value);
	}
	
	/**
	 * Provides a list of all the users debts per person, as well as incoming debts
	 * This matches incoming and outgoing debts over time and settles any difference
	 * @param userId
	 */
	public static void index(Long userId) {
		validation.required(userId);
		if(!validate(userId)) return;
		
		
		//TODO(dschlyter) This operation is pretty expensive, but premature optimization is the root of all evil etc
		// Opt Idea 1: Cache result and invalidate on new receipt with user involved
		// Opt Idea 2: Cache result and date and only use new data to re-calculate (harder)
		
		User user = User.findById(userId);
		HashMap<Long, Integer> debt = new HashMap<Long, Integer>();
		// Track debt that is linked to fresh receipts
		HashMap<Long, Integer> freshDebt = new HashMap<Long, Integer>(); 
		
		// Sum all receipts where you owe money, subtract sum of all receipts owned
		for(Receipt r : user.incomingReceipts) {
			int total = r.getTotal(user);
			increment(debt, r.owner.id, total);
			if(r.hasPayment(user)) increment(freshDebt, r.owner.id, total);
		}
		for(Receipt r : user.receipts) {
			for(User u : r.members) {
				int total = r.getTotal(u);
				increment(debt, u.id, -total);
				if(r.hasPayment(u)) increment(freshDebt, u.id, -total);
			}
		}
		
		// Sum all payments received, subtract sum of all payments made
		for(Payment p : user.incomingPayments) {
			increment(debt, p.payer.id, p.amount);
		}
		for(Payment p : user.payments) {
			increment(debt, p.receiver.id, -p.amount);
		}
		
		// TODO maybe remove 0 summed
		// TODO render(debt, freshDebt)
	}

	
	/**
	 * Adds a new payment
	 * @param senderId
	 * @param receiverId
	 * @param receiptId A list of receipts this payment corresponds to
	 * @param amount
	 */
	public static void add(Long senderId, Long receiverId, List<Long> receiptId, int amount)
	{
		validation.required(senderId);
		validation.required(receiverId);
		validation.required(amount);
		if(!validate(senderId)) return;
		
		List<Receipt> receipts = new ArrayList<Receipt>();
		
		for (Long id : receiptId) 
		{
			Receipt r = Receipt.findById(id);
			receipts.add(r);
		}
		
		User receiver = User.findById(receiverId);
		Payment payment = new Payment(Security.connectedUser(), receiver, amount, receipts);
		payment.save();

		// Do nothing, this should be accessed by ajax?
	}
	
	/**
	 * List incoming payments that are to be accepted
	 * @param userId
	 */
	public static void listIncomming(Long userId) {
		validation.required(userId);
		if(!validate(userId)) return;
		
		List<Payments> incoming = Payment.find("receiver = ? AND accepted = NULL", userId).fetch();
		//TODO render(incoming)
	}
	
	/**
	 * List the complete history of payments done
	 * @param userId
	 */
	public static void listHistory(Long userId) {
		validation.required(userId);
		if(!validate(userId)) return;
		
		//TODO(dschlyter) later - not critical
	}
	
	/**
	 * Marks a payment as accepted
	 * @param userId
	 * @param paymentId
	 */
	public static void accept(Long userId, Long paymentId) {
		validation.required(userId);
		validation.required(paymentId);
		if(!validate(userId)) return;
		
		Payment payment = Payment.findById(paymentId);
		payment.accepted = new Date();
		payment.save();
	}
	
	/**
	 * Verify that user ID is same as authorized ID and input has no errors
	 * @param userId Id of user
	 * @return true iff validation is successful
	 */
	static boolean validate(Long userId) {
		if (validation.hasErrors()) {
			error("Params failed validation.");
			return false;
		}
		
		User user = User.findById(userId);
		if (user == null || !Security.isAuthorized(user)) {
			error("User not authorized");
			return false;
		}
			
		return true;
	}
}
