package models;

import java.util.Date;

import javax.persistence.*;

import play.db.jpa.Model;

@Entity
public class Comment extends Model
{
	// Owning side
	@ManyToOne
	public Receipt receipt;

	// Owning side
	@ManyToOne
	public User poster;

	public Date date;

	@Lob
	public String content;

	public Comment(Receipt receipt, User poster, String content)
	{
		this.receipt = receipt;
		this.poster = poster;
		this.content = content;
		this.date = new Date();
	}

	public String toString()
	{
		return poster + ": " + content;
	}
}
