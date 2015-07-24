package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceException;

import com.avaje.ebean.Model;

@Entity
public class Teacher extends AUser {

	@OneToMany
	public SchoolClass schoolClass;

	public static Model.Finder<UUID, Teacher> find = new Model.Finder<UUID, Teacher>(Teacher.class);

	public Teacher(String name, String email, String salt, String password) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.salt = salt;
	}

	public static Teacher create(String name, String email, String salt, String password) throws PersistenceException{
		if (find.where().eq("email", email.toLowerCase()).eq("password", password).findUnique() == null && Parent.find.where().eq("email", email.toLowerCase()).findUnique() == null && Student.find.where().eq("email", email.toLowerCase()).findUnique() == null) {
			Teacher teacher = new Teacher(name, email, salt, password);
			teacher.save();
			return teacher;
		}
		return null;
	}

	public static Teacher authenticate(String email, String password) {
		Teacher teacher = find.where().eq("email", email).eq("password", password).findUnique();
		if (teacher == null) return null;
		return teacher;
	}

	public static boolean exists(String email) {
		Teacher teacher = find.where().eq("email", email).findUnique();
		if (teacher == null) return false;
		return true;
	}

}