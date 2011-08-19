package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class User extends Model implements Comparable<User>
{
	public String email;
	public String username;
	public String password;
	public String fullname;
	public String accountNumber;

	// Inverse side
	@OneToMany(mappedBy = "owner", cascade = CascadeType.PERSIST)
	public List<Receipt> receipts;

	public User(String email, String username, String password, String fullname)
	{
		this.email = email;
		this.username = username;
		this.password = password;
		this.fullname = fullname;
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
