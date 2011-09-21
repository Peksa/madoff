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

	public double total;
	public double tip;

	// Owning side
	@ManyToOne
	public User owner;

	// Owning side
	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<User> members;

	@Lob
	public String description;

	// Inverse side
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Comment> comments;

	// Inverse side
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Subpot> subpots;

	@ManyToMany(cascade=CascadeType.ALL)
	public List<Payment> payments;

	public Receipt(String title, User owner, String description, double total)
	{
		this.finished = false;
		this.title = title;
		this.owner = owner;
		this.description = description;
		this.total = total;
		this.created = new Date();
		this.comments = new ArrayList<Comment>();
		this.members = new TreeSet<User>();
		this.subpots = new ArrayList<Subpot>();
	}

	/**
	 * @return Total amount of money on this receipt
	 */
	public double getTotal()
	{
		return total;
	}
	
	/**
	 * @param user
	 * @return The amount of money user should pay
	 */
	public double getTotal(User user)
	{
		double amount = 0;
		double subpotTotal = 0;
		for (Subpot pot : subpots)
		{
			amount += pot.getTotal(user);
			subpotTotal += pot.total;
		}
		
		//TODO fix rounding errors etc
		amount += (total - subpotTotal) / members.size();
		
		// Calculate amount of tip user should pay
		// if user has X% of non-tip debt, he should pay X% of the tip
		if(total == 0) amount += tip / members.size(); // Special case of just tip
		else 
		{
			//TODO fix rounding errors etc
			double percentage = amount / total;
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
		return "Receipt by " + owner + " for " + getTotal() + " SEK";
	}
}
