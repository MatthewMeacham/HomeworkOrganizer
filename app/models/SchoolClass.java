package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class SchoolClass extends Model {
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;
	@Required
	public String subject;
	@Required
	public String color;
	//TODO CHANGE TO FOREIGN KEY
	@ManyToOne
	public Student student;
	
	public String password;
	

	public static Finder<Long, SchoolClass> find = new Finder<Long, SchoolClass>(Long.class, SchoolClass.class);

	public SchoolClass(String subject, Student student, String color) {
		this.subject = subject;
		this.student = student;
		this.color = color;
	}

	public static SchoolClass create(String subject, Long studentId, String color) {
		SchoolClass schoolClass = new SchoolClass(subject, Student.find.ref(studentId), color);
		schoolClass.save();
		return schoolClass;
	}
	
	public static void edit(Long id, String subject, String color, Student student) {
		SchoolClass schoolClass = find.ref(id);
		schoolClass.subject = subject;
		schoolClass.color = color;
		schoolClass.student = student;
		schoolClass.save();
	}
}