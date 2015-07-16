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
import views.html.index;
import views.html.studentProfile;
import controllers.Application.AccountSettings;
import controllers.Application.Login;
import controllers.Utilities;

public class Students extends Controller {

	private static final Hasher HASHER = new Hasher();

	private final static int MIN_GRADE = 1;
	private final static int MAX_GRADE = 16;

	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);
	private static Form<Login> loginForm = Form.form(Login.class);

	// Direct to the student profile page after authentication
	public Result toProfile(String studentID) {
		Student student = Student.find.ref(UUID.fromString(studentID));
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
	}

	// Changes the account settings for the student with the given studentID and
	// returns either A) Successful or B) Error
	public Result updateSettings(String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Student student = Student.find.ref(UUID.fromString(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error occurred while processing."));
		} else {
			String grade = filledForm.data().get("grade");
			if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid grade level."));
			student.grade = grade;
			student.save();

			String name = filledForm.data().get("name");
			if (!name.equals(student.name)) {
				if (name.equals("") || name.trim().isEmpty()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Name was too long."));
				student.name = name;
				student.save();
			}

			String email = filledForm.data().get("email").toLowerCase();
			if (!email.equals(student.email)) {
				if (email.equals("") || email.trim().isEmpty() || !email.contains("@") || !filledForm.data().get("email").contains(".")) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid email address."));
				if (email.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Email was too long."));
				if (Parent.exists(email) || Student.exists(email) || Teacher.exists(email)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Email is already associated with an account."));
				student.email = email;
				student.save();
			}

			try {
				String currentPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("currentPassword"), student.salt);
				String newPassword = HASHER.hashWithSaltSHA256(filledForm.data().get("newPassword"), student.salt);
				String newPasswordAgain = HASHER.hashWithSaltSHA256(filledForm.data().get("newPasswordAgain"), student.salt);
				if (!currentPassword.equals("") && !newPassword.equals("") && !newPasswordAgain.equals("")) {
					if (currentPassword.equals("") || !currentPassword.equals(student.password)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Current password was incorrect."));
					if (newPassword.equals("") || newPasswordAgain.equals("")) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid new password."));
					if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid passwords."));
					if (!newPassword.equals(newPasswordAgain)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "New passwords did not match."));
					student.password = newPassword;
					student.save();
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error while processing."));
			}

			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Account changed successfully."));
		}
	}

	// Refreshed the studentProfile page
	public Result refresh(UUID studentID) {
		System.out.println("CALLED");
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		return redirect(routes.Students.toProfile(student.id.toString()));
	}

}
