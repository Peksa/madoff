package helpers.serializers;

import com.ocpsoft.pretty.time.PrettyTime;

import java.lang.reflect.Type;
import java.text.DateFormat;

import models.Comment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import play.templates.JavaExtensions;

	public class CommentJsonSerializer implements JsonSerializer<Comment>
{

	@Override
	public JsonElement serialize(Comment comment, Type t, JsonSerializationContext jsc)
	{
		// Get pretty time
		PrettyTime p = new PrettyTime();
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", comment.id);
		obj.addProperty("own", (comment.poster == comment.receipt.owner));
		obj.addProperty("poster", comment.poster.fullname);
		obj.addProperty("content", comment.content);
		obj.addProperty("date", p.format(comment.date));
		obj.addProperty("timestamp", comment.date.getTime());
		return obj;
	}

}
