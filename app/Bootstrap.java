import java.util.List;

import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

@OnApplicationStart
public class Bootstrap extends Job
{
	public void doJob()
	{
		// Check if the database is empty
		if (User.count() == 0)
		{
			Logger.debug("Loading initial data from YAML");
			Fixtures.loadModels("initial-data.yml");
			
			// Perform payment generation on yml-data
			List<Receipt> list = Receipt.findAll();
			for(Receipt r : list)
			{
				if(r.payments.size() == 0) Payment.generatePayments(r);
			}
		}
	}

}