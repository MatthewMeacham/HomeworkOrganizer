package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Parent extends Model {
	private static final long serialVersionUID = 1L;

	@Required
	public String name;
	@Id
	@Required
	public String email;
	@Required
	public String password;
	public String salt;

	public static Finder<String, Parent> find = new Finder<String, Parent>(String.class, Parent.class);

	public Parent(String name, String email, String salt, String password) {
		this.email = email;
		this.salt = salt;
		this.password = password;
		this.name = name;
	}

	public static Parent create(String name, String email, String salt, String password) {
		if (find.where().eq("email", email.toLowerCase()).findUnique() == null && Student.find.where().eq("email", email).findUnique() == null && Teacher.find.where().eq("email", email).findUnique() == null) {
			Parent parent = new Parent(name, email, salt, password);
			parent.save();
			return parent;
		}
		return null;
	}

	public static Parent authenticate(String email, String password) {
		Parent parent = find.where().eq("email", email.toLowerCase()).eq("password", password).findUnique();
		if (parent == null) return null;
		return parent;
	}

	public static boolean exists(String email) {
		Parent parent = find.where().eq("email", email.toLowerCase()).findUnique();
		if (parent == null) return false;
		return true;
	}
}
