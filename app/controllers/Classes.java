package controllers;

import java.util.List;
import java.util.UUID;

import javax.persistence.PersistenceException;

import controllers.Application.Login;
import controllers.Application.SchoolClassFromCode;
import models.Assignment;
import models.Parent;
import models.SchoolClass;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.studentProfile;
import views.html.teacherProfile;

public class Classes extends Controller {

	private static Form<SchoolClass> schoolClassForm = Form.form(SchoolClass.class);
	private static Form<Login> loginForm = Form.form(Login.class);
	private static Form<SchoolClassFromCode> schoolClassFromCodeForm = Form.form(SchoolClassFromCode.class);

	// Create a new school class from the request
	public Result create(String studentID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Student student = Student.find.ref(UUID.fromString(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));

		List<SchoolClass> schoolClassList = Utilities.createSchoolClassesList(student);
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty() || subject.equals("")) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Class name can not be just spaces."));
		if (subject.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Class name was too long."));
		for (SchoolClass schoolClass : schoolClassList) {
			if (subject.equals(schoolClass.subject)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Can't have two classes with the same name."));
		}
		SchoolClass.create(subject, student.email, UUID.fromString(studentID), filledForm.data().get("color"), "");
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
	}

	// Creates a new school class for the teacher from the request
	public Result createForTeacher(String teacherID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		Teacher teacher = Teacher.find.ref(UUID.fromString(teacherID));
		if (teacher == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(views.html.teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Error while processing."));
		List<SchoolClass> schoolClassList = Utilities.createSchoolClassListForTeacher(teacher);
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty() || subject.equals("")) return badRequest(views.html.teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Class name can't be spaces."));
		if (subject.length() >= 250) return badRequest(views.html.teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Class name was too long."));
		for (SchoolClass schoolClass : schoolClassList) {
			if (subject.equals(schoolClass.subject)) return badRequest(views.html.teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Can't have two classes with the same name."));
		}
		SchoolClass.create(subject, teacher.email, UUID.fromString(teacherID), filledForm.data().get("color"), filledForm.data().get("password"));
		return ok(views.html.teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", ""));
	}

	// Create a new school class from a teacher provided id and password
	public Result createFromTeacher(String studentID) {
		Form<SchoolClassFromCode> filledForm = schoolClassFromCodeForm.bindFromRequest();
		Student student = Student.find.ref(UUID.fromString(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));

		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassID"))).findUnique();
		if (schoolClass == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Invalid class ID."));
		if (!schoolClass.password.equals(filledForm.data().get("password"))) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Password was incorrect."));
		List<SchoolClass> schoolClasses = Utilities.createSchoolClassesList(student);
		for (SchoolClass schoolClassIterated : schoolClasses) {
			if (schoolClassIterated.subject.equals(schoolClass.subject)) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Can't have two classes with the same name."));
		}
		schoolClass.students.add(Student.find.ref(UUID.fromString(studentID)));
		schoolClass.save();

		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
	}

	// Direct to the edit school class page
	public Result read(String schoolClassID, String studentID) {
		SchoolClass schoolClass = SchoolClass.find.ref(Long.valueOf(schoolClassID));
		Student student = Student.find.ref(UUID.fromString(studentID));
		return ok(views.html.schoolClassEdit.render(schoolClass, student, ""));
	}

	// Direct to the edit school class page for teacher
	public Result readForTeacher(String schoolClassID, String teacherID) {
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		return ok(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, ""));
	}

	// Edit a schoolClass from a request
	public Result update(String schoolClassID, String studentID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		SchoolClass schoolClass = SchoolClass.find.ref(Long.valueOf(schoolClassID));
		Student student = Student.find.ref(UUID.fromString(studentID));
		if (student == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) {
			return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Error while processing."));
		} else {
			String subject = filledForm.data().get("subject");
			if (subject.trim().isEmpty() || subject.equals("")) return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Subject can not be empty."));
			if (subject.length() >= 250) return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Subject was too long."));
			List<SchoolClass> schoolClassList = Utilities.createSchoolClassesList(student);
			for (SchoolClass schoolClass2 : schoolClassList) {
				if (schoolClass.id == schoolClass2.id) continue;
				if (subject.equals(schoolClass2.subject)) return badRequest(views.html.schoolClassEdit.render(schoolClass, student, "Can't have two classes with the same name."));
			}
			String color = filledForm.data().get("color");
			SchoolClass.edit(Long.valueOf(schoolClassID), subject, color, student.id, "");
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
		}
	}

	// Edit a schoolClass for a teacher from a request
	public Result updateForTeacher(String schoolClassID, String teacherID) {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return badRequest(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
		if (filledForm.hasErrors()) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Error while processing."));
		String subject = filledForm.data().get("subject");
		if (subject.trim().isEmpty() || subject.equals("")) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Subject can not be empty."));
		if (subject.length() >= 250) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Subject was too long."));
		List<SchoolClass> schoolClassList = Utilities.createSchoolClassListForTeacher(teacher);
		for (SchoolClass schoolClass2 : schoolClassList) {
			if (schoolClass.id == schoolClass2.id) continue;
			if (subject.equals(schoolClass2.subject)) return badRequest(views.html.schoolClassEditForTeacher.render(schoolClass, teacher, "Can't have two classes with the same name."));
		}
		String color = filledForm.data().get("color");
		SchoolClass.edit(Long.valueOf(schoolClassID), subject, color, teacher.id, "");
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", ""));
	}

	public Result delete(String schoolClassID, String studentID) {
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (schoolClass == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", "Error while processing."));
		if (schoolClass.teacherID != null) {
			schoolClass.students.remove(student);
			schoolClass.save();
			List<Assignment> schoolClassAssignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
			List<Assignment> assignments = Assignment.find.where().eq("FOREIGN_ID", UUID.fromString(studentID)).findList();
			for (int j = schoolClassAssignments.size() - 1; j >= 0; j--) {
				for (int i = assignments.size() - 1; i >= 0; i--) {
					if (Assignment.same(schoolClassAssignments.get(j), assignments.get(i))) {
						assignments.get(i).delete();
						assignments.remove(i);
					}
				}
			}
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
		} else {
			List<Assignment> assignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
			for (int i = assignments.size() - 1; i >= 0; i--) {
				assignments.get(i).delete();
				assignments.remove(i);
			}
			try {
				schoolClass.delete();
			} catch (PersistenceException e) {
				return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
			}
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "schoolClasses", ""));
		}
	}

	public Result deleteForTeacher(String schoolClassID, String teacherID) {
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(schoolClassID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "schoolClasses", "Error while processing."));
		List<Assignment> assignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClass.id).findList();
		for (int j = assignments.size() - 1; j >= 0; j--) {
			assignments.get(j).delete();
			assignments.remove(j);
		}
		try {
			schoolClass.delete();
		} catch (PersistenceException e) {
			return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "", ""));
		}
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "", ""));
	}

}