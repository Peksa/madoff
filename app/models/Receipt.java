package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import org.eclipse.jdt.internal.core.util.MementoTokenizer;

import java.util.*;

@Entity
public class Receipt extends Model
{
	public String title;
	public Date created;

	//public double total; // deprecated, use sum of owner amount
	public double tip;

	@ManyToOne
	public User creator;
	
	// TODO merge owner and member!
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<ReceiptOwner> owners;

	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<User> members;

	@Lob
	public String description;

	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Comment> comments;

	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Subpot> subpots;

	@ManyToMany(mappedBy = "receipts", cascade=CascadeType.ALL)
	public List<Payment> payments;

	public Receipt(String title, User creator, String description)
	{
		this.title = title;
		this.creator = creator;
		this.description = description;
		this.created = new Date();
		this.comments = new ArrayList<Comment>();
		this.members = new TreeSet<User>();
		this.subpots = new ArrayList<Subpot>();
		this.owners = new ArrayList<ReceiptOwner>();
	}

	/**
	 * @return Total amount of money on this receipt
	 */
	public double getTotal()
	{
		double ret = 0;
		for(ReceiptOwner r : owners) ret += r.amount;
		return ret;
	}
	
	/**
	 * @param user
	 * @return The amount of money user this user spent on this receipt
	 */
	public double getTotal(User user)
	{
		if(!members.contains(user)) return 0;
		
		// Amount to pay from subpots
		double amount = 0;
		double subpotTotal = 0;
		for (Subpot pot : subpots)
		{
			amount += pot.getTotal(user);
			subpotTotal += pot.total;
		}
		
		// Amount to pay outside of subrounds and tip
		double total = getTotal();
		amount += (total - subpotTotal - tip) / members.size();
		
		// Calculate amount of tip user should pay
		// if user has X% of non-tip debt, he should pay X% of the tip
		double totalWithoutTip = total - tip;
		if(totalWithoutTip < 1e-8) amount += tip / members.size(); // Special case of just tip
		else 
		{
			double percentage = amount / totalWithoutTip;
			amount += tip * percentage;
		}
		
		return amount;
	}
	
	public double shouldPay(User payer)
	{
		double pSpent = getTotal(payer);
		double pPaid = 0;
		for(ReceiptOwner o : owners)
		{
			if(o.user.equals(payer)) pPaid = o.amount;
		}
		
		return pSpent - pPaid;
	}
	
	/**
	 * Payment algorithm:
	 * Some members will have paid more than they spent (payers)
	 * and others less than they spent (spenders)
	 * 
	 * Every spender should pay back the difference
	 * payments from every spender will be split proportionately between payers
	 * 
	 * Eg. Peksa paid 200 kr more than he spent, gunde 100 kr more
	 * other users paid less than they spent
	 * from the difference they should pay 2/3 to peksa and 1/3 to gunde
	 * Once the entire difference of 300 kr is payed by spenders, all debts are settled.
	 */
	public double shouldPay(User payer, User receiver)
	{
		if(!members.contains(payer) || !members.contains(receiver)) return 0;
		
		// Add spending
		double pPayment = getTotal(payer);
		double rPayment = getTotal(receiver);
		double totalOverPaid = 0;
		
		for(ReceiptOwner o : owners)
		{
			// Subtract payment
			if(o.user.equals(payer)) pPayment -= o.amount;
			else if(o.user.equals(receiver)) rPayment -= o.amount;
			
			// Also keep track of total payment transfer
			double ownerSpent = getTotal(o.user);
			if(o.amount > ownerSpent) totalOverPaid += o.amount - ownerSpent;
		}
		
		// Account for rounding errors, if close to zero count as zero
		if(Math.abs(pPayment) <= 1e-8 || Math.abs(rPayment) <= 1e-8 ) return 0;
		
		boolean pShouldPay = pPayment > 0;
		boolean rShouldPay = rPayment > 0;
		// No payment between two payers, and no payment between two spenders
		if(!(pShouldPay ^ rShouldPay)) return 0;
		
		// Only one of theese should be true
		if(pShouldPay)
		{
			return pPayment * (-rPayment / totalOverPaid);
		}
		if(rShouldPay)
		{
			// Return negative, to indicate debt
			return -(rPayment * (-pPayment / totalOverPaid));
		}
		
		return 0;
	}

	public String toString()
	{
		return "Receipt by " + creator + " for " + getTotal() + " SEK";
	}
}
