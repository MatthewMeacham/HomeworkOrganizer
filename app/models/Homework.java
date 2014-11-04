package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import java.util.Date;

@Entity
public class Homework extends Model {
	
	@Id
	public Long id;
	public Date dueDate;
	@ManyToOne
	public SchoolClass schoolClass; 
	
	public static Finder<Long, Homework> find = new Finder<Long, Homework>(Long.class, Homework.class);
	
	public Homework(Date dueDate, SchoolClass schoolClass) {
		this.dueDate = dueDate;
		this.schoolClass = schoolClass;
	}
	
	public static Homework create(Homework homework) {
		//Homework homework = new Homework(dueDate, SchoolClass.find.ref(schoolClassId));
		homework.save();
		return homework;
	}
	
	
}