package models;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class User extends Model implements Comparable<User>
{
	@Required
	public String email;
	@Required
	public String username;
	@Required
	public String password;
	public String fullname;
	public String accountNumber;
	
	public String bankName;
	public String accountNo;
	public String clearingNo;

	public String fish;

	public boolean admin = false;
	
	@OneToOne
	public Picture picture;

	// Inverse side
	@OneToMany(mappedBy = "creator", cascade = CascadeType.PERSIST)
	public List<Receipt> receipts;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public Set<ReceiptOwner> ownedReceipts;
	
	@ManyToMany(mappedBy = "members", cascade = CascadeType.PERSIST)
	public Set<Receipt> incomingReceipts;
	
	@OneToMany(mappedBy = "payer", cascade = CascadeType.PERSIST)
	public List<Payment> payments;
	
	@OneToMany(mappedBy = "receiver", cascade = CascadeType.PERSIST)
	public List<Payment> incomingPayments;

	public User(String email, String username, String password)
	{
		this.email = email;
		this.username = username;
		this.password = password;
		this.receipts = new ArrayList<Receipt>();
	}

	@Override
	public int compareTo(User other)
	{
		return username.compareTo(other.username);
	}

	public String toString()
	{
		return username;
	}
}
