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
import views.html.teacherProfile;
import controllers.Application.AccountSettings;

public class Teachers extends Controller {

	private static final Hasher HASHER = new Hasher();

	private static Form<AccountSettings> accountSettingsForm = Form
			.form(AccountSettings.class);

	// Direct to the teacher profile page after authentication
	public Result toProfile(Long teacherID) {
		Teacher teacher = Teacher.find.ref(teacherID);
		return ok(views.html.teacherProfile.render(teacher,
				Utilities.createAssignmentsListForTeacher(teacher),
				Utilities.createSchoolClassListForTeacher(teacher),
				Utilities.today, "", ""));
	}

	// Changes account settings for a teacher
	public Result updateSettings(String teacherID) {
		Form<AccountSettings> filledForm = accountSettingsForm
				.bindFromRequest();
		Teacher teacher = Teacher.find.ref(Long.valueOf(teacherID));
		if (filledForm.hasErrors()) {
			return badRequest(teacherProfile.render(teacher,
					Utilities.createAssignmentsListForTeacher(teacher),
					Utilities.createSchoolClassListForTeacher(teacher),
					Utilities.today, "accountSettings",
					"Error while processing."));
		} else {
			String name = filledForm.data().get("name");
			if (!name.equals(teacher.name)) {
				if (name.equals("") || name.trim().isEmpty())
					return badRequest(teacherProfile
							.render(teacher,
									Utilities
											.createAssignmentsListForTeacher(teacher),
									Utilities
											.createSchoolClassListForTeacher(teacher),
									Utilities.today, "accountSettings",
									"Invalid name."));
				if (name.length() >= 250)
					return badRequest(teacherProfile.render(teacher,
							Utilities.createAssignmentsListForTeacher(teacher),
							Utilities.createSchoolClassListForTeacher(teacher),
							Utilities.today, "accountSettings",
							"Name was too long."));
				teacher.name = name;
				teacher.save();
			}

			String email = filledForm.data().get("email").toLowerCase();
			if (!email.equals(teacher.email)) {
				if (email.equals("") || email.trim().isEmpty()
						|| !email.contains("@")
						|| !filledForm.data().get("email").contains("."))
					return badRequest(teacherProfile.render(teacher,
							Utilities.createAssignmentsListForTeacher(teacher),
							Utilities.createSchoolClassListForTeacher(teacher),
							Utilities.today, "accountSettings",
							"Invalid email address.."));
				if (email.length() >= 250)
					return badRequest(teacherProfile.render(teacher,
							Utilities.createAssignmentsListForTeacher(teacher),
							Utilities.createSchoolClassListForTeacher(teacher),
							Utilities.today, "accountSettings",
							"Email was too long."));
				if (Parent.exists(email) || Student.exists(email)
						|| Teacher.exists(email))
					return badRequest(teacherProfile
							.render(teacher,
									Utilities
											.createAssignmentsListForTeacher(teacher),
									Utilities
											.createSchoolClassListForTeacher(teacher),
									Utilities.today, "accountSettings",
									"That email is already associated with an account."));
				teacher.email = email;
				teacher.save();
			}

			try {
				String currentPassword = HASHER.hashWithSaltSHA256(filledForm
						.data().get("currentPassword"), teacher.salt);
				String newPassword = HASHER.hashWithSaltSHA256(filledForm
						.data().get("newPassword"), teacher.salt);
				String newPasswordAgain = HASHER.hashWithSaltSHA256(filledForm
						.data().get("newPasswordAgain"), teacher.salt);
				if (!currentPassword.equals("") && !newPassword.equals("")
						&& !newPasswordAgain.equals("")) {
					if (currentPassword.equals("")
							|| !currentPassword.equals(teacher.password))
						return badRequest(teacherProfile
								.render(teacher,
										Utilities
												.createAssignmentsListForTeacher(teacher),
										Utilities
												.createSchoolClassListForTeacher(teacher),
										Utilities.today, "accountSettings",
										"Current password was incorrect."));
					if (newPassword.equals("") || newPasswordAgain.equals(""))
						return badRequest(teacherProfile
								.render(teacher,
										Utilities
												.createAssignmentsListForTeacher(teacher),
										Utilities
												.createSchoolClassListForTeacher(teacher),
										Utilities.today, "accountSettings",
										"Invalid new password."));
					if (currentPassword.trim().isEmpty()
							|| newPassword.trim().isEmpty()
							|| newPasswordAgain.trim().isEmpty())
						return badRequest(teacherProfile
								.render(teacher,
										Utilities
												.createAssignmentsListForTeacher(teacher),
										Utilities
												.createSchoolClassListForTeacher(teacher),
										Utilities.today, "accountSettings",
										"Invalid passwords."));
					if (!newPassword.equals(newPasswordAgain))
						return badRequest(teacherProfile
								.render(teacher,
										Utilities
												.createAssignmentsListForTeacher(teacher),
										Utilities
												.createSchoolClassListForTeacher(teacher),
										Utilities.today, "accountSettings",
										"New passwords did not match."));
					teacher.password = newPassword;
					teacher.save();
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				return badRequest(teacherProfile.render(teacher,
						Utilities.createAssignmentsListForTeacher(teacher),
						Utilities.createSchoolClassListForTeacher(teacher),
						Utilities.today, "accountSettings",
						"Error while processing."));
			}

			return badRequest(teacherProfile.render(teacher,
					Utilities.createAssignmentsListForTeacher(teacher),
					Utilities.createSchoolClassListForTeacher(teacher),
					Utilities.today, "overview",
					"Account changed successfully."));
		}
	}

}
