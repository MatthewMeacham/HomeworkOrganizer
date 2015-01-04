package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import play.data.validation.Constraints.*;

@Entity
public class SchoolClass extends Model {
	
	@Id
	public Long id;
	@Required
	public String subject;
	@ManyToOne
	public Student student;
	
	public static Finder<Long, SchoolClass> find = new Finder<Long, SchoolClass>(Long.class, SchoolClass.class);

	public SchoolClass(String subject, Student student) {
		this.subject = subject;
		this.student = student;
	}
	
	public static SchoolClass create(String subject, String studentEmail) {
		SchoolClass schoolClass = new SchoolClass(subject, Student.find.ref(studentEmail));
		schoolClass.save();
		return schoolClass;
	}
}