package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Receipt extends Model
{
	public String title;
	public Date created;
	public Date cleared;
	
	public int tip;
	
	// Inverse side
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<PaidAmount> paid;

	// Owning side
	@ManyToOne
	public User owner;

	@Lob
	public String description;

	// Inverse side
	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	public List<Comment> comments;

	// Owning side
	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<User> members;

	public Receipt(String title, int totalAmount, User owner, String description)
	{
		this.title = title;
		this.owner = owner;
		this.description = description;
		this.created = new Date();
		this.comments = new ArrayList<Comment>();
		this.members = new TreeSet<User>();
		this.paid = new ArrayList<PaidAmount>();
	}

}
