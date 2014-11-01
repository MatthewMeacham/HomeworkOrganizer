package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;

@Entity
public class Student extends Model{

	@Id
	public String email;
	public String name;
	public int grade;
	public boolean male;
	
	public static Finder<String, Student> find = new Finder<String, Student>(String.class, Student.class);

	public Student(String email, String name, int grade, boolean male) {
		this.email = email;
		this.name = name;
		this.grade = grade;
		this.male = male;
	}
	
	public static Student create(Student student) {
		student.save();
		return student;
	}
	
}