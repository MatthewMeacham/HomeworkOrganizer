package models;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class Teacher extends Model {
	private static final long serialVersionUID = 1L;

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