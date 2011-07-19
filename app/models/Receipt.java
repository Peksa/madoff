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
	
	public int tip;
	
	// Owning side
	@ManyToOne
	public User owner;
	
	// Owning side
	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<User> members;
	
	// Inverse side
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<PaidAmount> paid;

	@Lob
	public String description;

	// Inverse side
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Comment> comments;

	
	// Inverse side
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Subpot> subpots;

	public Receipt(String title, int totalAmount, User owner, String description)
	{
		this.title = title;
		this.owner = owner;
		this.description = description;
		this.created = new Date();
		this.comments = new ArrayList<Comment>();
		this.members = new TreeSet<User>();
		this.paid = new ArrayList<PaidAmount>();
		this.subpots = new ArrayList<Subpot>();
	}
	
	public int getTotal()
	{
		int amount = 0;
		for (Subpot pot : subpots)
		{
			amount += pot.getTotal();
			amount += (members.size() - pot.cases.size()) * pot.restAmount;
		}
		return amount;
	}
	
	public String toString()
	{
		return "Receipt by " + owner + " for " + getTotal() + " SEK";
	}

}
