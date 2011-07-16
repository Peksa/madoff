package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Comment extends Model
{

	@ManyToOne
	public User poster;

	public Date date;

	@Lob
	public String content;

	@ManyToOne
	public Receipt receipt;

	public Comment(Receipt receipt, User poster, String content) {
		this.receipt = receipt;
		this.poster = poster;
		this.content = content;
		this.date = new Date();
	}

}
