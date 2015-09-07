package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.persistence.PersistenceException;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import com.matthew.hasher.Hasher;

import models.Parent;
import models.Student;
import models.Teacher;

import views.html.studentSignUp;
import views.html.studentProfile;
import views.html.parentSignUp;
import views.html.parentProfile;
import views.html.teacherSignUp;
import views.html.teacherProfile;
import views.html.accountTypeSelector;

public class Signups extends Controller {

	private static Form<Student> studentForm = Form.form(Student.class);
	private static Form<Parent> parentForm = Form.form(Parent.class);
	private static Form<Teacher> teacherForm = Form.form(Teacher.class);

	private static final Hasher HASHER = new Hasher();

	// Directs the request to the sign up page
	public Result accountTypeSelector() {
		return ok(accountTypeSelector.render(""));
	}

	// Directs the request to the student sign up
	public Result studentForm() {
		return ok(studentSignUp.render(studentForm, ""));
	}

	// Directs the request to the parent sign up
	public Result parentForm() {
		return ok(parentSignUp.render(parentForm, ""));
	}

	// Directs the request to the teacher sign up page
	public Result teacherForm() {
		return ok(teacherSignUp.render(teacherForm, ""));
	}

	// Create a new student from the request
	public Result createStudent() {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) return badRequest(studentSignUp.render(studentForm, "Form had errors."));
		String email = filledForm.data().get("email").toLowerCase();
		String name = filledForm.data().get("name");
		String password = filledForm.data().get("password");
		String passwordAgain = filledForm.data().get("passwordAgain");
		String grade = filledForm.data().get("grade");
		if (Integer.valueOf(grade) <= 8) return badRequest(studentSignUp.render(studentForm, "Sorry, if you are not in at least ninth grade, a parent must create an account and add you."));
		if (!email.contains("@") || !email.contains(".") || email.trim().isEmpty()) return badRequest(studentSignUp.render(studentForm, "Invalid email address."));
		if (Parent.exists(email) || Teacher.exists(email) || Student.exists(email)) return badRequest(studentSignUp.render(studentForm, "That email is already associated with an account."));
		if (email.length() >= 250) return badRequest(studentSignUp.render(studentForm, "Email was too long."));
		if (name.length() >= 250) return badRequest(studentSignUp.render(studentForm, "Name was too long."));
		if (name.trim().isEmpty()) return badRequest(studentSignUp.render(studentForm, "Invalid name."));
		if (password.trim().isEmpty() || passwordAgain.trim().isEmpty()) return badRequest(studentSignUp.render(studentForm, "Invalid password."));
		if (password.length() < 8 || passwordAgain.length() < 8) return badRequest(studentSignUp.render(studentForm, "Password must be at least 8 characters long."));
		if (!password.equals(passwordAgain)) return badRequest(studentSignUp.render(studentForm, "Passwords did not match."));
		String[] hashed = null;
		try {
			hashed = HASHER.hashSHA256(password);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return badRequest(studentSignUp.render(studentForm, "Error while processing."));
		}
		Student student = null;
		try {
			student = Student.create(name, email, hashed[0], hashed[1], filledForm.data().get("grade"));
		} catch (PersistenceException e) {
			return badRequest(studentSignUp.render(studentForm, "Error while processing."));
		}
		if (student == null) return badRequest(studentSignUp.render(studentForm, "That email is already associated with an account."));

		Utilities.createCookies(session(), "userID", student.id.toString());

		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
	}

	// Create a new parent from the request
	public Result createParent() {
		Form<Parent> filledForm = parentForm.bindFromRequest();
		if (filledForm.hasErrors()) return badRequest(parentSignUp.render(parentForm, "Form had errors."));
		String email = filledForm.data().get("email").toLowerCase();
		String name = filledForm.data().get("name");
		String password = filledForm.data().get("password");
		String passwordAgain = filledForm.data().get("passwordAgain");
		if (!email.contains("@") || !email.contains(".") || email.trim().isEmpty()) return badRequest(parentSignUp.render(parentForm, "Invalid email address."));
		if (Student.exists(email) || Teacher.exists(email) || Parent.exists(email)) return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));
		if (email.length() >= 250) return badRequest(parentSignUp.render(parentForm, "Email was too long."));
		if (name.length() >= 250) return badRequest(parentSignUp.render(parentForm, "Name was too long."));
		if (name.trim().isEmpty()) return badRequest(parentSignUp.render(parentForm, "Invalid name."));
		if (password.trim().isEmpty() || passwordAgain.trim().isEmpty()) return badRequest(parentSignUp.render(parentForm, "Invalid password."));
		if (password.length() < 8 || passwordAgain.length() < 8) return badRequest(parentSignUp.render(parentForm, "Password must be at least 8 characters long."));
		if (!password.equals(passwordAgain)) return badRequest(parentSignUp.render(parentForm, "Passwords did not match."));
		String[] hashed = null;
		try {
			hashed = HASHER.hashSHA256(password);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return badRequest(parentSignUp.render(parentForm, "Error while processing, try again."));
		}
		Parent parent = null;
		try {
			parent = Parent.create(name, email, hashed[0], hashed[1]);
		} catch (PersistenceException e) {
			return badRequest(parentSignUp.render(parentForm, "Error while processing."));
		}
		if (parent == null) return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));

		Utilities.createCookies(session(), "userID", parent.id.toString());

		return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", ""));
	}

	// Create a new Teacher account from the request
	public Result createTeacher() {
		Form<Teacher> filledForm = teacherForm.bindFromRequest();
		if (filledForm.hasErrors()) return badRequest(teacherSignUp.render(teacherForm, "Form had errors."));
		String email = filledForm.data().get("email").toLowerCase();
		String name = filledForm.data().get("name");
		String password = filledForm.data().get("password");
		String passwordAgain = filledForm.data().get("passwordAgain");
		if (!email.contains("@") || !email.contains(".") || email.trim().isEmpty()) return badRequest(teacherSignUp.render(teacherForm, "Invalid email address."));
		if (Student.exists(email) || Parent.exists(email) || Teacher.exists(email)) return badRequest(teacherSignUp.render(teacherForm, "That email is already associated with an account."));
		if (email.length() >= 250) return badRequest(teacherSignUp.render(teacherForm, "Email was too long."));
		if (name.length() >= 250) return badRequest(teacherSignUp.render(teacherForm, "Name was too long."));
		if (name.trim().isEmpty()) return badRequest(teacherSignUp.render(teacherForm, "Invalid name."));
		if (password.trim().isEmpty() || passwordAgain.trim().isEmpty()) return badRequest(teacherSignUp.render(teacherForm, "Invalid password."));
		if (password.length() < 8 || passwordAgain.length() < 8) return badRequest(teacherSignUp.render(teacherForm, "Password must be at least 8 characters long."));
		if (!password.equals(passwordAgain)) return badRequest(teacherSignUp.render(teacherForm, "Passwords did not match."));
		String[] hashed = null;
		try {
			hashed = HASHER.hashSHA256(password);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Teacher teacher = null;
		try {
			teacher = Teacher.create(name, email, hashed[0], hashed[1]);
		} catch (PersistenceException e) {
			return badRequest(teacherSignUp.render(teacherForm, "Error while processing."));
		}
		if (teacher == null) return badRequest(teacherSignUp.render(teacherForm, "That email is already associated with an account."));

		Utilities.createCookies(session(), "userID", teacher.id.toString());

		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
	}

}
