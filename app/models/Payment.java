package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

/**
 * A payment represent a transfer of money from a user to another
 * with their Internet bank or similar. 
 * 
 * The sending user creates the payment, and the receiving user must accept
 * 
 * @author David
 */
@Entity
public class Payment extends Model
{
	public boolean deprecated = false;
	
	@ManyToOne
	public User payer;

	@ManyToOne
	public User receiver;

	// Date of payment and acceptance, accepted == null iff not accepted
	public Date created;
	public Date paid;
	public Date accepted;

	public String identifier; // 10-char bank transaction ID
	
	// Deprecated, use sum of receipts instead
	@Deprecated
	public double amount = 0;
	
	// Once payment covers the debt from one or more receipts
	@ManyToMany(cascade=CascadeType.ALL)
	public List<Receipt> receipts;

	/**
	 * Creates a payment from a receipt
	 */
	public Payment(User payer, User receiver, Receipt receipt)
	{
		this.created = new Date();
		
		this.payer = payer;
		this.receiver = receiver;
		this.identifier = generateId();
		
		List<Receipt> list = new ArrayList<Receipt>();
		list.add(receipt);
		this.receipts = list;
		
		fixDirection();
	}
	
	private String generateId()
	{
		int paymentCounter = Payment.find("payer = ? AND receiver = ? AND deprecated = false", payer, receiver).fetch().size() % 9999 + 1;
		return payer.username.substring(0,Math.min(6,payer.username.length())) 
				+ Integer.toString(paymentCounter);
	}
	
	public double getAmount()
	{
		double amount = 0;
		for(Receipt r : receipts)
		{
			amount += r.shouldPay(payer, receiver);
		}
		return amount;
	}
	
	/**
	 * Reverses the direction of the payment if it gets negative
	 */
	private void fixDirection()
	{
		if(receipts.size() == 0) {
			this.deprecated = true;
			this.paid = new Date();
			return;
		}
		
		// If amount is zero (or very close, doubles..) the payment is auto-settled
		double amount = getAmount();
		
		// If amount is negative, switch direction and generate new ID
		if(amount < 0)
		{
			User tmp = receiver;
			receiver = payer;
			payer = tmp;
			
			this.identifier = generateId();
		}
		
		if(Math.abs(amount) < 0.01)
		{
			// TODO should flag the payment as optimized or something to enable resurrection
			paid = new Date();
			accepted = new Date();
			identifier = "auto-settled";
		}
	}
	
	/**
	 * Changes an existing payment by adding or removing a receipt
	 */
	public Payment(Payment oldPayment, Receipt changeReceipt, boolean addReceiptToPayment)
	{
		copyAttributes(oldPayment);
		if(addReceiptToPayment) {
			receipts.addAll(oldPayment.receipts);
			receipts.add(changeReceipt);
		}
		else {
			for(Receipt receipt : oldPayment.receipts) {
				if(!receipt.equals(changeReceipt)) receipts.add(receipt);
			}
		}
		
		fixDirection();
	}
	
	/**
	 * Copys all attributes of an older payment, except the list of receipts!!
	 */
	private void copyAttributes(Payment oldPayment) {
		oldPayment.deprecated = true;
		oldPayment.paid = new Date(); // The old payment is payed for by the new one (for tracking)
		oldPayment.save();
		this.created = new Date();
		
		this.payer = oldPayment.payer;
		this.receiver = oldPayment.receiver;
		this.identifier = oldPayment.identifier;
		this.receipts = new ArrayList<Receipt>();
	}
	
	/**
	 * Creates a new payment from an old one, use to refresh when editing a receipt included
	 * A new receipt with a brand new ID is created to avoid users paying on depricated information
	 * @param oldPayment
	 */
	public Payment(Payment oldPayment) {
		copyAttributes(oldPayment);
		this.receipts.addAll(oldPayment.receipts);
		fixDirection();
	}

	// Potential concurrency fail, but play is supposed to run single threaded?
	// Make it synchronized and cross our fingers in any case
	public static synchronized void generatePayments(Receipt receipt)
	{
		Set<User> iteratedOwners = new HashSet<User>();
		
		boolean paymentsGenerated = false;
		for(ReceiptOwner o : receipt.owners)
		{
			User owner = o.user;
			iteratedOwners.add(owner);
			for(User member : receipt.members)
			{
				// Do not pay to yourself or previously iterated owners
				if(iteratedOwners.contains(member)) continue;
				if(receipt.shouldPay(member, owner) != 0)
				{
					// Find existing payments to extend
					List<Object> list = Payment.find("deprecated = false AND paid = null AND ((payer = ? AND receiver = ?) OR (payer = ? AND receiver = ?))",
							member, owner, owner, member).fetch();
					if(list.size() > 1)
					{
						// This should not happen
						// TODO better error
						System.out.println("ERROR: Multiple active payments!! " + list.size());
					}
					else if(list.size() > 0)
					{
						Payment oldPayment = (Payment) list.get(0);
						Payment payment = new Payment(oldPayment, receipt, true);
						payment.save();
						paymentsGenerated = true;
					}
					else
					{
						Payment payment = new Payment(member, owner, receipt);
						payment.save();
						paymentsGenerated = true;
					}
				}
			}
		}
		
		// Hack, generate dummy payment TODO remove?
		if(!paymentsGenerated)
		{
			Payment dummy = new Payment(receipt.creator, receipt.creator, receipt);
			dummy.deprecated = true;
			dummy.save();
		}
	}

	public String toString()
	{
		return payer + " -> " + receiver + ", amount: " + amount;
	}
}
