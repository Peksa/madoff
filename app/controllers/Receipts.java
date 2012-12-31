package controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Comment;
import models.Payment;
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
public class Receipts extends Controller
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
		User user = Security.connectedUser();

		if (!receipt.members.contains(user))
			unauthorized: {
			// (hack) A user could also have removed himself, and left a comment
			// check comment authors for access
			for(Comment c : receipt.comments) {
				if(c.poster.equals(user)) break unauthorized;
			}

			error(Messages.get("controllers.Payments.validate.unauthorized"));
		}

		render(receipt, user);
	}

	/**
	 * Removes the current user from the receipt
	 * @param id
	 */
	public static void removeMe(Long id)
	{
		if (validation.hasErrors())
			error(Messages.get("controllers.Receipts.show.error"));

		Receipt receipt = Receipt.findById(id);
		User user = Security.connectedUser();
		if (!receipt.members.contains(Security.connectedUser())) {
			error(Messages.get("You are not a member of this receipt"));
		} else if (receipt.hasPaymentsDone()){
			// Might be good to pay after editing, but will screw up old payments
			error(Messages.get("Payments has been done on this recipt, cannot edit"));
		} else {
			removeUser(receipt, user);
			Application.index(false);
		}

	}

	private static void removeUser(Receipt receipt, User user) {
		// Remove receipt from all active payments
		for(Payment payment : receipt.payments) {
			if(!payment.deprecated && 
					(payment.payer.equals(user) || payment.receiver.equals(user))) {
				Payment fixedPayment = new Payment(payment, receipt, false);
				fixedPayment.save();
			}
		}

		// Remove user from all subrounds
		for(Subpot pot : receipt.subpots) {
			pot.members.remove(user);
		}
		for(ReceiptOwner owner : receipt.owners) {
			if(owner.user.equals(user)) {
				owner.receipt = null;
				owner.save();
			}
		}
		receipt.members.remove(user);
		new Comment(receipt, user, user.username +" removed h(im|er)self from the receipt").save();
		receipt.save();
	}

	public static void delete(Long id)
	{
		if (validation.hasErrors())
			error(Messages.get("controllers.Receipts.show.error"));
		Receipt receipt = Receipt.findById(id);

		if (!Security.isAuthorized(receipt.creator)) {
			error(Messages.get("Must be owner"));
		} else if (receipt.hasPaymentsDone()){
			error(Messages.get("Payments has been done on this recipt, cannot delete"));
		} else {
			Set<User> remove = new HashSet<User>(receipt.members);
			for(User user : remove) removeUser(receipt, user);
			receipt.flagAsDeleted();
			receipt.save();
			Application.index(false);
		}
	}

	public static void edit(Long id)
	{
		if (validation.hasErrors())
			error(Messages.get("controllers.Receipts.show.error"));

		Receipt receipt = Receipt.findById(id);
		if (!Security.isAuthorized(receipt.creator)) {
			error(Messages.get("Must be owner"));
		} else if (receipt.hasPaymentsDone()){
			error(Messages.get("Payments has been done on this recipt, cannot edit"));
		} else {
			String paid;
			if(receipt.owners.size() == 1 && receipt.owners.get(0).id == receipt.creator.id) paid = "creator";
			else {
				boolean equalSplit = true;
				if(receipt.owners.size() != receipt.members.size()) equalSplit = false;
				else {
					for(ReceiptOwner owner : receipt.owners) {
						if(owner.amount != receipt.owners.get(0).amount) equalSplit = false;
					}
				}
				paid = equalSplit ? "split" : "custom";
			}
			
			ArrayList<Long> members = new ArrayList<Long>();
			for(User user : receipt.members) members.add(user.id);
			
			ArrayList<SubroundInput> subrounds = new ArrayList<SubroundInput>();
			for(Subpot pot : receipt.subpots) {
				SubroundInput input = new SubroundInput();
				subrounds.add(input);
				
				input.members = new ArrayList<Long>();
				for(User user : pot.members) input.members.add(user.id);
				if(pot.calculation != null) input.amount = pot.calculation;
				else input.amount = ""+ pot.total;
				input.description = pot.description;
			}	
			
			Map<Long,Double> payment = new HashMap<Long, Double>();
			if(paid == "custom") {
				for(ReceiptOwner owner : receipt.owners) {
					payment.put(owner.user.id, owner.amount);
				}
			}
			
			ReceiptData data = new ReceiptData(receipt.id, 
					receipt.title, 
					receipt.tip, 
					members, 
					receipt.description, 
					receipt.getTotal(), 
					paid,
					subrounds,
					payment);
			
			registerCommon(null, data);
		}
	}
	
	// All data for a receipt, to send back to edit or retry on register error
	public static class ReceiptData {
		public ReceiptData(Long id, String title, Double tip,
				List<Long> members, String description, Double total,
				String paid, List<SubroundInput> subrounds, Map<Long,Double> payment) {
			super();
			this.id = id;
			this.title = title;
			this.tip = tip;
			this.members = members;
			this.description = description;
			this.total = total;
			this.paid = paid;
			this.subrounds = subrounds;
			this.payment = payment;
		}
		
		public Long id;
		public String title;
		public Double tip;
		public List<Long> members = new ArrayList<Long>();
		public String description;
		public Double total;
		public String paid;
		public List<SubroundInput> subrounds;
		public Map<Long,Double> payment;
	}
	
	public static class PaymentSplitInput
	{
		public long id;
		public double amount;
		
		public PaymentSplitInput(long id, double amount) {
			super();
			this.id = id;
			this.amount = amount;
		}
	}

	// Sort of ugly with public, but play breaks otherwise
	public static class SubroundInput
	{
		public ArrayList<Long> members = new ArrayList<Long>();
		public String description;
		public String amount;
		public String calculation;
		public double parsedAmount;
		// public boolean everyoneExcept;
		// public boolean together;

		public void testPrint()
		{
			System.out.println("Subround:");
			for(Long s : members) System.out.println(s.toString());
			System.out.println(description);
			System.out.println(amount);
		}

		public String parseAmount(double totalAmount, int subroundIndex) {
			if(this.amount == null || this.amount.length() == 0) return null; // Empty subrounds
			
			String work = amount;
			work = work.replaceAll("[+]", " + ");
			work = work.replaceAll("[-]", " - ");
			work = work.replaceAll("[*]", " * ");
			work = work.replaceAll("[/]", " / ");
			work = work.replaceAll("total", " "+ String.valueOf(totalAmount) +" ");
			work = work.replaceAll("\\s+", " "); // Remove multiple spaces space before split
			work = work.trim();
			List<String> parts = new ArrayList<String>(Arrays.asList(work.split(" ")));
			
			try {
				String[] precedence = new String[] { "[*/]", "[+-]" };
				
				for(String operations : precedence) {
					for(int i=1; i<parts.size()-1; i++) {
						if(!parts.get(i).matches(operations)) continue;
						
						double prev = Double.parseDouble(parts.get(i-1));
						double next = Double.parseDouble(parts.get(i+1));
						
						if(parts.get(i).equals("*")) parts.set(i-1, ""+ (prev * next));
						else if(parts.get(i).equals("/")) parts.set(i-1, ""+ (prev / next));
						else if(parts.get(i).equals("+")) parts.set(i-1, ""+ (prev + next));
						else if(parts.get(i).equals("-")) parts.set(i-1, ""+ (prev - next));
						
						parts.remove(i+1);
						parts.remove(i);
						i--;
					}
				}
				
				if(parts.size() != 1) throw new NumberFormatException();
				
				this.parsedAmount = Double.parseDouble(parts.get(0));
				// If these are not equal, a calculation has been made, save it!
				if(!this.amount.equals(""+ this.parsedAmount)) {
					this.calculation = this.amount;
				}
				
			} catch(NumberFormatException e) {
				e.printStackTrace();
				return "Parse failure on amount for subround " + subroundIndex;
			}
			
			return null;
		}

		public String validate(List<Long> receiptGlobalMembers)
		{
			if(this.members == null || this.members.size() == 0) return "Subround has no members (empty all fields to discard a subround)";
			if(description == null || description.length() == 0) return "Subround lacks description (empty all fields to discard a subround)";
			if(parsedAmount <= 1e-8) return "Positive subround amount requred (empty all fields to discard a subround)";
			for (Long id : this.members)
			{
				if(!receiptGlobalMembers.contains(id))
				{
					return Messages.get("controllers.Receipts.add.subroundMemberNotMember");
				}
			}
			
			return null;
		}
	}

	public static void add(Long receiptId, String title, Double tip, List<Long> members, String description, Double total, List<SubroundInput> subrounds, String paid, Map<Long,Double> payment)
	{
		// Scrub away any empty subrounds
		if(subrounds == null) subrounds = new ArrayList<SubroundInput>();
		Iterator<SubroundInput> it = subrounds.iterator();
		while(it.hasNext()) {
			SubroundInput data = it.next();
			boolean noMembers = data.members == null || data.members.size() == 0;
			boolean noItem = data.description == null || data.description.length() == 0;
			boolean noAmount = data.amount == null || data.amount.length() == 0;
			if(noMembers && noItem && noAmount) it.remove();
		}
		
		// Save the data in a handy object to be able to pass it back to render() on failure
		ReceiptData data = new ReceiptData(receiptId, title, tip, members, description, total, paid, subrounds, payment);
		
		// Init with some default values
		if(members == null) members = new ArrayList<Long>();
		if(payment == null) payment = new HashMap<Long, Double>();
		if(tip == null) tip = 0.0;
		if(total == null) total = 0.0;
		
		User creator = Security.connectedUser();
		boolean creatorIsMember = false;
		for(Long id : members) {
			if(id.equals(creator.id)) creatorIsMember = true;
		}
		
		// Hack to make adding of debts where you spent nothing simpler
		// this must be done in "i paid everything"-mode right now
		if(paid.equals("creator") && !creatorIsMember)
		{
			double subTotal = 0;
			for (SubroundInput subround : subrounds) subTotal += subround.parsedAmount;

			// Add everything to a subround with just the old members
			if(subTotal < total)
			{
				SubroundInput restRound = new SubroundInput();
				restRound.members = new ArrayList<Long>();
				restRound.members.addAll(members);
				restRound.parsedAmount = total - subTotal;
				restRound.description = "Other";
				subrounds.add(restRound);
			}

			members.add(creator.id);
			creatorIsMember = true;
		}

		// Validate
		String errorStr = null;
		if(title == null || title.length() == 0) errorStr = "Title requred";
		else if(members.size() == 0) errorStr = "Members requred";
		else if(total == null || total <= 1e-8) errorStr = "Total requred (and must be positive)";
		else if(!creatorIsMember) errorStr = "Creator must also be a member";
		else
		{
			double subTotal = 0;
			for(int i=0; i<subrounds.size(); i++){
				SubroundInput subround = subrounds.get(i);
				errorStr = subround.parseAmount(total, i);
				if(errorStr != null) break; // break on first error
				subTotal += subround.parsedAmount;
				errorStr = subround.validate(members);
				if(errorStr != null) break; // break on first error
			}
			if(subTotal > total) errorStr = "Subround amount grater than total";
			
			if(paid.equals("custom")) {
				double paysplitTotal = 0;
				for(Entry<Long, Double> split : payment.entrySet()){
					if(!members.contains(split.getKey())) errorStr = "Member in payment split not in receipt members";
					paysplitTotal += split.getValue();
				}
				if(Math.abs(paysplitTotal - total) > (0.01 + 1e-8) * members.size()) errorStr = "Payment split sum does not equal total";
			}
		}
		if(errorStr != null) {
			registerCommon(errorStr, data);
			return;
		}

		Set<User> membersSet = new HashSet<User>();

		for (Long id : members) 
		{
			User u = User.findById(id);
			membersSet.add(u);
		}

		Receipt receipt = null;
		if(receiptId == null) {
			receipt = new Receipt(title, Security.connectedUser(), description);
		} else {
			receipt = Receipt.findById(receiptId);
			if(receipt.creator.id != creator.id) Security.reportToSanta();
			receipt.title = title;
			receipt.description = description;
			
			// Clear relation to add them later (maybe not the best way to do it, but quickest!)
			receipt.members.clear();
			for(Subpot pot : receipt.subpots) pot.delete();
			receipt.subpots.clear();
			for(ReceiptOwner owner : receipt.owners) owner.delete();
			receipt.owners.clear();
		}
		
		receipt.tip = tip;
		receipt.members.addAll(membersSet);

		if(paid.equals("split"))
		{
			double eachShare = total / receipt.members.size();
			for(User u : receipt.members)
			{
				receipt.owners.add(new ReceiptOwner(receipt, u, eachShare));
			}
		}
		else if(paid.equals("custom")) {
			for(Entry<Long, Double> split : payment.entrySet()) {
				if(split.getValue() > 1e-8) {
					for(User u : receipt.members) {
						if(u.id == split.getKey()) {
							receipt.owners.add(new ReceiptOwner(receipt, u, split.getValue()));
						}
					}
				}
			}
		}
		else // Creator payed for everything
		{
			receipt.owners.add(new ReceiptOwner(receipt, receipt.creator, total));
		}

		if(subrounds != null)
		{
			for(SubroundInput input : subrounds)
			{
				if(input.members != null)
				{
					Subpot subpot = new Subpot(input.parsedAmount);
					subpot.calculation = input.calculation;
					subpot.description = input.description;
					for (Long id : input.members)
					{
						User u = User.findById(id);
						subpot.members.add(u);
					}
					// Why the fuck do i have to do both?
					receipt.subpots.add(subpot);
					subpot.receipt = receipt;
				}
			}
		}
		receipt.save();

		Payment.generatePayments(receipt);
		Receipts.show(receipt.id);
	}

	public static void details(Long id) 
	{
		Receipt receipt = Receipt.findById(id);
		render(receipt);
	}
	
	private static void registerCommon(String error, ReceiptData existingData) {
		List<User> members = User.find("order by fullname asc").fetch();
		User user = Security.connectedUser();
		
		render("Receipts/register.html", members, user, error, existingData);
	}
	
	public static void register(String error, ReceiptData existingData)
	{
		registerCommon(null, null);
	}
}
