package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;

import play.data.validation.Constraints.*;

@Entity
public class Student extends Model{

	@Required
	public String name;
	@Id
	@Required
	public String email;
	@Required
	public String password;
	@Required
	public String grade;
	
	public static Finder<String, Student> find = new Finder<String, Student>(String.class, Student.class);

	public Student(String name, String email, String password, String grade) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.grade = grade;
	}
	
	//TODO MAKE SURE TO CHANGE THIS IN ORDER TO ONLY MAKE IT SO THERE CAN ONLY BE ONE USER WITH THE INFORMATION, SO TWO PEOPLE CANT HAVE THE SAME 
	//TODO EMAIL.... 
	public static Student create(String name, String email, String password, String grade) {
		Student student = new Student(name, email, password, grade);
		student.save();
		return student;
	}
	
	public static Student authenticate(String email, String password) {
		Student student =  find.where().eq("email", email).eq("password", password).findUnique();
		System.err.println("found student " + student.email);
		return student;
	}
	
}