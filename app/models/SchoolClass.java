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
	@ManyToOne
	public Student student;

	public static Finder<Long, SchoolClass> find = new Finder<Long, SchoolClass>(Long.class, SchoolClass.class);

	public SchoolClass(String subject, Student student) {
		this.subject = subject;
		this.student = student;
	}

	public static SchoolClass create(String subject, Long studentId) {
		SchoolClass schoolClass = new SchoolClass(subject, Student.find.ref(studentId));
		schoolClass.save();
		return schoolClass;
	}
}