package models;

import java.text.SimpleDateFormat;
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
	@ManyToOne(cascade = CascadeType.PERSIST)
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
	
	/**
	 * Return a formatted date.
	 * TODO(peksa): This is pretty common, perhaps static method in util-class somewhere?
	 * @param format of the date
	 * @return formatted date
	 */
	public String getFormattedDate(String format)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(this.date);
	}
}
