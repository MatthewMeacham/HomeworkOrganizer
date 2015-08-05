package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import javax.persistence.PersistenceException;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import com.matthew.hasher.Hasher;

import models.Parent;
import models.Student;
import models.Teacher;
import models.Assignment;

import controllers.Utilities;
import controllers.Application.AccountSettings;
import controllers.Application.Login;
import controllers.Students;

import views.html.index;
import views.html.parentProfile;
import views.html.studentProfile;

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
		if(parent == null) return redirect(routes.Application.index());
		return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", ""));
	}

	// Direct the request to the student with the ID, parent accounts use this
	public Result redirectToStudent(UUID studentID, String parentID) {
		Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();
		if(parent == null) return redirect(routes.Application.index());
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		if(student == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		List<Student> children = Utilities.createChildrenList(parent);
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).email.equals(student.email)) {
				return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
			}
		}
		return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
	}

	// Add a child to a parent
	public Result createChild(String parentID) {
		Form<Student> filledForm = studentForm.bindFromRequest();
		Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();
		if (parent == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Error while processing."));
		
		String name = filledForm.data().get("name");
		String password = filledForm.data().get("password");
		String grade = filledForm.data().get("grade");

		if (Integer.valueOf(grade) <= MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid grade level."));

		List<Student> children =  Utilities.createChildrenList(parent);
		
		if (name.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid name."));
		if (name.length() >= 250) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Name was too long."));
		for (Student child : children) {
			if(child.name.equals(name))return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Can't have two children with the same name.")); 
		}
		
		if (password.trim().isEmpty()) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid password."));

		try {
			password = HASHER.hashWithSaltSHA256(password, parent.salt);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if(password.equals(parent.password)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid password."));
		for (Student child : children) {
			if(child.password.equals(password)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid password."));
		}
		Student newStudent = null;
		try {
			newStudent = Student.create(name, filledForm.data().get("email").toLowerCase(), parent.salt, password, grade);
		} catch (PersistenceException e) {
			 return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Error while processing."));
		}
		if (newStudent == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "addChild", "Invalid password."));
		
		return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", ""));
	}

	// Changes account settings for a parent account OR account settings for one
	// of their kids and return
	// either A) successful or B) Error
	public Result updateSettings(String parentID, String studentID) {
		Form<AccountSettings> filledForm = accountSettingsForm.bindFromRequest();
		Parent parent = Parent.find.where().eq("ID", UUID.fromString(parentID)).findUnique();
		if(parent == null) return redirect(routes.Application.index());
		List<Student> children = Utilities.createChildrenList(parent);
		if (filledForm.hasErrors()) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));

		String name = filledForm.data().get("name");
		String oldEmail = parent.email;

		// parent request to change themselves
		if (studentID.equals("0")) {
			String email = filledForm.data().get("email").toLowerCase();
			if (!name.equals(parent.name)) {
				if (name.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Name was too long."));
				for (Student child : children) {
					if (child.name.equals(name)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "One of your children already has that name."));
				}
				parent.name = name;
				try {
					parent.save();
				} catch (PersistenceException e) {
					return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
				}
			}

			if (!email.equals(parent.email)) {
				if (email.trim().isEmpty() || !email.contains("@") || !email.contains(".")) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid email address."));
				if (email.length() >= 250) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Email was too long."));
				if (Parent.exists(email) || Teacher.exists(email) || Student.exists(email)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "That email is already associated with an account."));

				//TODO FIGURE OUT WHAT TO DO HERE
				for (int i = 0; i < children.size(); i++) {
					children.get(i).email = email;
					try {
						children.get(i).save();
					} catch (PersistenceException e) {
						//One failed, therefore we need to revert the rest back
						boolean errored = false;
						for(int j = 0; j < i; j++) {
							children.get(j).email = oldEmail;
							try {
								children.get(j).save();
							} catch (PersistenceException e2) {
								//In this case, try to finish the rest, but set errored equal to true because we just want to stop.
								errored = true;
							}
						}
						if(errored) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
					}
				}
				parent.email = email;
				try {
					parent.save();
				} catch (PersistenceException e) {
					for(Student child : children) {
						child.email = oldEmail;
						try {
							child.save();
						} catch (PersistenceException e2) {
							//have to try again, this is important
							int tries = 0;
							do {
								try {
									child.save();
								} catch (PersistenceException e3) {
									//Do nothing, child was probably deleted on another instance of the account
								}
								tries++;
							} while (tries < 5);
						}
					}
					return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
				}
			}
			String currentPassword = filledForm.data().get("currentPassword");
			String newPassword = filledForm.data().get("newPassword");
			String newPasswordAgain = filledForm.data().get("newPasswordAgain");
			if (!currentPassword.trim().isEmpty() && !newPassword.trim().isEmpty() && !newPasswordAgain.trim().isEmpty()) {
				try {
					currentPassword = HASHER.hashWithSaltSHA256(currentPassword, parent.salt);
					newPassword = HASHER.hashWithSaltSHA256(newPassword, parent.salt);
					newPasswordAgain = HASHER.hashWithSaltSHA256(newPasswordAgain, parent.salt);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					e.printStackTrace();
					return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
				}
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid passwords."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "New passwords did not match."));
				for (Student child : children) {
					if (child.password.equals(newPassword)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Child can not have the same password as the parent."));
				}
				parent.password = newPassword;
				try {
					parent.save();
				} catch (PersistenceException e) {
					return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
				}
			}
			return ok(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Account changed successfully."));
		} else {
			// parent request to change student
			Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
			if(student == null) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));

			String grade = filledForm.data().get("grade");
			if (Integer.valueOf(grade) < MIN_GRADE || Integer.valueOf(grade) > MAX_GRADE) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid grade level."));
			student.grade = grade;
			try {
				student.save();
			} catch (PersistenceException e) {
				return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
			}

			if (!name.equals(student.name)) {
				if (name.trim().isEmpty()) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid name."));
				if (name.length() >= 250) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Name was too long."));
				student.name = name;
				try {
					student.save();
				} catch (PersistenceException e) {
					return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
				}
			}
			String currentPassword = filledForm.data().get("currentPassword");
			String newPassword = filledForm.data().get("newPassword");
			String newPasswordAgain = filledForm.data().get("newPasswordAgain");
			if (!currentPassword.trim().isEmpty() && !newPassword.trim().isEmpty() && !newPasswordAgain.trim().isEmpty()) {
				try {
					currentPassword = HASHER.hashWithSaltSHA256(currentPassword, parent.salt);
					newPassword = HASHER.hashWithSaltSHA256(newPassword, parent.salt);
					newPasswordAgain = HASHER.hashWithSaltSHA256(newPasswordAgain, parent.salt);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					e.printStackTrace();
					return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
				}
				if (currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || newPasswordAgain.trim().isEmpty()) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Invalid password."));
				if (!newPassword.equals(newPasswordAgain)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "New passwords did not match."));
				if (newPassword.equals(parent.password)) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Child can not have the same password as the parent."));

				for (Student child : children) {
					if (child == student) continue;
					if (newPassword.equals(child.password)) return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Child can not have the same password as another."));
				}

				student.password = newPassword;
				try {
					student.save();
				} catch (PersistenceException e) {
					return badRequest(parentProfile.render(parent, children, Utilities.createAssignmentsListForParent(parent), Utilities.today, "accountSettings", "Error while processing."));
				}
			}
			return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Account changed successfully."));
		}
	}

	// Refresh the parentProfile page
	public Result refresh(UUID parentID) {
		Parent parent = Parent.find.where().eq("ID", parentID).findUnique();
		if (parent == null) return redirect(routes.Application.index());
		return redirect(routes.Parents.toProfile(parent.id.toString()));
	}
	
	public Result deleteParentAccount(UUID parentID){
		Parent parent = Parent.find.where().eq("ID", parentID).findUnique();
		if(parent == null) return redirect(routes.Application.index());
		List<Assignment> assignments = Utilities.createAssignmentsListForParent(parent);
		List<Assignment> finishedAssignments = Utilities.createFinishedAssignmentsListForParent(parent);
		for (int i = 0; i < assignments.size(); i++) {
			assignments.get(i).delete();
		}
		for (int i = 0; i < finishedAssignments.size(); i++) {
			finishedAssignments.get(i).delete();
		}
		List<Student> children = Utilities.createChildrenList(parent);
		for (int i = 0; i < children.size(); i++) {
			UUID studentID = children.get(i).id;
			Students s = new Students();
			s.deleteStudentAccount(studentID);
		}
		
		parent.delete();
		return redirect(routes.Application.index());
	}

}
