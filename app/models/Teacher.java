package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import java.util.ArrayList;

@Entity
public class Teacher extends Model {
	
	@Id
	public Long id;
	public String name;
	public ArrayList<String> specialties = new ArrayList<String>();
	@OneToOne
	public SchoolClass schoolClass;
	
	public static Finder<Long, Teacher> find = new Finder<Long, Teacher>(Long.class, Teacher.class);
	
	public Teacher(String name, ArrayList<String> specialties, SchoolClass schoolClass) {
		this.name = name;
		this.specialties = specialties;
		this.schoolClass = schoolClass;
	}
	
	public static Teacher create(String name, ArrayList<String> specialties, Long schoolClassId) {
		Teacher teacher = new Teacher(name, specialties, SchoolClass.find.ref(schoolClassId));
		teacher.save();
		return teacher;
	}
	
}