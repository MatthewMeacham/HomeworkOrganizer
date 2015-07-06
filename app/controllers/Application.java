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
	@SuppressWarnings("unused")
	private static Form<Teacher> teacherForm = Form.form(Teacher.class);
	private static Form<Assignment> assignmentForm = Form.form(Assignment.class);
	private static Form<Parent> parentForm = Form.form(Parent.class);
	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);
	private static Form<Login> loginForm = Form.form(Login.class);
	private static Form<ContactUs> contactUsForm = Form.form(ContactUs.class);

	private static final Hasher HASHER = new Hasher();

	// The number of days in today, this is set to year * 366 - (12 - month) *
	// 31 - (31 - day)
	private static int today;

	// Directs the request to the index
	public static Result index() {
		return ok(index.render(Student.find.all().size() + Parent.find.all().size(), loginForm));
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
				if (Parent.exists(email)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Email is already associated with an account."));
				student.email = email;
				student.save();
			}

			String currentPassword = filledForm.data().get("currentPassword");
			String newPassword = filledForm.data().get("newPassword");
			String newPasswordAgain = filledForm.data().get("newPasswordAgain");
			if (!currentPassword.equals("") || !newPassword.equals("") || !newPasswordAgain.equals("")) {
				if (currentPassword.equals("") || !currentPassword.equals(student.password) || currentPassword.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Current password was incorrect."));
				if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid new password."));
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid passwords."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "New passwords did not match."));
				if (newPassword.length() >= 250 || newPasswordAgain.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "New password was too long."));
				student.password = newPassword;
				student.save();
			}
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", "Account changed successfully."));
		}
	}

	// Changes account settings for a parent account OR account settings for one
	// of their kids and return
	// either A) successful or B) Error
	public static Result changeParentAccountSettings(String parentID, String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Parent parent = Parent.find.ref(parentID);
		List<Student> children = createChildrenList(parent);
		if (filledForm.hasErrors()) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Error while processing."));

		String name = filledForm.data().get("name");
		String currentPassword = filledForm.data().get("currentPassword");
		String newPassword = filledForm.data().get("newPassword");
		String newPasswordAgain = filledForm.data().get("newPasswordAgain");
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
				if (Parent.exists(email)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "That email is already associated with an account."));

				for (Student child : children) {
					child.email = email;
					child.save();
				}
				parent.email = email;
				parent.save();
			}

			if (!currentPassword.equals("") || !newPassword.equals("") || !newPasswordAgain.equals("")) {
				if (currentPassword.equals("") || !currentPassword.equals(parent.password) || currentPassword.length() >= 250) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Current password was incorrect."));
				if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Invalid new password."));
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "Invalid passwords."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "New passwords did not match."));
				if (newPassword.length() >= 250 || newPasswordAgain.length() >= 250) return badRequest(parentProfile.render(parent, children, createAssignmentsListForParent(parent), today, "accountSettings", "New password was too long."));
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

			if (!currentPassword.equals("") || !newPassword.equals("") || !newPasswordAgain.equals("")) {
				if (currentPassword.equals("") || !currentPassword.equals(student.password) || currentPassword.length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Current password was incorrect."));
				if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid new password."));
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid password."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "New passwords did not match."));
				if (newPassword.length() >= 250 || newPasswordAgain.length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "New password was too long."));
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

	// Direct to the edit assignment page
	public static Result editPageAssignment(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(views.html.assignmentEdit.render(student, assignment, createSchoolClassesList(student), ""));
	}

	// Direct to the edit school class page
	public static Result schoolClassEditPage(String schoolClassID, String studentID) {
		SchoolClass schoolClass = SchoolClass.find.ref(Long.valueOf(schoolClassID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(views.html.schoolClassEdit.render(schoolClass, student, ""));
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
			if (Parent.exists(email) || Teacher.exists(email)) return badRequest(studentSignUp.render(studentForm, "That email is already associated with an account."));
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
			if (Student.exists(email) || Teacher.exists(email)) return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));
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
			if (parent == null) {
				return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));
			}
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
		}
	}

	// Create a new school class from the request
	public static Result newSchoolClass(String studentID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size(), loginForm));
		List<SchoolClass> schoolClassList = createSchoolClassesList(student);
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Error while processing."));
		} else {
			String subject = filledForm.data().get("subject");
			if (subject.trim().isEmpty() || subject.equals("")) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Class name can not be just spaces."));
			if (subject.length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Class name was too long."));
			for (SchoolClass schoolClass : schoolClassList) {
				if (subject.equals(schoolClass.subject)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Can't have two classes with the same name."));
			}
			SchoolClass.create(subject, Long.valueOf(filledForm.data().get("studentId")), filledForm.data().get("color"));
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
		}
	}

	// Add a child to a parent
	public static Result addChild() {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			Parent parent = Parent.find.ref(filledForm.data().get("email"));
			return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Error while processing."));
		} else {
			Parent parent = Parent.find.ref(filledForm.data().get("email"));

			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			String grade = filledForm.data().get("grade");

			if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Invalid grade level."));

			if (name.equals("") || name.trim().isEmpty()) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Invalid name."));
			if (name.length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Name was too long."));

			if (password.equals("") || password.trim().isEmpty() || password.equals(parent.password)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "addChild", "Invalid password"));

			try {
				password = HASHER.hashSHA256WithSalt(password, parent.salt);
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

	public static Result newTeacher() {
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
			Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), description);
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
	}

	// Edit an assignment from a request
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

	// Edit a schoolClass from a request
	public static Result editSchoolClass(String schoolClassID, String studentID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		SchoolClass schoolClass = SchoolClass.find.ref(Long.valueOf(schoolClassID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size(), loginForm));
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
			SchoolClass.edit(Long.valueOf(schoolClassID), subject, color, student);
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", ""));
		}
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
		SchoolClass schoolClass = SchoolClass.find.ref(Long.valueOf(schoolClassID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (schoolClass == null) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Error while processing."));
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
	public static Result parentProfileLogin(String parentEmail) {
		Parent parent = Parent.find.ref(parentEmail);
		return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
	}

	// Direct the request to the student with the ID, parent accounts use this
	public static Result redirectToStudent(Long studentID, String parentID) {
		Student student = Student.find.ref(Long.valueOf(studentID));
		Parent parent = Parent.find.ref(parentID);
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
			List<Student> students = Student.find.where().eq("email", email).findList();

			// Try parent login first
			if (parent != null) {
				String password = null;
				try {
					password = HASHER.hashSHA256WithSalt(filledForm.data().get("password"), parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (Parent.authenticate(filledForm.data().get("email"), password) != null) {
					parent = Parent.find.where().eq("email", filledForm.get().email.toLowerCase()).eq("password", password).findUnique();
					return redirect(routes.Application.parentProfileLogin(parent.email));
				}
			}

			// Else do student login
			String password = null;
			Student student = null;
			if (students.size() > 1) {
				try {
					password = HASHER.hashSHA256WithSalt(filledForm.data().get("password"), students.get(0).parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				student = Student.find.where().eq("email", email).eq("password", password).findUnique();
			} else {
				try {
					password = HASHER.hashSHA256WithSalt(filledForm.data().get("password"), students.get(0).salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				student = Student.find.where().eq("email", email).eq("password", password).findUnique();

			}
			if (password == null || student == null) return badRequest(login.render(loginForm, "Invalid email or password."));

			if (Student.authenticate(email, password) != null) {
				return redirect(routes.Application.profileLogin(String.valueOf(student.id)));
			}
			//
			// // Try parent login first
			// if (Parent.authenticate(filledForm.data().get("email"), filledForm.data().get("password")) != null) {
			// Parent parent = Parent.find.where().eq("email", filledForm.get().email.toLowerCase()).eq("password", filledForm.get().password).findUnique();
			// return redirect(routes.Application.parentProfileLogin(parent.email));
			// }
			//
			// // Else do student login
			// Student student = Student.find.where().eq("email", filledForm.data().get("email").toLowerCase()).findUnique();
			// String password = null;
			// try {
			// password = HASHER.hashSHA256WithSalt(filledForm.data().get("password"), student.salt);
			// } catch (NoSuchAlgorithmException e) {
			// e.printStackTrace();
			// } catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
			// }
			// if(Student.authenticate(filledForm.data().get("email").toLowerCase(), password) != null) {
			// //Student student = Student.find.where().eq("email", filledForm.get().email.toLowerCase()).eq("password", filledForm.get().password).findUnique();
			// return redirect(routes.Application.profileLogin(String.valueOf(student.id)));
			// }
			return badRequest(login.render(loginForm, "Invalid email or password."));
		}
	}

	// For All of the create_Lists Methods:
	// Recall that SQL uses underscores, you can't use student.id it becomes
	// student_id

	// Creates the school classes list for the given student
	public static List<SchoolClass> createSchoolClassesList(Student student) {
		return SchoolClass.find.where().eq("STUDENT_ID", student.id).findList();
	}

	// Creates the assignments list for the student
	public static List<Assignment> createAssignmentsList(Student student) {
		setToday();
		List<Assignment> assignments = Assignment.find.where().eq("STUDENT_ID", student.id).eq("FINISHED", false).findList();
		// This code here will remove late assignments from the list
		// However, we think late assignments should be in the overview and so
		// this block has been commented out

		// for (int i = 0; i < assignments.size(); i++) {
		// if (assignments.get(i).total < today) {
		// assignments.remove(i);
		// i--;
		// }
		// }
		return sortList(assignments);
	}

	// Creates the finished assignments list for the student
	public static List<Assignment> createFinishedAssignmentsList(Student student) {
		setToday();
		List<Assignment> finishedAssignments = Assignment.find.where().eq("STUDENT_ID", student.id).eq("FINISHED", true).findList();
		return sortList(finishedAssignments);
	}

	// Creates the late assignments list for the given student
	public static List<Assignment> createLateAssignmentsList(Student student) {
		setToday();
		List<Assignment> lateAssignments = Assignment.find.where().eq("STUDENT_ID", student.id).eq("FINISHED", false).findList();

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
		teachers = Teacher.find.all();
		// teachers = Teacher.find.where().eq("student.id",
		// student.id).findList();
		for (int i = teachers.size() - 1; i >= 0; i--) {
			if (!student.teacher.email.equals(teachers.get(i).email)) teachers.remove(i);
		}
		return teachers;
	}

	// Create the notes list for the given student
	public static List<Note> createNotesList(Student student) {
		List<Note> notes = Note.find.where().eq("STUDENT_ID", student.id).findList();
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

}
