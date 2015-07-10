package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

@Entity
public class Student extends User {
	private static final long serialVersionUID = 1L;

	@Required
	public String name;
	@Required
	public String email;
	@Required
	public String password;
	public String salt;
	@Required
	public String grade;

	@ManyToOne
	public Parent parent;

	@OneToMany
	public Teacher teacher;

	public static Model.Finder<Long, Student> find = new Model.Finder<Long, Student>(Student.class);

	public Student(String name, String email, String salt, String password, String grade) {
		this.email = email;
		this.salt = salt;
		this.password = password;
		this.name = name;
		this.grade = grade;
	}

	public static Student create(String name, String email, String salt, String password, String grade) {
		if (find.where().eq("email", email.toLowerCase()).eq("password", password).findUnique() == null && Teacher.find.where().eq("email", email).findUnique() == null) {
			Student student = new Student(name, email, salt, password, grade);
			student.save();
			return student;
		}
		return null;
	}

	public static Student authenticate(String email, String password) {
		Student student = find.where().eq("email", email.toLowerCase()).eq("password", password).findUnique();
		if (student == null) return null;
		return student;
	}

	public static boolean exists(String email) {
		Student student = find.where().eq("email", email.toLowerCase()).findUnique();
		if (student == null) return false;
		return true;
	}

}