package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@With(Secure.class) // Require login for contoller access
public class Application extends Controller
{
	/**
	 * Show index-page
	 */
	public static void index()
	{
		List<Receipt> receipts = Receipt.find("order by created asc").fetch();
		render(receipts);
	}
}