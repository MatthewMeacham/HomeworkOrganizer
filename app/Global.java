import play.*;
import play.libs.*;

import com.avaje.ebean.Ebean;

import models.*;

import java.util.*;

public class Global extends GlobalSettings {

	//this will change all the data in the database to the data contained in the test-data.yml file
	private boolean overwriteAllDataInDatabase = false;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void onStart(Application app) {
		if(Student.find.findRowCount() == 0 || overwriteAllDataInDatabase){
			Ebean.save((List) Yaml.load("test-data.yml"));
		}
	}
	
}