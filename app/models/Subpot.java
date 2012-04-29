package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

/**
 * Represents a subpot to a receipt, an amount of money to be equally divided among the members
 * @author David
 *
 */
@Entity
public class Subpot extends Model
{
	@ManyToOne
	public Receipt receipt;
	public String description;
	
	public double total;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<User> members;
	
	public Subpot(double total)
	{
		this.total = total;
		this.members = new TreeSet<User>();
	}

	/**
	 * Get total amount of all rounds
	 * 
	 * @return total amount of money owed to receipt owner
	 */
	public double getTotal()
	{
		return total;
	}
	
	public double getTotal(User user)
	{
		if(!members.contains(user)) return 0;
		return total / members.size();
	}
}
