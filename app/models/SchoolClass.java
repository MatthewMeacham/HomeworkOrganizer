package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceException;

import play.data.validation.Constraints.Required;

import com.avaje.ebean.Model;

@Entity
public class SchoolClass extends Model {

	@Id
	public Long id;
	@Required
	public String subject;
	@Required
	public String color;

	@ManyToMany(cascade = CascadeType.REMOVE)
	public List<Student> students = new ArrayList<Student>();

	public UUID teacherID;

	public String password;

	public static Finder<Long, SchoolClass> find = new Finder<Long, SchoolClass>(SchoolClass.class);

	public SchoolClass(String subject, String email, UUID foreignID, String color, String password) {
		this.subject = subject;
		if (Student.find.where().eq("email", email).findList().size() > 0) this.students.add(Student.find.where().eq("ID", foreignID).findUnique());
		else if (Teacher.find.where().eq("email", email).findUnique() != null) this.teacherID = foreignID;
		this.color = color;
		this.password = password;
	}

	public static SchoolClass create(String subject, String email, UUID foreignID, String color, String password) throws PersistenceException {
		SchoolClass schoolClass = new SchoolClass(subject, email, foreignID, color, password);
		schoolClass.save();
		return schoolClass;
	}

	public static void edit(Long id, String subject, String color, UUID foreignID, String password) throws PersistenceException {
		SchoolClass schoolClass = find.where().eq("ID", id).findUnique();
		if(schoolClass == null) throw new PersistenceException("Unable to find class with that ID");
		schoolClass.subject = subject;
		schoolClass.color = color;
		schoolClass.password = password;
		schoolClass.save();
	}
}