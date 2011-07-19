package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class PaidAmount extends Model
{
	@ManyToOne
    public User user;
    public int amount;
    @ManyToOne
    public Receipt receipt;
    
    public PaidAmount(Receipt receipt, User user, int amount)
    {
    	this.receipt = receipt;
    	this.user = user;
    	this.amount = amount;
    }
}
