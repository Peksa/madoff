package controllers;

import models.Comment;
import models.Receipt;
import play.*;
import play.mvc.*;
import helpers.serializers.CommentJsonSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for contoller access
public class Comments extends CRUD
{
	
	/**
	 * Add a comment to a recept using ajax
	 * @param id receipt to add the comment to
	 * @param content content of comment
	 */
	public static void post(Long id, String content, Long lastPost)
	{
		Receipt receipt = Receipt.findById(id);
		new Comment(receipt, Security.connectedUser(), content).save();
		getCommentsCommon(id, lastPost);
	}
	
	public static void get(Long id, Long lastPost) {
		getCommentsCommon(id, lastPost);
	}
	
	static void getCommentsCommon(Long id, Long lastPost) {
		if(lastPost < 0) lastPost = 0L; // Avoid exception
		Date date = new Date(lastPost);

		// TODO add enterprise generalized code with generics and interfaces
		List<Comment> comments = Comment.find("receipt.id = ? AND date > ? ORDER BY date asc", id, date).fetch();
		renderJSON(comments, new CommentJsonSerializer());
	}
}
