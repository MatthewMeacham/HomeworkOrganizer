package controllers;

import play.*;
import play.mvc.*;
import models.*;
import play.data.*;
import views.html.*;
import  play.mvc.Http.Session;
import  play.mvc.Http;
import static play.data.Form.*;
import java.util.List;

public class Application extends Controller {

	private static Form<Student> studentForm = Form.form(Student.class);
	private static Form<SchoolClass> schoolClassForm = Form.form(SchoolClass.class);
	private static Form<Homework> homeworkForm = Form.form(Homework.class);
	private static Form<Test> testForm = Form.form(Test.class);
	private static Form<Note> noteForm = Form.form(Note.class);
	private static Form<Teacher> teacherForm = Form.form(Teacher.class);
	
	private static Student student;
	private static List<Homework> homeworks;
	private static List<SchoolClass> schoolClasses;
	private static List<Teacher> teachers;
	private static List<Test> tests;
	private static List<Note> notes;
	
	static Session session = Http.Context.current().session();

    public static Result index() {
		return ok(index.render(Student.find.all(), Form.form(Login.class)));
    }
	
	public static Result signup() {
		return ok(signup.render(studentForm));
	}

	public static Result login() {
		return ok(login.render(Form.form(Login.class)));
	}

	public static Result logout() {
		session.clear();
		return redirect(routes.Application.index());
	}

	public static Result students() {
		return ok(students.render(Student.find.all()));
	}
	
	public static Result newStudent() {	
		Form<Student> filledForm = studentForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(index.render(Student.find.all(), Form.form(Login.class)));
		} else {
			Student.create(filledForm.get());
			return ok(index.render(Student.find.all(), Form.form(Login.class)));
		}
	}
	
	public static Result newSchoolClass() {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			System.out.println("HAD ERRORS");
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes));
		} else {		
			SchoolClass newSchoolClass = SchoolClass.create(filledForm.data().get("subject"), filledForm.data().get("studentEmail"));
			System.out.println("SCHOOLCLASS: " + newSchoolClass);
			schoolClasses.add(newSchoolClass);
			System.out.println("NO ERRORS");
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes));
		}	
	}
	
	public static String getSubject(Homework homework) {
		if(homework == null) return ""; 
		try {
			System.out.println("SUBJECT: " + homework.schoolClass.subject);
			if(homework.schoolClass.subject != null) {
				return homework.schoolClass.subject;
			}
		} catch(Exception e) {
			return "";
		}
		return "";
	}
	
	public static Result newHomework() {
		Form<Homework> filledForm = homeworkForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			System.out.println("HAD ERRORS");
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes));
		} else {
			System.out.println("DID NOT HAVE ERRORS");
			Homework newHomework = Homework.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"));
			homeworks.add(newHomework);
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes));
		}
	}

	public static Result profileLogin() {
		homeworks = Homework.find.findList();
		schoolClasses = SchoolClass.find.findList();
		teachers = Teacher.find.findList();
		tests = Test.find.findList();
		notes = Note.find.findList();
		student = Student.find.ref(session.get("email"));
		
		return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes));
	}
						
	public static Result authenticate() {
		Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
		if(loginForm.hasErrors()) {
			return badRequest(index.render(Student.find.all(), Form.form(Login.class)));
		} else {
			session.clear();
			session.put("email", loginForm.get().email);
			return redirect(routes.Application.profileLogin());
		}
	}

	public static class Login {
		public String email;
		public String password;
		
		public String validate() {
			if(Student.authenticate(email, password) == null) {
				return "Invalid email or password";
			} 
			System.err.println("Authenticated");
			return null;
		}
	}
	
}
