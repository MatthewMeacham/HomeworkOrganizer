package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import com.matthew.hasher.Hasher;

import models.Parent;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.parentProfile;
import views.html.parentSignUp;
import views.html.profile;
import views.html.studentSignUp;
import views.html.signup;

public class Signups extends Controller {

	private static Form<Student> studentForm = Form.form(Student.class);
	private static Form<Parent> parentForm = Form.form(Parent.class);
	private static Form<Teacher> teacherForm = Form.form(Teacher.class);

	private static final Hasher HASHER = new Hasher();

	// Directs the request to the sign up page
	public Result signup() {
		return ok(signup.render(""));
	}

	// Directs the request to the student sign up
	public Result studentSignup() {
		return ok(studentSignUp.render(studentForm, ""));
	}

	// Directs the request to the parent sign up
	public Result parentSignup() {
		return ok(parentSignUp.render(parentForm, ""));
	}

	// Directs the request to the teacher sign up page
	public Result teacherSignup() {
		return ok(views.html.teacherSignUp.render(teacherForm, ""));
	}

	// Create a new student from the request
	public Result newStudent() {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(studentSignUp.render(studentForm,
					"Form had errors."));
		} else {
			String email = filledForm.data().get("email").toLowerCase();
			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			if (!email.contains("@") || !email.contains(".")
					|| email.trim().isEmpty() || email.equals(""))
				return badRequest(studentSignUp.render(studentForm,
						"Invalid email address."));
			if (Parent.exists(email) || Teacher.exists(email)
					|| Student.exists(email))
				return badRequest(studentSignUp.render(studentForm,
						"That email is already associated with an account."));
			if (email.length() >= 250)
				return badRequest(studentSignUp.render(studentForm,
						"Email was too long."));
			if (name.length() >= 250)
				return badRequest(studentSignUp.render(studentForm,
						"Name was too long."));
			if (name.trim().isEmpty())
				return badRequest(studentSignUp.render(studentForm,
						"Invalid name."));
			if (password.trim().isEmpty())
				return badRequest(studentSignUp.render(studentForm,
						"Invalid password."));
			String[] hashed = null;
			try {
				hashed = HASHER.hashSHA256(password);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Student student = Student.create(name, email, hashed[0], hashed[1],
					filledForm.data().get("grade"));
			if (student == null) {
				return badRequest(studentSignUp.render(studentForm,
						"That email is already associated with an account."));
			}
			return ok(profile.render(student,
					Utilities.createSchoolClassesList(student),
					Utilities.createAssignmentsList(student),
					Utilities.createFinishedAssignmentsList(student),
					Utilities.createLateAssignmentsList(student),
					Utilities.createTeachersList(student),
					Utilities.createNotesList(student), Utilities.today,
					"overview", ""));
		}
	}

	// Create a new parent from the request
	public Result newParent() {
		Form<Parent> filledForm = parentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(parentSignUp.render(parentForm,
					"Form had errors."));
		} else {
			String email = filledForm.data().get("email").toLowerCase();
			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			if (!email.contains("@") || !email.contains(".")
					|| email.trim().isEmpty() || email.equals(""))
				return badRequest(parentSignUp.render(parentForm,
						"Invalid email address."));
			if (Student.exists(email) || Teacher.exists(email)
					|| Parent.exists(email))
				return badRequest(parentSignUp.render(parentForm,
						"That email is already associated with an account."));
			if (email.length() >= 250)
				return badRequest(parentSignUp.render(parentForm,
						"Email was too long."));
			if (name.length() >= 250)
				return badRequest(parentSignUp.render(parentForm,
						"Name was too long."));
			if (name.trim().isEmpty())
				return badRequest(parentSignUp.render(parentForm,
						"Invalid name."));
			if (password.trim().isEmpty())
				return badRequest(parentSignUp.render(parentForm,
						"Invalid password."));
			String[] hashed = null;
			try {
				hashed = HASHER.hashSHA256(password);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Parent parent = Parent.create(name, email, hashed[0], hashed[1]);
			if (parent == null)
				return badRequest(parentSignUp.render(parentForm,
						"That email is already associated with an account."));
			return ok(parentProfile.render(parent,
					Utilities.createChildrenList(parent),
					Utilities.createAssignmentsListForParent(parent),
					Utilities.today, "", ""));
		}
	}

	// Create a new Teacher account from the request
	public Result newTeacher() {
		Form<Teacher> filledForm = teacherForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.teacherSignUp.render(teacherForm,
					"Form had errors."));
		} else {
			String email = filledForm.data().get("email").toLowerCase();
			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			if (!email.contains("@") || !email.contains(".")
					|| email.trim().isEmpty() || email.equals(""))
				return badRequest(views.html.teacherSignUp.render(teacherForm,
						"Invalid email address."));
			if (Student.exists(email) || Parent.exists(email)
					|| Teacher.exists(email))
				return badRequest(views.html.teacherSignUp.render(teacherForm,
						"That email is already associated with an account."));
			if (email.length() >= 250)
				return badRequest(views.html.teacherSignUp.render(teacherForm,
						"Email was too long."));
			if (name.length() >= 250)
				return badRequest(views.html.teacherSignUp.render(teacherForm,
						"Name was too long."));
			if (name.trim().isEmpty())
				return badRequest(views.html.teacherSignUp.render(teacherForm,
						"Invalid name."));
			if (password.trim().isEmpty())
				return badRequest(views.html.teacherSignUp.render(teacherForm,
						"Invalid password."));
			String[] hashed = null;
			try {
				hashed = HASHER.hashSHA256(password);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Teacher teacher = Teacher.create(name, email, hashed[0], hashed[1]);
			if (teacher == null)
				return badRequest(views.html.teacherSignUp.render(teacherForm,
						"That email is already associated with an account."));
			return ok(views.html.teacherProfile.render(teacher,
					Utilities.createAssignmentsListForTeacher(teacher),
					Utilities.createSchoolClassListForTeacher(teacher),
					Utilities.today, "", ""));
		}
	}

}
