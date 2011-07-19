package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class PaidAmount extends Model
{
    @ManyToOne
    public Receipt receipt;
    
	@ManyToOne
    public User user;
	
    public int amount;

    
    public PaidAmount(Receipt receipt, User user, int amount)
    {
    	this.receipt = receipt;
    	this.user = user;
    	this.amount = amount;
    }
}
