package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.PersistenceException;

import com.avaje.ebean.Model;

@Entity
public class Parent extends AUser {

	public static Model.Finder<UUID, Parent> find = new Model.Finder<UUID, Parent>(Parent.class);

	public Parent(String name, String email, String salt, String password) {
		this.email = email;
		this.salt = salt;
		this.password = password;
		this.name = name;
	}

	public static Parent create(String name, String email, String salt, String password) throws PersistenceException{
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
		return !(find.where().eq("email", email.toLowerCase()).findUnique() == null);
	}
}
