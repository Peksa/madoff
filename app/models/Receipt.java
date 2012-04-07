package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Receipt extends Model
{
	public String title;
	public Date created;
	public Date cleared;
	
	public boolean finished;

	//public double total; // deprecated, use sum of owner amount
	public double tip;

	@ManyToOne
	public User creator;
	
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

	@ManyToMany(cascade=CascadeType.ALL)
	public List<Payment> payments;

	public Receipt(String title, User creator, String description)
	{
		this.finished = false;
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
	 * @return The amount of money user should pay
	 */
	public double getTotal(User user)
	{
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
		if(totalWithoutTip == 0) amount += tip / members.size(); // Special case of just tip
		else 
		{
			double percentage = amount / totalWithoutTip;
			amount += tip * percentage + 0.5;
		}
		
		return amount;
	}
	
	/**
	 * Returns whether user has paid money for this receipt
	 * @param user
	 * @return True iff user has a payment associated with this receipt
	 */
	public boolean hasPayment(User user)
	{
		//TODO(dschlyter) later - verify if payments are for correct amount (turns into graph problem)
		if (!members.contains(user)) return true;
		
		for (Payment p : payments) {
			if (p.payer.equals(user)) return true;
		}
		
		return false;
	}

	public String toString()
	{
		return "Receipt by " + creator + " for " + getTotal() + " SEK";
	}
}
