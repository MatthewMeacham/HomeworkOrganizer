package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;

@Entity
public class SchoolClass extends Model {
	
	@Id
	public Long id;
	public String type;
	public Student student;
	
	public static Finder<Long, SchoolClass> find = new Finder<Long, SchoolClass>(Long.class, SchoolClass.class);
	
	public SchoolClass(String type, Student student) {
		this.type = type;
		this.student = student;
	}
	
	public static SchoolClass create(String type, String studentEmail) {
		SchoolClass newClass = new SchoolClass(type, Student.find.ref(studentEmail));
		newClass.save();
		return newClass;
	}
	
}