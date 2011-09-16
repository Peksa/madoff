package controllers;
 
import play.*;
import play.mvc.*;
import java.util.*;
import models.*;
 
@With(Secure.class)
public class Pictures extends CRUD
{
	public static void get(Long id)
	{
		Picture picture = Picture.findById(id);
		response.setContentTypeIfNotSet(picture.image.type());
		renderBinary(picture.image.get());
	}
}