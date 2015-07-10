package models;

import javax.persistence.Entity;

import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

@Entity
public class Parent extends User {
	private static final long serialVersionUID = 1L;

	@Required
	public String name;
	@Required
	public String email;
	@Required
	public String password;
	public String salt;

	public static Model.Finder<Long, Parent> find = new Model.Finder<Long, Parent>(Parent.class);

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
