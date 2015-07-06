package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Teacher extends Model {
	private static final long serialVersionUID = 1L;

	@Id
	public String email;
	@Required
	public String password;
	@Required
	public String name;

	@OneToMany
	public SchoolClass schoolClass;

	public static Finder<Long, Teacher> find = new Finder<Long, Teacher>(Long.class, Teacher.class);

	public Teacher(String email, String password, String name) {
		this.email = email;
		this.password = password;
		this.name = name;
	}

	public static Teacher create(String email, String password, String name) {
		if (find.where().eq("email", email.toLowerCase()).eq("password", password).findUnique() == null && Parent.find.where().eq("email", email.toLowerCase()).findUnique() == null && Student.find.where().eq("email", email.toLowerCase()).findUnique() == null) {
			Teacher teacher = new Teacher(email, password, name);
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