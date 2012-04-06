package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Comment;
import models.Receipt;
import models.ReceiptOwner;
import models.Subpot;
import models.User;
import play.*;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.*;

/**
 * For CRUD-interface
 * 
 * @author Peksa
 */
@With(Secure.class) // Require login for controller access
public class Receipts extends CRUD
{
	/**
	 * Show a receipt
	 * 
	 * @param id of receipt
	 */
	public static void show(Long id)
	{
		if (validation.hasErrors())
			error(Messages.get("controllers.Receipts.show.error"));
		Receipt receipt = Receipt.findById(id);
		User connectedUser = Security.connectedUser();

		System.out.println("Subpot testing!");
		for(Subpot p : receipt.subpots)
		{
			System.out.println(p.total);
			for(User u : p.members) System.out.println(u.username);
		}

		render(receipt, connectedUser);
	}


	public static void delete(Long id)
	{
		if (validation.hasErrors())
			error(Messages.get("controllers.Receipts.show.error"));
		Receipt receipt = Receipt.findById(id);


		// Check that the user is owner of receipt.
		if (Security.isAuthorized(receipt.creator))
		{
			receipt.delete();
		}
		else
		{
			error(Messages.get("error"));
		}
	}

	// Sort of ugly with public, but play breaks otherwise
	public class SubroundInput
	{
		public ArrayList<Long> members;
		public String description;
		public double amount;
		public boolean everyoneExcept;
		public boolean together;

		public void testPrint()
		{
			System.out.println("Subround:");
			for(Long s : members) System.out.println(s.toString());
					System.out.println(description);
					System.out.println(amount);
		}
		
		public String validate(List<Long> members)
		{
			// Empty subrounds not counted
			if(this.members == null) return null;
			
			for (Long id : this.members)
			{
				if(!members.contains(id))
				{
					return Messages.get("controllers.Receipts.add.subroundMemberNotMember");
				}
				if(description == null || description.length() == 0) return "Subround lacks description";
				if(amount <= 1e-8) return "Subround amount requred (and must be positive)";
			}
			return null;
		}
		
	}

	public static void add(String title, int tip, List<Long> members, String description, double total, List<SubroundInput> subrounds, String payed)
	{
		// validate
		// TODO validate on client first, to give better and faster feedback
		String errorStr = null;
		if(title == null || title.length() == 0) errorStr = "Title requred";
		else if(members.size() == 0) errorStr = "Members requred";
		else if(total <= 1e-8) errorStr = "Total requred (and must be positive)";
		else
		{
			double subTotal = 0;
			for (SubroundInput subround : subrounds)
			{
				subTotal += subround.amount;
				errorStr = subround.validate(members);
				if(errorStr != null) break; // break on first error
			}
			if(subTotal > total) errorStr = "Subround amount grater than total";
		}
		if(errorStr != null)
		{
			// TODO return to old page with flash cache
			error(errorStr);
			return;
		}
		
		Set<User> membersSet = new HashSet<User>();

		for (Long id : members) 
		{
			User u = User.findById(id);
			membersSet.add(u);
		}

		Receipt receipt = new Receipt(title, Security.connectedUser(), description, total);
		receipt.tip = tip;
		receipt.members.addAll(membersSet);
		receipt.finished = true;
		
		if(payed.equals("split"))
		{
			double eachShare = total / receipt.members.size();
			for(User u : receipt.members)
			{
				receipt.owners.add(new ReceiptOwner(receipt, u, eachShare));
			}
		}
		else // Creator payed for everything
		{
			receipt.owners.add(new ReceiptOwner(receipt, receipt.creator, total));
		}
		
		receipt.save();

		if(subrounds != null)
		{
			for(SubroundInput input : subrounds)
			{
				if(input.members != null)
				{
					input.testPrint();
					Subpot subpot = new Subpot(input.amount);
					subpot.description = input.description;
					for (Long id : input.members)
					{
						User u = User.findById(id);
						subpot.members.add(u);
					}
					subpot.receipt = receipt;
					subpot.save();
				}
			}
		}

		Receipts.show(receipt.id);
	}

	public static void details(Long id) 
	{
		Receipt receipt = Receipt.findById(id);
		render(receipt);
	}


	public static void register()
	{
		List<User> members = User.find("order by fullname asc").fetch();
		User currentUser = Security.connectedUser();
		render(members, currentUser);
	}


}
