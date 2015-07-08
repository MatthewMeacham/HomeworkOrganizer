package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

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

	@ManyToMany(cascade = CascadeType.REMOVE)
	public List<Student> students = new ArrayList<Student>();

	public Long teacherID;

	public String password;

	public static Finder<Long, SchoolClass> find = new Finder<Long, SchoolClass>(Long.class, SchoolClass.class);

	public SchoolClass(String subject, String email, long foreignID, String color, String password) {
		this.subject = subject;
		if (Student.find.where().eq("email", email).findUnique() != null) this.students.add(Student.find.ref(foreignID));
		else if (Teacher.find.where().eq("email", email).findUnique() != null) this.teacherID = foreignID;
		this.color = color;
		this.password = password;
	}

	public static SchoolClass create(String subject, String email, Long foreignID, String color, String password) {
		SchoolClass schoolClass = new SchoolClass(subject, email, foreignID, color, password);
		schoolClass.save();
		return schoolClass;
	}

	public static void edit(Long id, String subject, String color, long foreignID, String password) {
		SchoolClass schoolClass = find.ref(id);
		schoolClass.subject = subject;
		schoolClass.color = color;
		schoolClass.password = password;
		schoolClass.save();
	}
}