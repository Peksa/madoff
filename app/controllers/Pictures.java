package controllers;
 
import play.*;
import play.mvc.*;

import java.io.File;
import java.util.*;
import models.*;
 
@With(Secure.class)
public class Pictures extends Controller
{
	public static void get(Long id)
	{

		User u = User.findById(id);
		if (u.picture == null)
		{
			response.setContentTypeIfNotSet("image/png");
			File root = Play.applicationPath;
			
			renderBinary(new File(root.getAbsolutePath() + "/public/images/default.png"));
		}
		else
		{
			response.setContentTypeIfNotSet(u.picture.image.type());
			renderBinary(u.picture.image.get());
		}
	}
}