package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import com.matthew.hasher.Hasher;

import models.Parent;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.parentProfile;
import views.html.studentProfile;
import controllers.Utilities;
import controllers.Application.AccountSettings;
import controllers.Application.Login;

public class Parents extends Controller {

	private static final Hasher HASHER = new Hasher();

	private static Form<Student> studentForm = Form.form(Student.class);
	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);
	private static Form<Login> loginForm = Form.form(Login.class);

	private final static int MIN_GRADE = 1;
	private final static int MAX_GRADE = 16;

	// Direct to the parent profile page after authentication
	public Result toProfile(String parentID) {
		Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();
		return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "", ""));
	}

	// Direct the request to the student with the ID, parent accounts use this
	public Result redirectToStudent(UUID studentID, String parentID) {
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();
		List<Student> children = Utilities.createChildrenList(parent);
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).email.equals(student.email)) {
				return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
			}
		}
		return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "", ""));
	}

	// Add a child to a parent
	public Result createChild(String parentID) {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();
			return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Error while processing."));
		} else {
			Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();

			String name = filledForm.data().get("name");
			String password = filledForm.data().get("password");
			String grade = filledForm.data().get("grade");

			if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid grade level."));

			if (name.equals("") || name.trim().isEmpty()) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid name."));
			if (name.length() >= 250) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Name was too long."));

			if (password.equals("") || password.trim().isEmpty() || password.equals(parent.password)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid password"));

			try {
				password = HASHER.hashWithSaltSHA256(password, parent.salt);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			Student newStudent = Student.create(name, filledForm.data().get("email").toLowerCase(), parent.salt, password, grade);
			if (newStudent == null) {
				return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Can't have the same password as another one of your kids."));
			}
			return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", ""));
		}
	}

	// Changes account settings for a parent account OR account settings for one
	// of their kids and return
	// either A) successful or B) Error
	public Result updateSettings(String parentID, String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();
		List<Student> children = Utilities.createChildrenList(parent);
		if (filledForm.hasErrors()) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));

		String name = filledForm.data().get("name");

		// parent request to change themselves
		if (studentID.equals("0")) {
			String email = filledForm.data().get("email").toLowerCase();
			if (!name.equals(parent.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Name was too long."));
				for (Student child : children) {
					if (child.name.equals(name)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "One of your children already has that name."));
				}
				parent.name = name;
				parent.save();
			}

			if (!email.equals(parent.email)) {
				if (email.equals("") || email.trim().isEmpty() || !email.contains("@") || !email.contains(".")) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid email address."));
				if (email.length() >= 250) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Email was too long."));
				if (Parent.exists(email) || Teacher.exists(email) || Student.exists(email)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "That email is already associated with an account."));

				for (Student child : children) {
					child.email = email;
					child.save();
				}
				parent.email = email;
				parent.save();
			}
			String currentPassword = filledForm.data().get("currentPassword");
			String newPassword = filledForm.data().get("newPassword");
			String newPasswordAgain = filledForm.data().get("newPasswordAgain");
			if (!currentPassword.equals("") && !newPassword.equals("") && !newPasswordAgain.equals("")) {
				try {
					currentPassword = HASHER.hashWithSaltSHA256(currentPassword, parent.salt);
					newPassword = HASHER.hashWithSaltSHA256(newPassword, parent.salt);
					newPasswordAgain = HASHER.hashWithSaltSHA256(newPasswordAgain, parent.salt);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (currentPassword.equals("") || !currentPassword.equals(parent.password)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Current password was incorrect."));
				if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid new password."));
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid passwords."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "New passwords did not match."));
				for (Student child : children) {
					if (child.password.equals(newPassword)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Child can not have the same password as the parent."));
				}
				parent.password = newPassword;
				parent.save();

			}
			return ok(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Account changed successfully."));
		} else {
			// parent request to change student
			Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();

			String grade = filledForm.data().get("grade");
			if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid grade level."));
			student.grade = grade;
			student.save();

			if (!name.equals(student.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Name was too long."));
				student.name = name;
				student.save();
			}
			String currentPassword = filledForm.data().get("currentPassword");
			String newPassword = filledForm.data().get("newPassword");
			String newPasswordAgain = filledForm.data().get("newPasswordAgain");
			if (!currentPassword.equals("") && !newPassword.equals("") && !newPasswordAgain.equals("")) {
				try {
					currentPassword = HASHER.hashWithSaltSHA256(currentPassword, parent.salt);
					newPassword = HASHER.hashWithSaltSHA256(newPassword, parent.salt);
					newPasswordAgain = HASHER.hashWithSaltSHA256(newPasswordAgain, parent.salt);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (currentPassword.equals("") || !currentPassword.equals(student.password)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Current password was incorrect."));
				if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid new password."));
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid password."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "New passwords did not match."));
				if (newPassword.equals(parent.password)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Child can not have the same password as the parent."));

				for (Student child : children) {
					if (child == student) continue;
					if (newPassword.equals(child.password)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Child can not have the same password as another."));
				}

				student.password = newPassword;
				student.save();
			}
			return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Account changed successfully."));
		}
	}

	// Refresh the parentProfile page
	public Result refresh(UUID parentID) {
		Parent parent = Parent.find.where().eq("ID", parentID).findUnique();
		if (parent == null) return badRequest(views.html.index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		return redirect(routes.Parents.toProfile(parent.id.toString()));
	}

}
