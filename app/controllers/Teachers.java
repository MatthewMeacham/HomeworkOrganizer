package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.List;

import javax.persistence.PersistenceException;

import com.matthew.hasher.Hasher;

import models.Parent;
import models.Student;
import models.Teacher;
import models.Assignment;
import models.SchoolClass;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.Application.AccountSettings;
import controllers.Application.Login;
import controllers.Utilities;
import controllers.Students;

import views.html.index;
import views.html.teacherProfile;

public class Teachers extends Controller {

	private static final Hasher HASHER = new Hasher();

	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);
	private static Form<Login> loginForm = Form.form(Login.class);

	// Direct to the teacher profile page after authentication
	public Result toProfile(UUID teacherID) {
		Teacher teacher = Teacher.find.where().eq("ID", teacherID).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "", ""));
	}

	// Changes account settings for a teacher
	public Result updateSettings(String teacherID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Error while processing."));
		String name = filledForm.data().get("name");
		if (!name.equals(teacher.name)) {
			if (name.trim().isEmpty()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Invalid name."));
			if (name.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Name was too long."));
			teacher.name = name;
			try {
				teacher.save();
			} catch (PersistenceException e) {
				return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Error while processing."));
			}
		}

		String email = filledForm.data().get("email").toLowerCase();
		if (!email.equals(teacher.email)) {
			if (email.trim().isEmpty() || !email.contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Invalid email address.."));
			if (email.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Email was too long."));
			if (Parent.exists(email) || Student.exists(email) || Teacher.exists(email)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "That email is already associated with an account."));
			teacher.email = email;
			try {
				teacher.save();
			} catch (PersistenceException e) {
				return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Error while processing."));
			}
		}

		try {
			String currentPassword = filledForm.data().get("currentPassword");
			String newPassword = filledForm.data().get("newPassword");
			String newPasswordAgain = filledForm.data().get("newPasswordAgain");
			if (newPassword.length() < 8 || newPasswordAgain.length() < 8) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "New password must be at least 8 characters long."));
			if (!currentPassword.trim().isEmpty() && !newPassword.trim().isEmpty() && !newPasswordAgain.trim().isEmpty()) {
				currentPassword = HASHER.hashWithSaltSHA256(currentPassword, teacher.salt);
				newPassword = HASHER.hashWithSaltSHA256(newPassword, teacher.salt);
				newPasswordAgain = HASHER.hashWithSaltSHA256(newPasswordAgain, teacher.salt);
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Invalid passwords."));
				if (!currentPassword.equals(teacher.password)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Current password was incorrect."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "New passwords did not match."));
				teacher.password = newPassword;
				try {
					teacher.save();
				} catch (PersistenceException e) {
					return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Error while processing."));
				}
			}
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Error while processing."));
		}

		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", "Account changed successfully."));
	}

	// Refresh the teacherProfile page
	public Result refresh(UUID teacherID) {
		Teacher teacher = Teacher.find.where().eq("ID", teacherID).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		return redirect(routes.Teachers.toProfile(teacher.id));
	}

	// Deletes the teacher account and everything created by the teacher, redirects to home page
	public Result deleteTeacherAccount(UUID teacherID) {
		Teacher teacher = Teacher.find.where().eq("ID", teacherID).findUnique();
		if (teacher == null) return redirect(routes.Application.index());

		List<Assignment> assignments = Utilities.createAssignmentsListForTeacher(teacher);
		List<SchoolClass> classes = Utilities.createSchoolClassListForTeacher(teacher);
		for (int i = 0; i < assignments.size(); i++) {
			try {
				assignments.get(i).delete();
			} catch (PersistenceException e) {
				// Do nothing
			}
		}
		for (int i = 0; i < classes.size(); i++) {
			routes.Classes.deleteForTeacher(classes.get(i).id.toString(), teacherID.toString());
		}

		teacher.delete();
		return redirect(routes.Application.index());
	}

}
