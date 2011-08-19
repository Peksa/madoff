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

	// Inverse side
	@OneToMany(mappedBy = "owner", cascade = CascadeType.PERSIST)
	public List<Receipt> receipts;

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
