package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Receipt extends Model
{

	public String title;
	public Date date;
	public int totalAmount;

	// TODO(peksa): need sub-pots, and ways to separate amounts to users

	@ManyToOne
	public User owner;

	@Lob
	public String description;

	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Comment> comments;

	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<User> members;

	public Receipt(String title, int totalAmount, User owner, String description)
	{
		this.title = title;
		this.totalAmount = totalAmount;
		this.owner = owner;
		this.description = description;
		this.date = new Date();
		this.comments = new ArrayList<Comment>();
		this.members = new TreeSet<User>();
	}

}
