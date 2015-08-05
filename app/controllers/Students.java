package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.List;
import javax.persistence.PersistenceException;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import com.matthew.hasher.Hasher;

import models.Parent;
import models.Student;
import models.Teacher;
import models.Assignment;
import models.SchoolClass;

import controllers.Application.AccountSettings;
import controllers.Application.Login;
import controllers.Classes;
import controllers.Utilities;

import views.html.index;
import views.html.studentProfile;

public class Students extends Controller {

	private static final Hasher HASHER = new Hasher();

	private final static int MIN_GRADE = 1;
	private final static int MAX_GRADE = 16;

	private static Form<AccountSettings> accountSettingsForm = Form.form(AccountSettings.class);
	private static Form<Login> loginForm = Form.form(Login.class);

	// Direct to the student profile page after authentication
	public Result toProfile(String studentID) {
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if(student == null) return redirect(routes.Application.index());
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
	}

	// Changes the account settings for the student with the given studentID and
	// returns either A) Successful or B) Error
	public Result updateSettings(String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error occurred while processing."));
		String grade = filledForm.data().get("grade");
		if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid grade level."));
		student.grade = grade;
		try {
			student.save();
		} catch (PersistenceException e) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error occurred while processing."));
		}

		String name = filledForm.data().get("name");
		if (!name.equals(student.name)) {
			if (name.trim().isEmpty()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid name."));
			if (name.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Name was too long."));
			student.name = name;
			try {
				student.save();
			} catch (PersistenceException e) {
				return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error occurred while processing."));
			}
		}

		String email = filledForm.data().get("email").toLowerCase();
		if (!email.equals(student.email)) {
			if (email.trim().isEmpty() || !email.contains("@") || !email.contains(".")) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid email address."));
			if (email.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Email was too long."));
			if (Parent.exists(email) || Student.exists(email) || Teacher.exists(email)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Email is already associated with an account."));
			student.email = email;
			try {
				student.save();
			} catch (PersistenceException e) {
				return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error occurred while processing."));
			}
		}

		try {
			String currentPassword = filledForm.data().get("currentPassword");
			String newPassword = filledForm.data().get("newPassword");
			String newPasswordAgain = filledForm.data().get("newPasswordAgain");
			if (!currentPassword.trim().isEmpty() && !newPassword.trim().isEmpty() && !newPasswordAgain.trim().isEmpty()) {
				currentPassword = HASHER.hashWithSaltSHA256(currentPassword, student.salt);
				newPassword = HASHER.hashWithSaltSHA256(newPassword, student.salt);
				newPasswordAgain = HASHER.hashWithSaltSHA256(newPasswordAgain, student.salt);
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Invalid passwords."));
				if (!currentPassword.equals(student.password)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Current password was incorrect."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "New passwords did not match."));
				student.password = newPassword;
				try {
					student.save();
				} catch (PersistenceException e) {
					return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error while processing."));
				}
			}
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "accountSettings", "Error while processing."));
		}

		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Account changed successfully."));
	}

	// Refresh the studentProfile page
	public Result refresh(UUID studentID) {
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		if (student == null) return redirect(routes.Application.index());
		return redirect(routes.Students.toProfile(student.id.toString()));
	}
	
	//Deletes the student account and redirects to main page
	public Result deleteStudentAccount(UUID studentID) {
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		if(student == null) return redirect(routes.Application.index());
		List<Assignment> assignments = Utilities.createAssignmentsList(student);
		List<Assignment> finishedAssignments = Utilities.createFinishedAssignmentsList(student);
		for(int i = 0; i < assignments.size(); i++){
			assignments.get(i).delete();
		}
		for(int j = 0; j < finishedAssignments.size(); j++){
			finishedAssignments.get(j).delete();
		}
		String studentid = studentID.toString();
		List<SchoolClass> classes = Utilities.createSchoolClassesList(student);
		if(classes == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));
		for(int i = 0; i < classes.size(); i++){
			if(classes.get(i).teacherID == null){
			classes.get(i).delete();
			}
			else{
				classes.get(i).students.remove(student);
				try {
				classes.get(i).save();
			} catch (PersistenceException e) {
				return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));
			}
			}
		}
		student.delete();
	
		
		
		return redirect(routes.Application.index());
	}

}
