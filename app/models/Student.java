package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceException;

import play.data.validation.Constraints.Required;

import com.avaje.ebean.Model;

@Entity
public class Student extends AUser {

	@Required
	public String grade;

	@ManyToOne
	public Parent parent;

	@OneToMany
	public Teacher teacher;

	public static Model.Finder<UUID, Student> find = new Model.Finder<UUID, Student>(Student.class);

	public Student(String name, String email, String salt, String password, String grade) {
		this.name = name;
		this.email = email;
		this.salt = salt;
		this.password = password;
		this.grade = grade;
	}

	public static Student create(String name, String email, String salt, String password, String grade) throws PersistenceException{
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
		return find.where().eq("email", email.toLowerCase()).findList().size() > 0;
	}

}