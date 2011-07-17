package models;


import java.util.Date;

import javax.persistence.*;

import play.db.jpa.Model;

@Entity
public class Comment extends Model
{
	// Owning side
	@ManyToOne
	public User poster;

	public Date date;

	@Lob
	public String content;

	// Owning side
	@ManyToOne
	public Receipt receipt;

	public Comment(Receipt receipt, User poster, String content)
	{
		this.receipt = receipt;
		this.poster = poster;
		this.content = content;
		this.date = new Date();
	}

}
