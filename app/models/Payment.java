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
	@ManyToOne
	public User payer;

	@ManyToOne
	public User receiver;

	// Date of payment and acceptance, accepted == null iff not accepted
	public Date paid;
	public Date accepted;

	public String identifier; // 10-char bank transaction ID
	
	public int amount;
	public int unsourced; // amount of debt not traceable to fresh receipts
	
	// Once payment covers the debt from one or more receipts
	@ManyToMany(cascade=CascadeType.ALL)
	public List<Receipt> receipts;

	/**
	 * Creates new payment
	 * @param payer
	 * @param receiver
	 * @param identifier a 10 char identifier for this payment
	 * @param amount
	 * @param unsourced money not trackable to related receipts
	 * @param receipts List of receipts this payment is covering
	 */
	public Payment(User payer, User receiver, String identifier, int amount, int unsourced, List<Receipt> receipts)
	{
		this.payer = payer;
		this.receiver = receiver;
		this.identifier = identifier;
		this.amount = amount;
		this.paid = new Date();
		this.receipts = receipts;
	}

	public String toString()
	{
		return payer + " -> " + receiver + ", amount: " + amount;
	}
}
