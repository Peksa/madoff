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
		}
	}

}