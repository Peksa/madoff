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

	public int amount;
	
	// Once payment covers the debt from one or more receipts
	@ManyToMany(cascade=CascadeType.ALL)
	public List<Receipt> receipts;

	/**
	 * Creates new payment
	 * @param payer
	 * @param receiver
	 * @param amount
	 * @param receipts List of receipts this payment is covering
	 */
	public Payment(User payer, User receiver, int amount, List<Receipt> receipts)
	{
		this.payer = payer;
		this.receiver = receiver;
		this.amount = amount;
		this.paid = new Date();
		this.receipts = receipts;
	}

	public String toString()
	{
		return payer + " -> " + receiver + ", amount: " + amount;
	}
}
