import models.Student;
import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {

	// this will change all the data in the database to the data contained in the test-data.yml file
	private boolean overwriteAllDataInDatabase = false;

	@Override
	public void onStart(Application app) {
		if (Student.find.findRowCount() == 0 || overwriteAllDataInDatabase) {
			// Ebean.save((List) Yaml.load("test-data.yml"));
		}
	}

}