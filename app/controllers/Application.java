package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Assignment;
import models.Note;
import models.Parent;
import models.SchoolClass;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.assignmentEdit;
import views.html.blog;
import views.html.blogIE;
import views.html.index;
import views.html.indexIE;
import views.html.login;
import views.html.parentProfile;
import views.html.parentSignUp;
import views.html.profile;
import views.html.signup;
import views.html.studentSignUp;

public class Application extends Controller {

	private static Form<Student> studentForm = Form.form(Student.class);
	private static Form<SchoolClass> schoolClassForm = Form.form(SchoolClass.class);
	@SuppressWarnings("unused")
	private static Form<Note> noteForm = Form.form(Note.class);
	@SuppressWarnings("unused")
	private static Form<Teacher> teacherForm = Form.form(Teacher.class);
	private static Form<Assignment> assignmentForm = Form.form(Assignment.class);
	private static Form<Parent> parentForm = Form.form(Parent.class);
	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);

	private static int today;
	public static String[] options = { "Homework", "Project", "Test", "Final", "Presentation", "Other" };

	public static Result index() {
		return ok(index.render(Student.find.all().size() + Parent.find.all().size(), Form.form(Login.class)));
	}

	public static Result indexForIE() {
		return ok(indexIE.render(Student.find.all().size() + Parent.find.all().size(), Form.form(Login.class)));
	}

	public static Result blog() {
		return ok(blog.render(Form.form(Login.class)));
	}

	public static Result blogForIE() {
		return ok(blogIE.render(Form.form(Login.class)));
	}

	public static Result signup() {
		return ok(signup.render(""));
	}

	public static Result studentSignup() {
		return ok(studentSignUp.render(studentForm, ""));
	}

	public static Result parentSignup() {
		return ok(parentSignUp.render(parentForm, ""));
	}

	public static Result login() {
		setToday();
		return ok(login.render(Form.form(Login.class), ""));
	}

	public static Result logout() {
		return redirect(routes.Application.index());
	}

	public static Result students() {
		return TODO;
		// return ok(students.render(Student.find.all()));
	}

	public static Result FAQ() {
		return ok(views.html.faq.render());
	}

	public static boolean childAccount(String studentID) {
		List<Parent> parents = Parent.find.all();
		Student student = Student.find.ref(Long.valueOf(studentID));
		for (int i = 0; i < parents.size(); i++) {
			if (student.email.equals(parents.get(i).email)) return true;
		}
		return false;
	}

	public static Result changeAccountSettings(String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Error occurred while processing."));
		} else {

			student.grade = filledForm.data().get("grade");
			student.save();

			if (!filledForm.data().get("name").equals(student.name)) {
				if (filledForm.data().get("name").length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Given name was too long."));
				student.name = filledForm.data().get("name");
				student.save();
			}

			if (!filledForm.data().get("email").equals(student.email)) {
				if (filledForm.data().get("email").length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Given email was too long."));
				if (!filledForm.data().get("email").contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Invalid email was given."));
				if (Parent.exists(filledForm.data().get("email"))) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Given email is already associated with an account."));
				student.email = filledForm.data().get("email");
				student.save();
			}

			if (!filledForm.data().get("currentPassword").equals("") || !filledForm.data().get("newPassword").equals("") || !filledForm.data().get("newPasswordAgain").equals("")) {
				if (!filledForm.data().get("currentPassword").equals(student.password)) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Current password was incorrect."));
				if (!filledForm.data().get("newPassword").equals(filledForm.data().get("newPasswordAgain"))) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "New passwords did not match."));
				if (filledForm.data().get("currentPassword").length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Given password was too long."));
				if (filledForm.data().get("newPassword").length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Given new password was too long."));
				if (filledForm.data().get("newPasswordAgain").length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "accountSettings", "Given new password was too long."));
				student.password = filledForm.data().get("newPassword");
				student.save();
			}
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
	}

	public static Result changeParentAccountSettings(String parentID, String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Parent parent = Parent.find.ref(parentID);
		if (filledForm.hasErrors()) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Error while processing."));

		if (studentID.contains("@")) {
			// parent request to change themselves
			
			if(!filledForm.data().get("name").equals(parent.name)) {
				if (filledForm.data().get("name").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given name was too long."));
				parent.name = filledForm.data().get("name");
				parent.save();
			}
			
			if(!filledForm.data().get("email").equals(parent.email)) {
				if (filledForm.data().get("email").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given email was too long."));
				if (!filledForm.data().get("email").contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid email address was given."));
				if (Parent.exists(filledForm.data().get("email"))) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "That email is already associated with an account."));
				parent.email = filledForm.data().get("email");
				parent.save();
			
			}
			
			if (!filledForm.data().get("currentPassword").equals("") || !filledForm.data().get("newPassword").equals("") || !filledForm.data().get("newPasswordAgain").equals("")) {
				if (!filledForm.data().get("currentPassword").equals(parent.password)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Password was incorrect."));
				if (!filledForm.data().get("newPassword").equals(filledForm.data().get("newPasswordAgain"))) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "New passwords did not match."));
				if (filledForm.data().get("currentPassword").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given current password was too long."));
				if (filledForm.data().get("newPassword").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given new password was too long."));
				if (filledForm.data().get("newPasswordAgain").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given new password was too long."));
				parent.password = filledForm.data().get("newPassword");
				parent.save();
			
			}
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "overview", ""));
		} else {
			// parent request to change student
			Student student = Student.find.ref(Long.valueOf(studentID));

			student.grade = filledForm.data().get("grade");
			student.save();

			if (!filledForm.data().get("name").equals(student.name)) {
				if (filledForm.data().get("name").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given name was too long."));
				student.name = filledForm.data().get("name");
				student.save();
			}

			if (!filledForm.data().get("email").equals(student.email)) {
				if (filledForm.data().get("email").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given email was too long."));
				if (!filledForm.data().get("email").contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Invalid email address was given."));
				if (Parent.exists(filledForm.data().get("email"))) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "That email is already associated with an account."));
				student.email = filledForm.data().get("email");
				student.save();
			}

			if (!filledForm.data().get("currentPassword").equals("") || !filledForm.data().get("newPassword").equals("") || !filledForm.data().get("newPasswordAgain").equals("")) {
				if (!filledForm.data().get("currentPassword").equals(student.password)) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Password was incorrect."));
				if (!filledForm.data().get("newPassword").equals(filledForm.data().get("newPasswordAgain"))) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "New passwords did not match."));
				if (filledForm.data().get("currentPassword").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given current password was too long."));
				if (filledForm.data().get("newPassword").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given new password was too long."));
				if (filledForm.data().get("newPasswordAgain").length() >= 250) return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "accountSettings", "Given new password was too long."));
				student.password = filledForm.data().get("newPassword");
				student.save();
			}
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "overview", ""));
		}
	}

	public static Result editPageAssignment(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(assignmentEdit.render(student, assignment, createSchoolClassesList(student)));
	}

	public static Result newStudent() {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(studentSignUp.render(studentForm, "Form had errors."));
		} else {
			if (!filledForm.data().get("email").contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(studentSignUp.render(studentForm, "Invalid email address was given."));
			if (Parent.exists(filledForm.data().get("email"))) return badRequest(studentSignUp.render(studentForm, "That email is already associated with an account."));
			if (filledForm.data().get("email").length() >= 250) return badRequest(studentSignUp.render(studentForm, "Given email was too long."));
			if (filledForm.data().get("name").length() >= 250) return badRequest(studentSignUp.render(studentForm, "Given name was too long."));
			if (filledForm.data().get("password").length() >= 250) return badRequest(studentSignUp.render(studentForm, "Given password was too long."));

			Student student = Student.create(filledForm.data().get("name"), filledForm.data().get("email"), filledForm.data().get("password"), filledForm.data().get("grade"));
			if (student == null) {
				return badRequest(studentSignUp.render(studentForm, "That email is already associated with an account."));
			}
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
	}

	// NEW METHODS
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	public static List<SchoolClass> createSchoolClassesList(Student student) {
		List<SchoolClass> schoolClasses = new ArrayList<SchoolClass>();
		schoolClasses = SchoolClass.find.where().eq("student.id", student.id).findList();
		for (int i = schoolClasses.size() - 1; i >= 0; i--) {
			if (!schoolClasses.get(i).student.id.equals(student.id)) schoolClasses.remove(i);
		}
		return schoolClasses;
	}

	public static List<Assignment> createAssignmentsList(Student student) {
		setToday();
		List<Assignment> assignments = Assignment.find.where().eq("schoolClass.student.id", student.id).findList();
		for (int i = 0; i < assignments.size(); i++) {
			if (assignments.get(i).finished || assignments.get(i).total < today) {
				assignments.remove(i);
				i--;
			}
		}
		return sortList(assignments);
	}

	public static List<Assignment> createFinishedAssignmentsList(Student student) {
		setToday();
		List<Assignment> finishedAssignments = Assignment.find.where().eq("schoolClass.student.id", student.id).findList();
		for (int i = 0; i < finishedAssignments.size(); i++) {
			if (!finishedAssignments.get(i).finished) {
				finishedAssignments.remove(i);
				i--;
			}
		}
		return sortList(finishedAssignments);
	}

	public static List<Assignment> createLateAssignmentsList(Student student) {
		setToday();
		List<Assignment> lateAssignments = Assignment.find.where().eq("schoolClass.student.id", student.id).findList();
		for (int i = 0; i < lateAssignments.size(); i++) {
			if (lateAssignments.get(i).finished || lateAssignments.get(i).total >= today) {
				lateAssignments.remove(i);
				i--;
			}
		}
		return sortList(lateAssignments);
	}

	public static List<Teacher> createTeachersList(Student student) {
		List<Teacher> teachers = new ArrayList<Teacher>();
		teachers = Teacher.find.where().eq("schoolClass.student.id", student.id).findList();
		for (int i = teachers.size() - 1; i >= 0; i--) {
			if (teachers.get(i).schoolClass.student.id != student.id) teachers.remove(i);
		}
		return teachers;
	}

	public static List<Assignment> sortList(List<Assignment> assignments) {
		if (assignments.size() <= 1) return assignments;
		List<Assignment> returnAssignments = new ArrayList<Assignment>();
		returnAssignments.add(assignments.remove(0));
		for (int i = 0; i < assignments.size(); i++) {
			for (int j = 0; j < returnAssignments.size(); j++) {
				if (assignments.get(i).total <= returnAssignments.get(j).total) {
					returnAssignments.add(j, assignments.remove(i));
					i--;
					break;
				}
				if (j == returnAssignments.size() - 1) returnAssignments.add(assignments.remove(i));
			}
		}
		return returnAssignments;
	}

	public static List<Note> createNotesList(Student student) {
		List<Note> notes = new ArrayList<Note>();
		notes = Note.find.all();
		for (int i = notes.size() - 1; i >= 0; i--) {
			if (!notes.get(i).schoolClass.student.id.equals(student.id)) notes.remove(i);
		}
		return notes;
	}

	public static List<Student> createChildrenList(Parent parent) {
		List<Student> children = Student.find.where().eq("email", parent.email).findList();
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).parent == null) continue;
			if (!children.get(i).parent.email.equals(parent.email)) children.remove(i);
		}
		return children;
	}

	public static List<Assignment> createAssignmentsListForParent(Parent parent) {
		List<Assignment> assignments = Assignment.find.findList();
		ArrayList<Boolean[]> booleans = new ArrayList<Boolean[]>();
		List<Student> children = createChildrenList(parent);
		for (int i = 0; i < assignments.size(); i++) {
			booleans.add(new Boolean[children.size()]);
			for (int j = 0; j < children.size(); j++) {
				booleans.get(i)[j] = false;
			}
		}
		for (int j = 0; j < assignments.size(); j++) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).email.equals(assignments.get(j).schoolClass.student.email)) booleans.get(j)[i] = true;
				else booleans.get(j)[i] = false;
			}
		}
		for (int i = booleans.size() - 1; i >= 0; i--) {
			for (int j = 0; j < booleans.get(i).length; j++) {
				if (booleans.get(i)[j] == true) break;
				if (j == booleans.get(i).length - 1) assignments.remove(i);
			}
		}
		return assignments;
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	public static Result newParent() {
		Form<Parent> filledForm = parentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(parentSignUp.render(parentForm, "Form had errors."));
		} else {
			if (!filledForm.data().get("email").contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(parentSignUp.render(parentForm, "Invalid email address was given."));
			if (Student.exists(filledForm.data().get("email"))) return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));
			if (filledForm.data().get("email").length() >= 250) return badRequest(parentSignUp.render(parentForm, "Given email was too long."));
			if (filledForm.data().get("name").length() >= 250) return badRequest(parentSignUp.render(parentForm, "Given name was too long."));
			if (filledForm.data().get("password").length() >= 250) return badRequest(parentSignUp.render(parentForm, "Given password was too long."));

			Parent parent = Parent.create(filledForm.data().get("name"), filledForm.data().get("email"), filledForm.data().get("password"));
			if (parent == null) {
				return badRequest(parentSignUp.render(parentForm, "That email is already associated with an account."));
			}
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
		}
	}

	public static Result newSchoolClass(String studentID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Error while processing."));
		} else {
			if (filledForm.data().get("subject").length() >= 250) return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "schoolClasses", "Class name was too long."));
			SchoolClass.create(filledForm.data().get("subject"), Long.valueOf(filledForm.data().get("studentId")));
			// TODO ERROR CHECKING
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
		}
	}

	public static Result addChild() {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			Parent parent = Parent.find.ref(filledForm.data().get("email"));
			return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
		} else {
			Parent parent = Parent.find.ref(filledForm.data().get("email"));
			Student newStudent = Student.create(filledForm.data().get("name"), filledForm.data().get("email"), filledForm.data().get("password"), filledForm.data().get("grade"));
			if (newStudent == null) {
				return badRequest(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
			}
			return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
		}
	}

	public static String getSubject(Assignment assignment) {
		if (assignment == null) return "";
		try {
			if (assignment.schoolClass.subject != null) {
				return assignment.schoolClass.subject;
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	// TODO ADD BACK NOTES AND TEACHER

	public static Result newAssignment(String studentID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "addAssignment", "Error while processing."));
		} else {
			Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "assignments", ""));
		}
	}

	public static Result editAssignment(String assignmentID, String studentID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", "Error while processing."));
		} else {
			Assignment.edit(Long.parseLong(assignmentID), SchoolClass.find.ref(Long.parseLong(filledForm.data().get("schoolClassId"))), filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
			return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "assignments", ""));
		}
	}

	public static Result deleteAssignment(String assignmentID, String studentID) {
		Assignment.find.ref(Long.valueOf(assignmentID)).delete();
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "assignments", ""));
	}

	public static Result deleteLateAssignment(String assignmentID, String studentID) {
		Assignment.find.ref(Long.valueOf(assignmentID)).delete();
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "lateAssignments", ""));
	}

	public static Result deleteFinishedAssignment(String assignmentID, String studentID) {
		Assignment.find.ref(Long.valueOf(assignmentID)).delete();
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "finishedAssignments", ""));
	}

	public static Result finishedAssignment(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.ref(Long.valueOf(assignmentID));
		assignment.finished = true;
		assignment.save();
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "finishedAssignments", ""));
	}

	public static void setToday() {
		int day = Calendar.getInstance().get(Calendar.DATE);
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int total = (year * 366) - ((12 - month) * 31) - (31 - day);
		today = total;
	}

	public static Result profileLogin(String studentID) {
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(profile.render(student, createSchoolClassesList(student), createAssignmentsList(student), createFinishedAssignmentsList(student), createLateAssignmentsList(student), createTeachersList(student), createNotesList(student), today, "overview", ""));
	}

	public static Result parentProfileLogin(String parentEmail) {
		Parent parent = Parent.find.ref(parentEmail);
		return ok(parentProfile.render(parent, createChildrenList(parent), createAssignmentsListForParent(parent), today, "", ""));
	}

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

	public static Result authenticate() {
		Form<Login> loginForm = Form.form(Login.class);
		Form<Login> filledForm = null;
		try {
			filledForm = loginForm.bindFromRequest();
		} catch (Exception e) {
			return badRequest(login.render(loginForm, "Invalid email or password"));
		}
		if (filledForm.hasErrors()) {
			return badRequest(login.render(loginForm, "Login error, try again."));
		} else {
			if (Parent.authenticate(filledForm.data().get("email"), filledForm.data().get("password")) == null && Student.authenticate(filledForm.data().get("email"), filledForm.data().get("password")) == null) {
				return badRequest(login.render(loginForm, "Invalid email or password."));
			}

			// Try parent login first
			if (Parent.authenticate(filledForm.data().get("email"), filledForm.data().get("password")) != null) {
				Parent parent = Parent.find.where().eq("email", filledForm.get().email).eq("password", filledForm.get().password).findUnique();
				return redirect(routes.Application.parentProfileLogin(parent.email));
			}

			// Else do student login
			Student student = Student.find.where().eq("email", filledForm.get().email).eq("password", filledForm.get().password).findUnique();
			return redirect(routes.Application.profileLogin(String.valueOf(student.id)));
		}
	}

	public static class Login {
		public String email;
		public String password;

		public String validate() {
			if (Student.authenticate(email, password) == null && Parent.authenticate(email, password) == null) {
				return "Invalid email or password";
			}
			System.err.println("Authenticated");
			return null;
		}
	}

	public static class AccountSettings {
		public String email;
		public String currentPassword;
		public String newPassword;
		public String newPasswordAgain;
		public String name;
		public String grade;
	}

}
