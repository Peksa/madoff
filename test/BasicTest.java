import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

	@Before
	public void setup()
	{
		Fixtures.deleteAllModels();
	}
	
    @Test
    public void createAndRetrieveUser()
    {
    	new User("bob@bob.com", "bob", "secret", "Bobby").save();
    	
    	User bob = User.find("byEmail", "bob@bob.com").first();
    	
    	// Test
    	assertNotNull(bob);
    	assertEquals("Bobby", bob.fullname);
    }
    
    @Test
    public void createReceipt()
    {
    	User bob = new User("bob@bob.com", "bob", "secret", "Bobby").save();
    	//User ann = new User("ann@ann.com", "ann", "verysecret", "Anne");
    	
    	new Receipt("Hotel-bar 23/4", 432, bob, "Can't remember, but found receipt").save();
    	
    	
    	// Get bob's receipts
    	List<Receipt> receipts = Receipt.find("byOwner", bob).fetch();
    	
    	Receipt firstReceipt = receipts.get(0);
    	
    	assertEquals(1, Receipt.count());
    	assertNotNull(firstReceipt);
    	assertEquals(bob, firstReceipt.owner);
    	assertEquals("Hotel-bar 23/4", firstReceipt.title);
    	assertEquals("Can't remember, but found receipt", firstReceipt.description);
    	assertNotNull(firstReceipt.date);
    	
    }
    

}
