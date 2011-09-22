package controllers;

import play.Logger;
import play.libs.F.EventStream;
import play.mvc.*;

@With(Secure.class)
public class Notifications extends CRUD
{
	public static EventStream liveStream = new EventStream();
	
    public static void stream()
    {
    	String notification = await(liveStream.nextEvent());
    	renderText(notification);
    }
    
    /**
     * Temporary for adding broadcast-notifications.
     * @param text notification text to appear in red box of all clients.
     */
    public static void add(String text)
    {
    	liveStream.publish(text);
    }
}
