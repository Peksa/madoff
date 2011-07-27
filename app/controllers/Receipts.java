package controllers;

import play.*;
import play.mvc.*;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for contoller access
public class Receipts extends CRUD
{
}
