package controllers;

import models.Comment;
import models.Receipt;
import models.User;
import play.*;
import play.mvc.*;
import play.i18n.Messages;
import helpers.serializers.CommentJsonSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for controller access
public class Comments extends Controller
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

		List<Comment> comments = Comment.find("receipt.id = ? AND date > ? ORDER BY date asc", id, date).fetch();
		renderJSON(comments, new CommentJsonSerializer());
	}
	
	
	/**
	 * Delete a comment
	 */
	public static void delete(Long id)
	{
		Comment comment = Comment.findById(id);
		Receipt receipt = comment.receipt;
		
		// Check that the user is either owner of receipt or owner of the post.
		if (Security.isAuthorized(comment.poster, receipt.creator))
		{
			comment.delete();
		}
		else
		{
			error(Messages.get("error"));
		}
	}
}
