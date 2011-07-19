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
     * @return
     */
    public int getTotal()
    {
    	int amount = 0;
    	for (IndebtAmount round : cases)
    		amount += round.amount;
    	return amount;
    }
    
    // TODO(p950nim): Method for adding rounds here?
}
