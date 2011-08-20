package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class Subpot extends Model
{

	@ManyToOne
	public Receipt receipt;

	public int restAmount;

	public String description;

	@OneToMany(mappedBy = "subpot", cascade = CascadeType.ALL)
	public List<IndebtAmount> cases;

	public Subpot(int restAmount)
	{
		this.restAmount = restAmount;
		this.cases = new ArrayList<IndebtAmount>();
	}

	/**
	 * Get total amount of all rounds
	 * 
	 * @return total amount of money owed to receipt owner
	 */
	public int getTotal()
	{
		int amount = 0;
		for (IndebtAmount round : cases)
			amount += round.amount;
		return amount;
	}
	
	public int getTotal(User user)
	{
		for (IndebtAmount round : cases)
			if(round.user.equals(user))
				return round.amount;
		return restAmount;
	}

	// TODO(peksa): Method for adding rounds here?
}
