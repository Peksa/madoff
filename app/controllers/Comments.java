package controllers;

import ext.json.serializers.CommentJsonSerializer;
import models.Comment;
import models.Receipt;
import models.User;
import play.*;
import play.mvc.*;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for controller access
public class Comments extends CRUD
{
	
	/**
	 * Add a comment to a recept using ajax
	 * @param id receipt to add the comment to
	 * @param content content of comment
	 */
	public static void post(Long id, String content)
	{
		Receipt receipt = Receipt.findById(id);
		Comment comment = new Comment(receipt, Security.connectedUser(), content).save();
		renderJSON(comment, new CommentJsonSerializer());
	}
	
	
	/**
	 * Delete a comment
	 */
	public static void delete(Long id)
	{
		Comment comment = Comment.findById(id);
		Receipt receipt = comment.receipt;
		
		// Check that the user is either owner of receipt or owner of the post.
		if (Security.isAuthorized(comment.poster, receipt.owner))
		{
			comment.delete();
		}
		else
		{
			error("WTF. Bad dog!");
		}
	}
}
