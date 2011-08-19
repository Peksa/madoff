package helpers.serializers;


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
		JsonObject obj = new JsonObject();
		obj.addProperty("id", comment.id);
		obj.addProperty("own", (comment.poster == comment.receipt.owner));
		obj.addProperty("poster", comment.poster.fullname);
		obj.addProperty("content", comment.content);
		obj.addProperty("date", comment.getFormattedDate("yyyy-MM-dd"));
		obj.addProperty("timestamp", comment.date.getTime());
		return obj;
	}

}
