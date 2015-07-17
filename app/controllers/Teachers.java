package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.matthew.hasher.Hasher;

import models.Parent;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.teacherProfile;
import controllers.Application.AccountSettings;
import controllers.Application.Login;

public class Teachers extends Controller {

	private static final Hasher HASHER = new Hasher();

	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);
	private static Form<Login> loginForm = Form.form(Login.class);

	// Direct to the teacher profile page after authentication
	public Result toProfile(UUID teacherID) {
		Teacher teacher = Teacher.find.where().eq("ID", teacherID).findUnique();
		return ok(views.html.teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "", ""));
	}

	// Changes account settings for a teacher
	public Result updateSettings(String teacherID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (filledForm.hasErrors()) {
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Error while processing."));
		} else {
			String name = filledForm.data().get("name");
			if (!name.equals(teacher.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Name was too long."));
				teacher.name = name;
				teacher.save();
			}

			String email = filledForm.data().get("email").toLowerCase();
			if (!email.equals(teacher.email)) {
				if (email.equals("") || email.trim().isEmpty() || !email.contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Invalid email address.."));
				if (email.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Email was too long."));
				if (Parent.exists(email) || Student.exists(email) || Teacher.exists(email)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "That email is already associated with an account."));
				teacher.email = email;
				teacher.save();
			}

			try {
				String currentPassword = filledForm.data().get("currentPassword");
				String newPassword = filledForm.data().get("newPassword");
				String newPasswordAgain = filledForm.data().get("newPasswordAgain");
				if (!currentPassword.equals("") && !newPassword.equals("") && !newPasswordAgain.equals("")) {
					currentPassword = HASHER.hashWithSaltSHA256(currentPassword, teacher.salt);
					newPassword = HASHER.hashWithSaltSHA256(newPassword, teacher.salt);
					newPasswordAgain = HASHER.hashWithSaltSHA256(newPasswordAgain, teacher.salt);
					if (currentPassword.equals("") || !currentPassword.equals(teacher.password)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Current password was incorrect."));
					if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Invalid new password."));
					if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Invalid passwords."));
					if (!newPassword.equals(newPasswordAgain)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "New passwords did not match."));
					teacher.password = newPassword;
					teacher.save();
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "accountSettings", "Error while processing."));
			}

			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", "Account changed successfully."));
		}
	}
	
	// Refresh the teacherProfile page
	public Result refresh(UUID teacherID) {
		Teacher teacher = Teacher.find.where().eq("ID", teacherID).findUnique();
		if (teacher == null) return badRequest(views.html.index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		return redirect(routes.Teachers.toProfile(teacher.id));
	}

}
