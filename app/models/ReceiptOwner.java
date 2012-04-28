package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class ReceiptOwner extends Model
{
	public ReceiptOwner(Receipt receipt, User owner, double amount) {
		super();
		this.receipt = receipt;
		this.user = owner;
		this.amount = amount;
	}

	@ManyToOne
	public Receipt receipt;

	@ManyToOne
	public User user;
	
	public double amount;
}
