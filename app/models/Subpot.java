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
	
	public int total;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<User> members;
	
	public Subpot(int total)
	{
		this.total = total;
		this.members = new TreeSet<User>();
	}

	/**
	 * Get total amount of all rounds
	 * 
	 * @return total amount of money owed to receipt owner
	 */
	public int getTotal()
	{
		return total;
	}
	
	public int getTotal(User user)
	{
		//TODO fix rounding errors etc
		System.out.println(user.toString() + " in " + description.toString() + " " + members.contains(user));
		if(!members.contains(user)) return 0;
		return total / members.size();
	}
}
