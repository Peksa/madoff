package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Payment extends Model
{
	@ManyToOne
	User payer;
	
	@ManyToOne
	User receiver;
	
	Date paid;
	Date accepted;
	
	int amount;
	
	public Payment(User payer, User receiver, int amount)
	{
		this.payer = payer;
		this.receiver = receiver;
		this.amount = amount;
		this.paid = new Date();
	}
	
	public String toString()
	{
		return payer + " -> " + receiver + ", amount: " + amount;
	}
}
