package controllers;
 
import play.*;
import play.mvc.*;
import play.i18n.*;

public class Languages extends Controller
{	
	public static void set(String language, String url)
	{
		if (language.equals("english"))
		{
			Lang.change("en");
		}
		else if (language.equals("swedish"))
		{
			Lang.change("se");
		}
		else
		{
			Lang.change("en");
		}
		redirect(url);
	}
}