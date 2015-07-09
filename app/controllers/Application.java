package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.PersistenceException;

import models.Assignment;
import models.Note;
import models.Parent;
import models.SchoolClass;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.parentProfile;
import views.html.parentSignUp;
import views.html.profile;
import views.html.signup;
import views.html.studentSignUp;
import views.html.teacherProfile;

import com.matthew.hasher.Hasher;

//The controller, it controls all data flow and handles/processes requests and returns the correct information
public class Application extends Controller {

	public final static String[] OPTIONS = { "Homework", "Project", "Test", "Final", "Presentation", "Other" };
	private final static int MIN_GRADE = 1;
	private final static int MAX_GRADE = 16;

	private static Form<Student> studentForm = Form.form(Student.class);
	private static Form<SchoolClass> schoolClassForm = Form.form(SchoolClass.class);
	@SuppressWarnings("unused")
	private static Form<Note> noteForm = Form.form(Note.class);
	private static Form<Teacher> teacherForm = Form.form(Teacher.class);
	private static Form<Assignment> assignmentForm = Form.form(Assignment.class);
	private static Form<Parent> parentForm = Form.form(Parent.class);
	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);
	private static Form<Login> loginForm = Form.form(Login.class);
	private static Form<ContactUs> contactUsForm = Form.form(ContactUs.class);
	private static Form<SchoolClassFromCode> schoolClassFromCodeForm = Form.form(SchoolClassFromCode.class);

	private static final Hasher HASHER = new Hasher();

	// The number of days in today, this is set to year * 366 - (12 - month) *
	// 31 - (31 - day)
	private static int today;

	// Directs the request to the index
	public static Result index() {
		return ok(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
	}

	// Directs the request to the sign up page
	public static Result signup() {
		return ok(signup.render(""));
	}

	// Directs the request to the student sign up
	public static Result studentSignup() {
		return ok(studentSignUp.render(studentForm, ""));
	}

	// Directs the request to the parent sign up
	public static Result parentSignup() {
		return ok(parentSignUp.render(parentForm, ""));
	}

	// Directs the request to the teacher sign up page
	public static Result teacherSignup() {
		return ok(views.html.teacherSignUp.render(teacherForm, ""));
	}

	// Directs the request to the login page
	public static Result login() {
		setToday();
		return ok(login.render(loginForm, ""));
	}

	// Redirects the request to the index for logging out
	public static Result logout() {
		return redirect(routes.Application.index());
	}

	// Directs the user to the FAQ page
	public static Result FAQ() {
		return ok(views.html.faq.render());
	}

	// Directs the user to the contact us page
	public static Result contactUsPage() {
		return ok(views.html.contact.render(""));
	}

	// Refreshed the profile page
	public static Result refresh(Long studentID) {
		System.out.println("CALLED");
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		return redirect(routes.Application.profileLogin(String.valueOf(student.id)));
	}

	// Performs the email sending operation and then redirects back to the index
	public static Result contactUs() {
		Form<ContactUs> filledForm = contactUsForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.contact.render("Error while processing."));
		}
		String name = filledForm.data().get("name");
		String email = filledForm.data().get("email");
		String message = filledForm.data().get("message");
		String subject = filledForm.data().get("subject");
		if (name.isEmpty() || name.trim().isEmpty()) return badRequest(views.html.contact.render("Message can't be empty."));
		if (email.isEmpty() || email.trim().isEmpty()) return badRequest(views.html.contact.render("Message can't be empty."));
		if (subject.isEmpty() || subject.trim().isEmpty()) return badRequest(views.html.contact.render("Subject can't be empty."));
		if (message.isEmpty() || message.trim().isEmpty()) return badRequest(views.html.contact.render("Message can't be empty."));
		EmailSender.generateAndSendEmail(name, email, subject, message);
		return redirect(routes.Application.index());
	}

	// Checks to see if the student acocunt with the given studentID is a child
	// account meaning it was parent created
	public static boolean childAccount(String studentID) {
		List<Parent> parents = Parent.find.all();
		Student student = Student.find.ref(Long.valueOf(studentID));
		for (Parent parent : parents) {
			if (student.email.equals(parent.email)) return true;
		}
		return false;
	}

	// Changes the account settings for the student with the given studentID and
	// returns either A) Successful or B) Error
	public static Result changeAccountSettings(String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Error occurred while processing."));
		} else {
			String grade = filledForm.data().get("grade");
			if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid grade level."));
			student.grade = grade;
			student.save();

			String name = filledForm.data().get("name");
			if (!name.equals(student.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Name was too long."));
				student.name = name;
				student.save();
			}

			String email = filledForm.data().get("email").toLowerCase();
			if (!email.equals(student.email)) {
				if (email.equals("") || email.trim().isEmpty() || !email.contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid email address."));
				if (email.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Email was too long."));
				if (Parent.exists(email) || Student.exists(email) || Teacher.exists(email)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Email is already associated with an account."));
				student.email = email;
				student.save();
			}

			try {
				String currentPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("currentPassword"), student.salt);
				String newPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("newPassword"), student.salt);
				String newPasswordAgain = HASHER.hashWithSaltSHA256(filledForm.data().get("newPasswordAgain"), student.salt);
				if (!currentPassword.equals("") || !newPassword.equals("") || !newPasswordAgain.equals("")) {
					if (currentPassword.equals("") || !currentPassword.equals(student.password)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Current password was incorrect."));
					if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid new password."));
					if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid passwords."));
					if (!newPassword.equals(newPasswordAgain)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "New passwords did not match."));
					student.password = newPassword;
					student.save();
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Error while processing."));
			}

			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", "Account changed successfully."));
		}
	}

	// Changes account settings for a parent account OR account settings for one
	// of their kids and return
	// either A) successful or B) Error
	public static Result changeParentAccountSettings(String parentID, String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Parent parent = Parent.find.ref(Long.valueOf(parentID));
		List<Student> children = createChildrenList(parent);
		if (filledForm.hasErrors()) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Error while processing."));

		String name = filledForm.data().get("name");
		String currentPassword = null;
		String newPassword = null;
		String newPasswordAgain = null;
		try {
			currentPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("currentPassword"), parent.salt);
			newPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("newPassword"), parent.salt);
			newPasswordAgain = HASHER.hashWithSaltSHA256(filledForm.data().get("newPasswordAgain"), parent.salt);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// parent request to change themselves
		if (studentID.equals("0")) {
			String email = filledForm.data().get("email").toLowerCase();
			if (!name.equals(parent.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Name was too long."));
				for (Student child : children) {
					if (child.name.equals(name)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "One of your children already has that name."));
				}
				parent.name = name;
				parent.save();
			}

			if (!email.equals(parent.email)) {
				if (email.equals("") || email.trim().isEmpty() || !email.contains("@") || !email.contains(".")) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Invalid email address."));
				if (email.length() >= 250) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Email was too long."));
				if (Parent.exists(email) || Teacher.exists(email) || Student.exists(email)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "That email is already associated with an account."));

				for (Student child : children) {
					child.email = email;
					child.save();
				}
				parent.email = email;
				parent.save();
			}

			if (!currentPassword.equals("") && !newPassword.equals("") && !newPasswordAgain.equals("")) {
				if (currentPassword.equals("") || !currentPassword.equals(parent.password)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Current password was incorrect."));
				if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Invalid new password."));
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Invalid passwords."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "New passwords did not match."));
				for (Student child : children) {
					if (child.password.equals(newPassword)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Child can not have the same password as the parent."));
				}
				parent.password = newPassword;
				parent.save();

			}
			return ok(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "overview", "Account changed successfully."));
		} else {
			// parent request to change student
			Student student = Student.find.ref(Long.valueOf(studentID));

			String grade = filledForm.data().get("grade");
			if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid grade level."));
			student.grade = grade;
			student.save();

			if (!name.equals(student.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Name was too long."));
				student.name = name;
				student.save();
			}

			if (!currentPassword.equals("") && !newPassword.equals("") && !newPasswordAgain.equals("")) {
				if (currentPassword.equals("") || !currentPassword.equals(student.password)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Current password was incorrect."));
				if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid new password."));
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid password."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "New passwords did not match."));
				if (newPassword.equals(parent.password)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Child can not have the same password as the parent."));

				for (Student child : children) {
					if (child == student) continue;
					if (newPassword.equals(child.password)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Child can not have the same password as another."));
				}

				student.password = newPassword;
				student.save();
			}
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "overview", "Account changed successfully."));
		}
	}

	// Changes account settings for a teacher
	public static Result changeTeacherAccountSettings(String teacherID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Teacher teacher = Teacher.find.ref(Long.valueOf(teacherID));
		if (filledForm.hasErrors()) {
			return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Error while processing."));
		} else {
			String name = filledForm.data().get("name");
			if (!name.equals(teacher.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Name was too long."));
				teacher.name = name;
				teacher.save();
			}

			String email = filledForm.data().get("email").toLowerCase();
			if (!email.equals(teacher.email)) {
				if (email.equals("") || email.trim().isEmpty() || !email.contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Invalid email address.."));
				if (email.length() >= 250) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Email was too long."));
				if (Parent.exists(email) || Student.exists(email) || Teacher.exists(email)) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "That email is already associated with an account."));
				teacher.email = email;
				teacher.save();
			}

			try {
				String currentPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("currentPassword"), teacher.salt);
				String newPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("newPassword"), teacher.salt);
				String newPasswordAgain = HASHER.hashWithSaltSHA256(filledForm.data().get("newPasswordAgain"), teacher.salt);
				if (!currentPassword.equals("") && !newPassword.equals("") && !newPasswordAgain.equals("")) {
					if (currentPassword.equals("") || !currentPassword.equals(teacher.password)) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Current password was incorrect."));
					if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Invalid new password."));
					if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Invalid passwords."));
					if (!newPassword.equals(newPasswordAgain)) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "New passwords did not match."));
					teacher.password = newPassword;
					teacher.save();
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "accountSettings", "Error while processing."));
			}

			return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "overview", "Account changed successfully."));
		}
	}

	// Direct to the edit assignment page
	public static Result editPageAssignment(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(views.html.assignmentEdit.render(student, assignment, createSchoolClassesList(student), ""));
	}

	// Direct a teacher to the edit assignment page
	public static Result editPageAssignmentForTeacher(String assignmentID, String teacherID) {
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Teacher teacher = Teacher.find.ref(Long.valueOf(teacherID));
		return ok(views.html.assignmentEditForTeacher.render(teacher, assignment, createSchoolClassListForTeacher(teacher), ""));
	}

	// Direct to the edit school class page
	public static Result schoolClassEditPage(String schoolClassID, String studentID) {
		SchoolClass schoolClass = SchoolClass.find.ref(Long.valueOf(schoolClassID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(views.html.schoolClassEdit.render(schoolClass, student, ""));
	}

	// Direct to the edit school class page for teacher
	public static Result schoolClassEditPageForTeacher(String schoolClassID, String teacherID) {
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", Long.valueOf(teacherID)).findUnique();
		return ok(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, ""));
	}

	// Create a new student from the request
	public static Result newStudent() {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(studentSignUp.render(studentForm, "Form had errors."));
		} else {
			String email = filledForm.data().get("email").toLowerCase();
			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			if (!email.contains("@") || !email.contains(".") || email.trim().isEmpty() || email.equals("")) return badRequest(studentSignUp.render(studentForm, "Invalid email address."));
			if (Parent.exists(email) || Teacher.exists(email) || Student.exists(email)) return badRequest(studentSignUp.render(studentForm, "That email is already associated with an account."));
			if (email.length() >= 250) return badRequest(studentSignUp.render(studentForm, "Email was too long."));
			if (name.length() >= 250) return badRequest(studentSignUp.render(studentForm, "Name was too long."));
			if (name.trim().isEmpty()) return badRequest(studentSignUp.render(studentForm, "Invalid name."));
			if (password.trim().isEmpty()) return badRequest(studentSignUp.render(studentForm, "Invalid password."));
			String[] hashed = null;
			try {
				hashed = HASHER.hashSHA256(password);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Student student = Student.create(name, email, hashed[0], hashed[1], filledForm.data().get("grade"));
			if (student == null) {
				return badRequest(studentSignUp.render(studentForm, "That email is already associated with an account."));
			}
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
	}

	// Create a new parent from the request
	public static Result newParent() {
		Form<Parent> filledForm = parentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(parentSignUp.render(parentForm, "Form had errors."));
		} else {
			String email = filledForm.data().get("email").toLowerCase();
			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			if (!email.contains("@") || !email.contains(".") || email.trim().isEmpty() || email.equals("")) return badRequest(parentSignUp.render(parentForm, "Invalid email address."));
			if (Student.exists(email) || Teacher.exists(email) || Parent.exists(email)) return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));
			if (email.length() >= 250) return badRequest(parentSignUp.render(parentForm, "Email was too long."));
			if (name.length() >= 250) return badRequest(parentSignUp.render(parentForm, "Name was too long."));
			if (name.trim().isEmpty()) return badRequest(parentSignUp.render(parentForm, "Invalid name."));
			if (password.trim().isEmpty()) return badRequest(parentSignUp.render(parentForm, "Invalid password."));
			String[] hashed = null;
			try {
				hashed = HASHER.hashSHA256(password);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Parent parent = Parent.create(name, email, hashed[0], hashed[1]);
			if (parent == null) return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
		}
	}

	// Create a new Teacher account from the request
	public static Result newTeacher() {
		Form<Teacher> filledForm = teacherForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.teacherSignUp.render(teacherForm, "Form had errors."));
		} else {
			String email = filledForm.data().get("email").toLowerCase();
			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			if (!email.contains("@") || !email.contains(".") || email.trim().isEmpty() || email.equals("")) return badRequest(views.html.teacherSignUp.render(teacherForm, "Invalid email address."));
			if (Student.exists(email) || Parent.exists(email) || Teacher.exists(email)) return badRequest(views.html.teacherSignUp.render(teacherForm, "That email is already associated with an account."));
			if (email.length() >= 250) return badRequest(views.html.teacherSignUp.render(teacherForm, "Email was too long."));
			if (name.length() >= 250) return badRequest(views.html.teacherSignUp.render(teacherForm, "Name was too long."));
			if (name.trim().isEmpty()) return badRequest(views.html.teacherSignUp.render(teacherForm, "Invalid name."));
			if (password.trim().isEmpty()) return badRequest(views.html.teacherSignUp.render(teacherForm, "Invalid password."));
			String[] hashed = null;
			try {
				hashed = HASHER.hashSHA256(password);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Teacher teacher = Teacher.create(name, email, hashed[0], hashed[1]);
			if (teacher == null) return badRequest(views.html.teacherSignUp.render(teacherForm, "That email is already associated with an account."));
			return ok(views.html.teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "", ""));
		}
	}

	// Creates a new school class for the teacher from the request
	public static Result newSchoolClassForTeacher(String teacherID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Teacher teacher = Teacher.find.ref(Long.valueOf(teacherID));
		if (teacher == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(views.html.teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "schoolClasses", "Error while processing."));
		List<SchoolClass> schoolClassList = createSchoolClassListForTeacher(teacher);
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty() || subject.equals("")) return badRequest(views.html.teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "schoolClasses", "Class name can't be spaces."));
		if (subject.length() >= 250) return badRequest(views.html.teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "schoolClasses", "Class name was too long."));
		for (SchoolClass schoolClass : schoolClassList) {
			if (subject.equals(schoolClass.subject)) return badRequest(views.html.teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "schoolClasses", "Can't have two classes with the same name."));
		}
		SchoolClass.create(subject, teacher.email, Long.valueOf(teacherID), filledForm.data().get("color"), filledForm.data().get("password"));
		return ok(views.html.teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "schoolClasses", ""));
	}

	// Create a new school class from the request
	public static Result newSchoolClass(String studentID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClassList = createSchoolClassesList(student);
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty() || subject.equals("")) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Class name can not be just spaces."));
		if (subject.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Class name was too long."));
		for (SchoolClass schoolClass : schoolClassList) {
			if (subject.equals(schoolClass.subject)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Can't have two classes with the same name."));
		}
		SchoolClass.create(subject, student.email, Long.valueOf(studentID), filledForm.data().get("color"), "");
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
	}

	// Create a new school class from a teacher provided id and password
	public static Result newSchoolClassFromTeacher(String studentID) {
		Form<SchoolClassFromCode> filledForm = schoolClassFromCodeForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Error while processing."));

		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassID"))).findUnique();
		if (schoolClass == null) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Invalid class ID."));
		if (!schoolClass.password.equals(filledForm.data().get("password"))) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Password was incorrect."));
		List<SchoolClass> schoolClasses = createSchoolClassesList(student);
		for (SchoolClass schoolClassIterated : schoolClasses) {
			if (schoolClassIterated.subject.equals(schoolClass.subject)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Can't have two classes with the same name."));
		}
		schoolClass.students.add(Student.find.ref(Long.valueOf(studentID)));
		schoolClass.save();

		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
	}

	// Add a child to a parent
	public static Result addChild(String parentID) {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			Parent parent = Parent.find.ref(Long.valueOf(parentID));
			return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Error while processing."));
		} else {
			Parent parent = Parent.find.ref(Long.valueOf(parentID));

			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			String grade = filledForm.data().get("grade");

			if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Invalid grade level."));

			if (name.equals("") || name.trim().isEmpty()) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Invalid name."));
			if (name.length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Name was too long."));

			if (password.equals("") || password.trim().isEmpty() || password.equals(parent.password)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Invalid password"));

			try {
				password = HASHER.hashWithSaltSHA256(password, parent.salt);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			Student newStudent = Student.create(name, filledForm.data().get("email").toLowerCase(), parent.salt, password, grade);
			if (newStudent == null) {
				return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Can't have the same password as another one of your kids."));
			}
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "overview", ""));
		}
	}

	// TODO need to do these
	public static Result addNote(String studentID) {
		return TODO;
	}

	// Create a new assignment from the request
	public static Result newAssignment(String studentID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "addAssignment", "Error while processing."));
		} else {
			String description = filledForm.data().get("description");
			if (description.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "addAssignment", "Description was too long."));
			Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), description, studentID);
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
	}

	public static Result newAssignmentForTeacher(String teacherID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Teacher teacher = Teacher.find.ref(Long.valueOf(teacherID));
		if (filledForm.hasErrors()) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "addAssignment", "Error while processing."));
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassId"))).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "addAssignment", "Error while processing."));
		String description = filledForm.data().get("description");
		if (description.length() >= 250) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "addAssignment", "Description was too long."));
		Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), description, teacherID);
		return ok(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "overview", ""));

	}

	// Edit an assignment from a request
	// TODO make errors take them back to edit page
	public static Result editAssignment(String assignmentID, String studentID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", "Error while processing."));
		} else {
			String description = filledForm.data().get("description");
			if (description.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", "Description was too long."));
			Assignment.edit(Long.parseLong(assignmentID), SchoolClass.find.ref(Long.parseLong(filledForm.data().get("schoolClassId"))), filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
	}

	// Edit an assignment from a teacher request
	// TODO make errors take them back to edit page
	public static Result editAssignmentForTeacher(String assignmentID, String teacherID) {
		Form<Assignment> filledform = assignmentForm.bindFromRequest();
		Teacher teacher = Teacher.find.ref(Long.valueOf(teacherID));
		if (filledform.hasErrors()) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "", "Error while trying to change assignment."));
		String description = filledform.data().get("description");
		if (description.length() >= 250) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "addAssignment", "Description was too long."));
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		List<Student> students = assignment.schoolClass.students;
		for (int i = 0; i < students.size(); i++) {
			List<Assignment> assignments = createAssignmentsList(students.get(i));
			for (int j = 0; j < assignments.size(); j++) {
				if (assignments.get(j).schoolClass.id == assignment.schoolClass.id) {
					if (Assignment.same(assignments.get(j), assignment)) Assignment.edit(assignments.get(j).id, SchoolClass.find.ref(Long.parseLong(filledform.data().get("schoolClassId"))), filledform.data().get("dueDate"), filledform.data().get("kindOfAssignment"), filledform.data().get("description"));
				}
			}
		}
		Assignment.edit(Long.parseLong(assignmentID), SchoolClass.find.ref(Long.parseLong(filledform.data().get("schoolClassId"))), filledform.data().get("dueDate"), filledform.data().get("kindOfAssignment"), filledform.data().get("description"));
		return ok(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "overview", ""));
	}

	// Edit a schoolClass from a request
	public static Result editSchoolClass(String schoolClassID, String studentID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		SchoolClass schoolClass = SchoolClass.find.ref(Long.valueOf(schoolClassID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) {
			return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Error while processing."));
		} else {
			String subject = filledForm.data().get("subject");
			if (subject.trim().isEmpty() || subject.equals("")) return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Subject can not be empty."));
			if (subject.length() >= 250) return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Subject was too long."));
			List<SchoolClass> schoolClassList = createSchoolClassesList(student);
			for (SchoolClass schoolClass2 : schoolClassList) {
				if (schoolClass.id == schoolClass2.id) continue;
				if (subject.equals(schoolClass2.subject)) return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Can't have two classes with the same name."));
			}
			String color = filledForm.data().get("color");
			SchoolClass.edit(Long.valueOf(schoolClassID), subject, color, student.id, "");
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
		}
	}

	// Edit a schoolClass for a teacher from a request
	public static Result editSchoolClassForTeacher(String schoolClassID, String teacherID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", Long.valueOf(teacherID)).findUnique();
		if (teacher == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Error while processing."));
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty() || subject.equals("")) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Subject can not be empty."));
		if (subject.length() >= 250) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Subject was too long."));
		List<SchoolClass> schoolClassList = createSchoolClassListForTeacher(teacher);
		for (SchoolClass schoolClass2 : schoolClassList) {
			if (schoolClass.id == schoolClass2.id) continue;
			if (subject.equals(schoolClass2.subject)) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Can't have two classes with the same name."));
		}
		String color = filledForm.data().get("color");
		SchoolClass.edit(Long.valueOf(schoolClassID), subject, color, teacher.id, "");
		return ok(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "schoolClasses", ""));
	}

	// Delete an assignment
	public static Result deleteAssignment(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (assignment == null) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", "Error while processing."));
		try {
			assignment.delete();
		} catch (PersistenceException e) {
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
	}

	// Delete an assignment for teacher
	public static Result deleteAssignmentForTeacher(String assignmentID, String teacherID) {
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", Long.valueOf(teacherID)).findUnique();
		if (assignment == null) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "overview", "Error while processing."));
		List<Student> students = assignment.schoolClass.students;
		for (int i = 0; i < students.size(); i++) {
			List<Assignment> assignments = createAssignmentsList(students.get(i));
			for (int j = 0; j < assignments.size(); j++) {
				Assignment studentAssignment = assignments.get(j);
				if (studentAssignment.dueDate.equals(assignment.dueDate) && studentAssignment.kindOfAssignment.equals(assignment.kindOfAssignment) && studentAssignment.description.equals(assignment.description)) {
					studentAssignment.delete();
					break;
				}
			}
		}
		try {
			assignment.delete();
		} catch (PersistenceException e) {
			return ok(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "overview", ""));
		}
		return ok(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "overview", ""));
	}

	// Delete a late assignment
	public static Result deleteLateAssignment(String assignmentID, String studentID) {
		Assignment lateAssignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (lateAssignment == null) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "lateAssignments", "Error while processing."));
		try {
			lateAssignment.delete();
		} catch (PersistenceException e) {
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "lateAssignments", ""));
		}
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "lateAssignments", ""));
	}

	// Delete a finished assignment
	public static Result deleteFinishedAssignment(String assignmentID, String studentID) {
		Assignment finishedAssignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (finishedAssignment == null) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "finishedAssignments", "Error while processing."));
		try {
			finishedAssignment.delete();
		} catch (PersistenceException e) {
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "finishedAssignments", ""));
		}
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "finishedAssignments", ""));
	}

	// Set the finished value of an assignment to true
	public static Result finishedAssignment(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (assignment == null) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", "Error while processing."));
		assignment.finished = true;
		try {
			assignment.save();
		} catch (PersistenceException e) {
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "finishedAssignments", ""));
		}
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "finishedAssignments", ""));
	}

	public static Result deleteSchoolClass(String schoolClassID, String studentID) {
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Student student = Student.find.where().eq("ID", Long.valueOf(studentID)).findUnique();
		if (schoolClass == null) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Error while processing."));
		if (schoolClass.teacherID != null) {
			schoolClass.students.remove(student);
			schoolClass.save();
			List<Assignment> schoolClassAssignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
			List<Assignment> assignments = Assignment.find.where().eq("FOREIGN_ID", Long.valueOf(studentID)).findList();
			for (int j = schoolClassAssignments.size() - 1; j >= 0; j--) {
				for (int i = assignments.size() - 1; i >= 0; i--) {
					if (Assignment.same(schoolClassAssignments.get(j), assignments.get(i))) {
						assignments.get(i).delete();
						assignments.remove(i);
					}
				}
			}
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
		} else {
			List<Assignment> assignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
			for (int i = assignments.size() - 1; i >= 0; i--) {
				assignments.get(i).delete();
				assignments.remove(i);
			}
			try {
				schoolClass.delete();
			} catch (PersistenceException e) {
				return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
			}
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
		}
	}

	public static Result deleteSchoolClassForTeacher(String schoolClassID, String teacherID) {
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", Long.valueOf(teacherID)).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "schoolClasses", "Error while processing."));
		List<Assignment> assignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
		for (int j = assignments.size() - 1; j >= 0; j--) {
			assignments.get(j).delete();
			assignments.remove(j);
		}
		try {
			schoolClass.delete();
		} catch (PersistenceException e) {
			return ok(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "", ""));
		}
		return ok(teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "", ""));
	}

	// Sets the variable today, the number of days in the date since 0AD
	public static void setToday() {
		int day = Calendar.getInstance().get(Calendar.DATE);
		// have to add one because 0 is January
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int total = (year * 366) - ((12 - month) * 31) - (31 - day);
		today = total;
	}

	// Direct to the student profile page after authentication
	public static Result profileLogin(String studentID) {
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
	}

	// Direct to the parent profile page after authentication
	public static Result parentProfileLogin(String parentID) {
		Parent parent = Parent.find.ref(Long.valueOf(parentID));
		return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
	}

	// Direct to the teacher profile page after authentication
	public static Result teacherProfileLogin(Long teacherID) {
		Teacher teacher = Teacher.find.ref(teacherID);
		return ok(views.html.teacherProfile.render(teacher, createAssignmentsListForTeacher(teacher), createSchoolClassListForTeacher(teacher), today, "", ""));
	}

	// Direct the request to the student with the ID, parent accounts use this
	public static Result redirectToStudent(Long studentID, String parentID) {
		Student student = Student.find.ref(Long.valueOf(studentID));
		Parent parent = Parent.find.ref(Long.valueOf(parentID));
		List<Student> children = createChildrenList(parent);
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).email.equals(student.email)) {
				return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
			}
		}
		return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "", ""));
	}

	// Authenticate a request
	public static Result authenticate() {
		Form<Login> filledForm = loginForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(login.render(loginForm, "Login error, try again."));
		} else {
			// if (Parent.authenticate(filledForm.data().get("email").toLowerCase(), filledForm.data().get("password")) == null && Student.authenticate(filledForm.data().get("email").toLowerCase(), filledForm.data().get("password")) == null) {
			// return badRequest(login.render(loginForm, "Invalid email or password."));
			// }

			String email = filledForm.data().get("email").toLowerCase();
			Parent parent = Parent.find.where().eq("email", email).findUnique();
			Teacher teacher = Teacher.find.where().eq("email", email).findUnique();
			List<Student> students = Student.find.where().eq("email", email).findList();

			// Try parent login first
			if (parent != null) {
				String password = null;
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (Parent.authenticate(filledForm.data().get("email"), password) != null) {
					parent = Parent.find.where().eq("email", email).eq("password", password).findUnique();
					return redirect(routes.Application.parentProfileLogin(String.valueOf(parent.id)));
				}
			}

			// Else try teacher login
			if (teacher != null) {
				String password = null;
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), teacher.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (Teacher.authenticate(filledForm.data().get("email"), password) != null) {
					teacher = Teacher.find.where().eq("email", email).eq("password", password).findUnique();
					return redirect(routes.Application.teacherProfileLogin(teacher.id));
				}
			}

			// Else do student login
			String password = null;
			Student student = null;
			if (students.size() <= 0) return badRequest(login.render(loginForm, "Invalid email or password."));
			if (students.size() > 1 && Parent.exists(email)) {
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), students.get(0).parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				student = Student.find.where().eq("email", email).eq("password", password).findUnique();
			} else {
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), students.get(0).salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				student = Student.find.where().eq("email", email).eq("password", password).findUnique();

			}
			if (password == null || student == null) return badRequest(login.render(loginForm, "Invalid email or password."));

			if (Student.authenticate(email, password) != null) return redirect(routes.Application.profileLogin(String.valueOf(student.id)));

			return badRequest(login.render(loginForm, "Invalid email or password."));
		}
	}

	// For All of the create_Lists Methods:
	// Recall that SQL uses underscores, you can't use student.id it becomes
	// student_id

	// Creates the school classes list for the given student
	// TODO FIND BETTER WAY TO DO THIS
	public static List<SchoolClass> createSchoolClassesList(Student student) {
		List<SchoolClass> schoolClasses = SchoolClass.find.all();
		List<SchoolClass> returnSchoolClasses = new ArrayList<SchoolClass>();

		for (int i = 0; i < schoolClasses.size(); i++) {
			if (schoolClasses.get(i).students == null || schoolClasses.get(i).students.size() <= 0) continue;
			for (int j = 0; j < schoolClasses.get(i).students.size(); j++) {
				if (schoolClasses.get(i).students.get(j).id == student.id) {
					returnSchoolClasses.add(schoolClasses.get(i));
					break;
				}
			}
		}

		return returnSchoolClasses;
	}

	// Creates the assignments list for the student
	// TODO FIND A BETTER WAY TO DO THIS
	// TODO TRIPLE NESTED FOR LOOP IT SUCH A BAAADDDD IDEA
	public static List<Assignment> createAssignmentsList(Student student) {
		setToday();

		List<Assignment> assignments = new ArrayList<Assignment>();
		// List<Assignment> assignments = Assignment.find.where().eq("FOREIGN_ID", student.id).findList();
		List<SchoolClass> schoolClasses = createSchoolClassesList(student);
		for (int i = 0; i < schoolClasses.size(); i++) {
			List<Assignment> schoolClassAssignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClasses.get(i).id).findList();
			if (schoolClasses.get(i).teacherID != null) {
				// SchoolClass from a teacher
				// for(int j = schoolClassAssignments.size() - 1; j >= 0; j--) {
				// for(int k = schoolClassAssignments.size() - 1; k >= 0; k--) {
				// if(j == k) continue;
				// if(Assignment.same(schoolClassAssignments.get(j), schoolClassAssignments.get(k))) {
				// schoolClassAssignments.remove(j);
				// }
				// }
				// }
				assignments.addAll(schoolClassAssignments);
			} else {
				// Self-added SchoolClass
				assignments.addAll(schoolClassAssignments);
			}
		}
		// for (int i = 0; i < schoolClasses.size(); i++) {
		//
		// List<Assignment> schoolClassAssignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClasses.get(i).id).findList();
		//
		// if (schoolClasses.get(i).teacherID != null) {
		// for (int k = schoolClassAssignments.size() - 1; k >= 0; k--) {
		// for (int j = assignments.size() - 1; j >= 0; j--) {
		// if (Assignment.same(assignments.get(j), schoolClassAssignments.get(k))) {
		// schoolClassAssignments.remove(k);
		// }
		// }
		// }
		// }
		// for (int j = 0; j < schoolClassAssignments.size(); j++) {
		// assignments.add(Assignment.create(schoolClassAssignments.get(j), student.id));
		// }
		// }
		//
		// for (int i = assignments.size() - 1; i >= 0; i--) {
		// if (assignments.get(i).finished) assignments.remove(i);
		// }

		return sortList(assignments);
	}

	// Creates the finished assignments list for the student
	public static List<Assignment> createFinishedAssignmentsList(Student student) {
		setToday();
		List<Assignment> finishedAssignments = Assignment.find.where().eq("FOREIGN_ID", student.id).eq("FINISHED", true).findList();
		return sortList(finishedAssignments);
	}

	// Creates the late assignments list for the given student
	public static List<Assignment> createLateAssignmentsList(Student student) {
		setToday();
		List<Assignment> lateAssignments = Assignment.find.where().eq("FOREIGN_ID", student.id).eq("FINISHED", false).findList();

		for (int i = 0; i < lateAssignments.size(); i++) {
			if (lateAssignments.get(i).total >= today) {
				lateAssignments.remove(i);
				i--;
			}
		}
		return sortList(lateAssignments);
	}

	// TODO FIND A BETTER WAY TO DO THIS CREATION
	public static List<Teacher> createTeachersList(Student student) {
		List<Teacher> teachers = new ArrayList<Teacher>();
		// teachers = Teacher.find.all();
		// // teachers = Teacher.find.where().eq("student.id",
		// // student.id).findList();
		// for (int i = teachers.size() - 1; i >= 0; i--) {
		// if (!student.teacher.email.equals(teachers.get(i).email)) teachers.remove(i);
		// }
		return teachers;
	}

	// Create the notes list for the given student
	public static List<Note> createNotesList(Student student) {
		List<Note> notes = Note.find.where().eq("FOREIGN_ID", student.id).findList();
		return notes;
	}

	// Create the children list for the given parent
	public static List<Student> createChildrenList(Parent parent) {
		List<Student> children = Student.find.where().eq("email", parent.email).findList();
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).parent == null) continue;
		}
		return children;
	}

	// Create the assignment list for the given parent
	public static List<Assignment> createAssignmentsListForParent(Parent parent) {
		List<Assignment> assignments = new ArrayList<Assignment>();
		List<Student> children = createChildrenList(parent);
		for (int i = 0; i < children.size(); i++) {
			assignments.addAll(createAssignmentsList(children.get(i)));
		}
		return assignments;
	}

	// Create the assignment list for the given teacher
	public static List<Assignment> createAssignmentsListForTeacher(Teacher teacher) {
		return sortList(Assignment.find.where().eq("FOREIGN_ID", teacher.id).findList());
	}

	// Create the schoolClasses list for the given teacher
	public static List<SchoolClass> createSchoolClassListForTeacher(Teacher teacher) {
		return SchoolClass.find.where().eq("TEACHER_ID", teacher.id).findList();
	}

	// Sort a list from the oldest date to the newest date
	public static List<Assignment> sortList(List<Assignment> assignments) {
		if (assignments.size() <= 1) return assignments;
		List<Assignment> returnAssignments = new ArrayList<Assignment>();
		returnAssignments.add(assignments.remove(0));

		for (int i = assignments.size() - 1; i >= 0; i--) {
			for (int j = returnAssignments.size() - 1; j >= 0; j--) {
				if (assignments.get(i).total >= returnAssignments.get(j).total) {
					if (j + 1 >= returnAssignments.size()) {
						returnAssignments.add(assignments.remove(i));
						break;
					}
					returnAssignments.add(j + 1, assignments.remove(i));
					break;
				}
				if (j == 0) returnAssignments.add(0, assignments.remove(i));
			}
		}
		return returnAssignments;
	}

	// Login class that the loginForm forms to for logging in
	public static class Login {
		public String email;
		public String password;

		public String validate() {
			// if (Student.authenticate(email, password) == null && Parent.authenticate(email, password) == null) {
			// return "Invalid email or password";
			// }
			return null;
		}
	}

	// Account settings class that the accountSettingsForm forms to for changing
	// account settings
	public static class AccountSettings {
		public String email;
		public String currentPassword;
		public String newPassword;
		public String newPasswordAgain;
		public String name;
		public String grade;
	}

	// Contact us class that the contactUsForm forms to for sending an email
	public static class ContactUs {
		public String name;
		public String email;
		public String subject;
		public String message;
	}

	// SchoolClassFromCode class that the schoolClassFromCodeForm forms to for adding a class with a teacher provided id and password
	public static class SchoolClassFromCode {
		public String id;
		public String password;
	}

}
