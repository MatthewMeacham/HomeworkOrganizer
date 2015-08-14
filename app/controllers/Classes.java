package controllers;

import java.util.List;
import java.util.UUID;

import javax.persistence.PersistenceException;

import controllers.Application.Login;
import controllers.Application.SchoolClassFromCode;
import models.Assignment;
import models.SchoolClass;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.index;
import views.html.studentProfile;
import views.html.teacherProfile;
import views.html.schoolClassEdit;
import views.html.schoolClassEditForTeacher;
import views.html.unauthorizedError;

public class Classes extends Controller {

	private Form<SchoolClass> schoolClassForm = Form.form(SchoolClass.class);
	private Form<Login> loginForm = Form.form(Login.class);
	private Form<SchoolClassFromCode> schoolClassFromCodeForm = Form.form(SchoolClassFromCode.class);

	// Create a new school class from the request
	public Result create(String studentID) {
		if(session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClassList = Utilities.createSchoolClassesList(student);
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Class name can not be just spaces."));
		if (subject.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Class name was too long."));
		for (SchoolClass schoolClass : schoolClassList) {
			if (subject.equals(schoolClass.subject)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Can't have two classes with the same name."));
		}
		try {
			SchoolClass.create(subject, student.email, UUID.fromString(studentID), filledForm.data().get("color"), "");
		} catch (PersistenceException e) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
	}

	// Creates a new school class for the teacher from the request
	public Result createForTeacher(String teacherID) {
		if(session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Error while processing."));
		List<SchoolClass> schoolClassList = Utilities.createSchoolClassListForTeacher(teacher);
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Class name can't be spaces."));
		if (subject.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Class name was too long."));
		for (SchoolClass schoolClass : schoolClassList) {
			if (subject.equals(schoolClass.subject)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Can't have two classes with the same name."));
		}
		try {
			SchoolClass.create(subject, teacher.email, UUID.fromString(teacherID), filledForm.data().get("color"), filledForm.data().get("password"));
		} catch (PersistenceException e) {
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Error while processing."));
		}
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", ""));
	}

	// Create a new school class from a teacher provided id and password
	public Result createFromTeacher(String studentID) {
		if(session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Form<SchoolClassFromCode> filledForm = schoolClassFromCodeForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));

		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassID"))).findUnique();
		if (schoolClass == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Invalid class ID."));
		if (!schoolClass.password.equals(filledForm.data().get("password"))) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Password was incorrect."));
		List<SchoolClass> schoolClasses = Utilities.createSchoolClassesList(student);
		for (SchoolClass schoolClassIterated : schoolClasses) {
			if (schoolClassIterated.subject.equals(schoolClass.subject)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Can't have two classes with the same name."));
		}
		schoolClass.students.add(student);
		try {
			schoolClass.save();
		} catch (PersistenceException e) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));
		}

		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
	}

	// Direct to the edit school class page
	public Result read(String schoolClassID, String studentID) {
		if(session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		if (schoolClass == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClasses = Utilities.createSchoolClassesList(student);
		if (!schoolClasses.contains(schoolClass)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Unauthorized request."));

		return ok(schoolClassEdit.render(schoolClass, student, ""));
	}

	// Direct to the edit school class page for teacher
	public Result readForTeacher(String schoolClassID, String teacherID) {
		if(session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClasses = Utilities.createSchoolClassListForTeacher(teacher);
		if (!schoolClasses.contains(schoolClass)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Unauthorized request."));

		return ok(schoolClassEditForTeacher.render(schoolClass, teacher, ""));
	}

	// Edit a schoolClass from a request
	public Result update(String schoolClassID, String studentID) {
		if(session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		if (schoolClass == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClasses = Utilities.createSchoolClassesList(student);
		if (!schoolClasses.contains(schoolClass)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Unauthorized request."));

		if (filledForm.hasErrors()) return badRequest(schoolClassEdit.render(schoolClass, student, "Error while processing."));
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty()) return badRequest(schoolClassEdit.render(schoolClass, student, "Subject can not be empty."));
		if (subject.length() >= 250) return badRequest(schoolClassEdit.render(schoolClass, student, "Subject was too long."));
		List<SchoolClass> schoolClassList = Utilities.createSchoolClassesList(student);
		for (SchoolClass schoolClass2 : schoolClassList) {
			if (schoolClass.id == schoolClass2.id) continue;
			if (subject.equals(schoolClass2.subject)) return badRequest(schoolClassEdit.render(schoolClass, student, "Can't have two classes with the same name."));
		}
		String color = filledForm.data().get("color");
		SchoolClass.edit(Long.valueOf(schoolClassID), subject, color, student.id, "");
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
	}

	// Edit a schoolClass for a teacher from a request
	public Result updateForTeacher(String schoolClassID, String teacherID) {
		if(session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClasses = Utilities.createSchoolClassListForTeacher(teacher);
		if (!schoolClasses.contains(schoolClass)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Unauthorized request."));

		if (filledForm.hasErrors()) return badRequest(schoolClassEditForTeacher.render(schoolClass, teacher, "Error while processing."));
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty()) return badRequest(schoolClassEditForTeacher.render(schoolClass, teacher, "Subject can not be empty."));
		if (subject.length() >= 250) return badRequest(schoolClassEditForTeacher.render(schoolClass, teacher, "Subject was too long."));
		List<SchoolClass> schoolClassList = Utilities.createSchoolClassListForTeacher(teacher);
		for (SchoolClass schoolClass2 : schoolClassList) {
			if (schoolClass.id == schoolClass2.id) continue;
			if (subject.equals(schoolClass2.subject)) return badRequest(schoolClassEditForTeacher.render(schoolClass, teacher, "Can't have two classes with the same name."));
		}
		String color = filledForm.data().get("color");
		SchoolClass.edit(Long.valueOf(schoolClassID), subject, color, teacher.id, "");
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", ""));
	}

	public Result delete(String schoolClassID, String studentID) {
		if(session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		if (schoolClass == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClasses = Utilities.createSchoolClassesList(student);
		if (!schoolClasses.contains(schoolClass)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Unauthorized request."));

		if (schoolClass.teacherID != null) {
			schoolClass.students.remove(student);
			try {
				schoolClass.save();
			} catch (PersistenceException e) {
				return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));
			}
			List<Assignment> schoolClassAssignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
			List<Assignment> assignments = Assignment.find.where().eq("FOREIGN_ID", student.id).findList();
			for (int j = schoolClassAssignments.size() - 1; j >= 0; j--) {
				for (int i = assignments.size() - 1; i >= 0; i--) {
					if (Assignment.same(schoolClassAssignments.get(j), assignments.get(i))) {
						try {
							assignments.get(i).delete();
						} catch (PersistenceException e) {
							// Do nothing
						}
						assignments.remove(i);
					}
				}
			}
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
		} else {
			List<Assignment> assignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
			for (int i = assignments.size() - 1; i >= 0; i--) {
				try {
					assignments.get(i).delete();
				} catch (PersistenceException e) {
					// Do nothing
				}
				assignments.remove(i);
			}
			try {
				schoolClass.delete();
			} catch (PersistenceException e) {
				return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
			}
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
		}
	}

	public Result deleteForTeacher(String schoolClassID, String teacherID) {
		if(session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClasses = Utilities.createSchoolClassListForTeacher(teacher);
		if (!schoolClasses.contains(schoolClass)) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Unauthorized request."));

		List<Assignment> assignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
		for (int j = assignments.size() - 1; j >= 0; j--) {
			try {
				assignments.get(j).delete();
			} catch (PersistenceException e) {
				// Do nothing
			}
			assignments.remove(j);
		}
		try {
			schoolClass.delete();
		} catch (PersistenceException e) {
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", ""));
		}
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", ""));
	}

}
