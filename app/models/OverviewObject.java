package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import play.data.validation.Constraints.*;

@Entity
public class OverviewObject extends Model {
	
	@Id
	public Long id;	
	@ManyToOne
	public SchoolClass schoolClass;
	public String date;
	public String description;
	public int month;
	public int day;
	public int year;
	//this is set to year * 366 - (12 - month) * 31 - (31 - day)
	public int total;
	public String spanner;
	
	public static Finder<Long, OverviewObject> find = new Finder<Long, OverviewObject>(Long.class, OverviewObject.class);
	
	public OverviewObject(Homework homework) {
		id = homework.id;
		schoolClass = homework.schoolClass;
		date = homework.dueDate;
		description = homework.description;
		month = homework.month;
		day = homework.day;
		year = homework.year;
		total = homework.total;
		spanner = "H";
	}
	
	public OverviewObject(Project project) {
		id = project.id;
		schoolClass = project.schoolClass;
		date = project.dueDate;
		description = project.description;
		month = project.month;
		day = project.day;
		year = project.year;
		total = project.total;
		spanner = "P";
	}
	
	public OverviewObject(Test test) {
		id = test.id;
		schoolClass = test.schoolClass;
		date = test.dateOf;
		description = test.description;
		month = test.month;
		day = test.day;
		year = test.year;
		total = test.total;
		spanner = "T";
	}
	
	public static OverviewObject create(Homework homework) {
		OverviewObject overviewObject = new OverviewObject(homework);
		return overviewObject;
	}
	
	public static OverviewObject create(Test test) {
		OverviewObject overviewObject = new OverviewObject(test);
		return overviewObject;
	}
	
	public static OverviewObject create(Project project) {
		OverviewObject overviewObject = new OverviewObject(project);
		return overviewObject;
	}
	
}