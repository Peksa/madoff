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
	
	public double amount;
	
	// Once payment covers the debt from one or more receipts
	@ManyToMany(cascade=CascadeType.ALL)
	public List<Receipt> receipts;

	/**
	 * Creates a payment from a receipt
	 */
	public Payment(User payer, User receiver, String identifier, Receipt receipt)
	{
		this.created = new Date();
		
		this.payer = payer;
		this.receiver = receiver;
		this.identifier = generateId(payer, receiver);
		this.amount = receipt.shouldPay(payer, receiver);
		
		List<Receipt> list = new ArrayList<Receipt>();
		list.add(receipt);
		this.receipts = list;
	}
	
	private String generateId(User payer, User receiver)
	{
		int paymentCounter = Payment.find("payer = ? AND receiver = ? AND deprecated = false", payer, receiver).fetch().size() % 9999 + 1;
		return payer.username.substring(0,Math.min(6,payer.username.length())) 
				+ Integer.toString(paymentCounter);
	}
	
	/*
	 * Adds a new receipt to a payment and 
	 */
	public Payment(Payment oldPayment, Receipt addedReceipt)
	{
		oldPayment.deprecated = true;
		this.created = new Date();
		
		this.payer = oldPayment.payer;
		this.receiver = oldPayment.receiver;
		this.identifier = oldPayment.identifier;
		this.amount = oldPayment.amount;
		this.receipts = oldPayment.receipts;
		
		receipts.add(addedReceipt);
		amount += addedReceipt.shouldPay(payer, receiver);
	}

	public String toString()
	{
		return payer + " -> " + receiver + ", amount: " + amount;
	}
}
