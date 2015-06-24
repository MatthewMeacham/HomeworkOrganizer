package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Student extends Model {
	private static final long serialVersionUID = 1L;

	@Required
	public String name;
	@Required
	public String email;
	@Required
	public String password;
	@Required
	public String grade;

	@Id
	public Long id;
	
	@OneToMany
	public Teacher teacher;
	@OneToMany
	public Note note;
	@OneToMany
	public Assignment assignment;
	@OneToMany
	public Assignment finishedAssignment;
	@OneToMany
	public Assignment lateAssignment;
	
	@ManyToOne
	public Parent parent;

	public static Finder<Long, Student> find = new Finder<Long, Student>(Long.class, Student.class);

	public Student(String name, String email, String password, String grade) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.grade = grade;
	}

	public static Student create(String name, String email, String password, String grade) {
		if (find.where().eq("email", email).eq("password", password).findUnique() == null) {
			Student student = new Student(name, email, password, grade);
			student.save();
			return student;
		}
		return null;
	}

	public static Student authenticate(String email, String password) {
		Student student = find.where().eq("email", email).eq("password", password).findUnique();
		if (student == null) return null;
		System.err.println("found student " + student.email);
		return student;
	}

	public static boolean exists(String email) {
		Student student = find.where().eq("email", email).findUnique();
		if (student == null) return false;
		return true;
	}

}