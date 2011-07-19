package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class IndebtAmount extends Model
{
	@ManyToOne
    public User user;
    public int amount;
    
    @ManyToOne
    public Subpot subpot;
    
    public IndebtAmount(Subpot subpot, User user, int amount)
    {
    	this.subpot = subpot;
    	this.user = user;
    	this.amount = amount;
    }
}
