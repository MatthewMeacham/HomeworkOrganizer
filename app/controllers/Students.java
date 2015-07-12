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
import views.html.profile;
import controllers.Application.AccountSettings;
import controllers.Utilities;

public class Students extends Controller {

	private static final Hasher HASHER = new Hasher();

	private final static int MIN_GRADE = 1;
	private final static int MAX_GRADE = 16;

	private static Form<AccountSettings> accountSettingsForm = Form
			.form(AccountSettings.class);

	// Direct to the student profile page after authentication
	public Result profileLogin(String studentID) {
		Student student = Student.find.ref(Long.valueOf(studentID));
		return ok(profile.render(student,
				Utilities.createSchoolClassesList(student),
				Utilities.createAssignmentsList(student),
				Utilities.createFinishedAssignmentsList(student),
				Utilities.createLateAssignmentsList(student),
				Utilities.createTeachersList(student),
				Utilities.createNotesList(student), Utilities.today,
				"overview", ""));
	}

	// Changes the account settings for the student with the given studentID and
	// returns either A) Successful or B) Error
	public Result changeAccountSettings(String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm
				.bindFromRequest();
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (filledForm.hasErrors()) {
			return badRequest(profile.render(student,
					Utilities.createSchoolClassesList(student),
					Utilities.createAssignmentsList(student),
					Utilities.createFinishedAssignmentsList(student),
					Utilities.createLateAssignmentsList(student),
					Utilities.createTeachersList(student),
					Utilities.createNotesList(student), Utilities.today,
					"accountSettings", "Error occurred while processing."));
		} else {
			String grade = filledForm.data().get("grade");
			if (Integer.valueOf(grade) <= MIN_GRADE
					|| Integer.valueOf(grade) > MAX_GRADE)
				return badRequest(profile.render(student,
						Utilities.createSchoolClassesList(student),
						Utilities.createAssignmentsList(student),
						Utilities.createFinishedAssignmentsList(student),
						Utilities.createLateAssignmentsList(student),
						Utilities.createTeachersList(student),
						Utilities.createNotesList(student), Utilities.today,
						"accountSettings", "Invalid grade level."));
			student.grade = grade;
			student.save();

			String name = filledForm.data().get("name");
			if (!name.equals(student.name)) {
				if (name.equals("") || name.trim().isEmpty())
					return badRequest(profile
							.render(student,
									Utilities.createSchoolClassesList(student),
									Utilities.createAssignmentsList(student),
									Utilities
											.createFinishedAssignmentsList(student),
									Utilities
											.createLateAssignmentsList(student),
									Utilities.createTeachersList(student),
									Utilities.createNotesList(student),
									Utilities.today, "accountSettings",
									"Invalid name."));
				if (name.length() >= 250)
					return badRequest(profile.render(student,
							Utilities.createSchoolClassesList(student),
							Utilities.createAssignmentsList(student),
							Utilities.createFinishedAssignmentsList(student),
							Utilities.createLateAssignmentsList(student),
							Utilities.createTeachersList(student),
							Utilities.createNotesList(student),
							Utilities.today, "accountSettings",
							"Name was too long."));
				student.name = name;
				student.save();
			}

			String email = filledForm.data().get("email").toLowerCase();
			if (!email.equals(student.email)) {
				if (email.equals("") || email.trim().isEmpty()
						|| !email.contains("@")
						|| !filledForm.data().get("email").contains("."))
					return badRequest(profile.render(student,
							Utilities.createSchoolClassesList(student),
							Utilities.createAssignmentsList(student),
							Utilities.createFinishedAssignmentsList(student),
							Utilities.createLateAssignmentsList(student),
							Utilities.createTeachersList(student),
							Utilities.createNotesList(student),
							Utilities.today, "accountSettings",
							"Invalid email address."));
				if (email.length() >= 250)
					return badRequest(profile.render(student,
							Utilities.createSchoolClassesList(student),
							Utilities.createAssignmentsList(student),
							Utilities.createFinishedAssignmentsList(student),
							Utilities.createLateAssignmentsList(student),
							Utilities.createTeachersList(student),
							Utilities.createNotesList(student),
							Utilities.today, "accountSettings",
							"Email was too long."));
				if (Parent.exists(email) || Student.exists(email)
						|| Teacher.exists(email))
					return badRequest(profile.render(student,
							Utilities.createSchoolClassesList(student),
							Utilities.createAssignmentsList(student),
							Utilities.createFinishedAssignmentsList(student),
							Utilities.createLateAssignmentsList(student),
							Utilities.createTeachersList(student),
							Utilities.createNotesList(student),
							Utilities.today, "accountSettings",
							"Email is already associated with an account."));
				student.email = email;
				student.save();
			}

			try {
				String currentPassword = HASHER.hashWithSaltSHA256(filledForm
						.data().get("currentPassword"), student.salt);
				String newPassword = HASHER.hashWithSaltSHA256(filledForm
						.data().get("newPassword"), student.salt);
				String newPasswordAgain = HASHER.hashWithSaltSHA256(filledForm
						.data().get("newPasswordAgain"), student.salt);
				if (!currentPassword.equals("") || !newPassword.equals("")
						|| !newPasswordAgain.equals("")) {
					if (currentPassword.equals("")
							|| !currentPassword.equals(student.password))
						return badRequest(profile.render(student, Utilities
								.createSchoolClassesList(student), Utilities
								.createAssignmentsList(student), Utilities
								.createFinishedAssignmentsList(student),
								Utilities.createLateAssignmentsList(student),
								Utilities.createTeachersList(student),
								Utilities.createNotesList(student),
								Utilities.today, "accountSettings",
								"Current password was incorrect."));
					if (newPassword.equals("") || newPasswordAgain.equals(""))
						return badRequest(profile.render(student, Utilities
								.createSchoolClassesList(student), Utilities
								.createAssignmentsList(student), Utilities
								.createFinishedAssignmentsList(student),
								Utilities.createLateAssignmentsList(student),
								Utilities.createTeachersList(student),
								Utilities.createNotesList(student),
								Utilities.today, "accountSettings",
								"Invalid new password."));
					if (currentPassword.trim().isEmpty()
							|| newPassword.trim().isEmpty()
							|| newPasswordAgain.trim().isEmpty())
						return badRequest(profile.render(student, Utilities
								.createSchoolClassesList(student), Utilities
								.createAssignmentsList(student), Utilities
								.createFinishedAssignmentsList(student),
								Utilities.createLateAssignmentsList(student),
								Utilities.createTeachersList(student),
								Utilities.createNotesList(student),
								Utilities.today, "accountSettings",
								"Invalid passwords."));
					if (!newPassword.equals(newPasswordAgain))
						return badRequest(profile.render(student, Utilities
								.createSchoolClassesList(student), Utilities
								.createAssignmentsList(student), Utilities
								.createFinishedAssignmentsList(student),
								Utilities.createLateAssignmentsList(student),
								Utilities.createTeachersList(student),
								Utilities.createNotesList(student),
								Utilities.today, "accountSettings",
								"New passwords did not match."));
					student.password = newPassword;
					student.save();
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				return badRequest(profile.render(student,
						Utilities.createSchoolClassesList(student),
						Utilities.createAssignmentsList(student),
						Utilities.createFinishedAssignmentsList(student),
						Utilities.createLateAssignmentsList(student),
						Utilities.createTeachersList(student),
						Utilities.createNotesList(student), Utilities.today,
						"accountSettings", "Error while processing."));
			}

			return ok(profile.render(student,
					Utilities.createSchoolClassesList(student),
					Utilities.createAssignmentsList(student),
					Utilities.createFinishedAssignmentsList(student),
					Utilities.createLateAssignmentsList(student),
					Utilities.createTeachersList(student),
					Utilities.createNotesList(student), Utilities.today,
					"overview", "Account changed successfully."));
		}
	}

}
