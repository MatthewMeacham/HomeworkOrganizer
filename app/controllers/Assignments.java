package controllers;

import java.util.List;
import java.util.UUID;

import javax.persistence.PersistenceException;

import models.Assignment;
import models.SchoolClass;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Result;
import views.html.studentProfile;
import views.html.teacherProfile;
import play.mvc.Controller;

public class Assignments extends Controller {

	private static Form<Assignment> assignmentForm = Form.form(Assignment.class);

	// Create a new assignment from the request
	public Result create(String studentID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (filledForm.hasErrors()) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "addAssignment", "Error while processing."));
		} else {
			String description = filledForm.data().get("description");
			if (description.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "addAssignment", "Description was too long."));
			Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), description, studentID);
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
		}
	}

	public Result createForTeacher(String teacherID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (filledForm.hasErrors()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Error while processing."));
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassId"))).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Error while processing."));
		String description = filledForm.data().get("description");
		if (description.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Description was too long."));
		Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), description, teacherID);
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));

	}

	// TODO need to do these
	public Result createNote(String studentID) {
		return TODO;
	}

	// Direct to the edit assignment page
	public Result read(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		
		return ok(views.html.assignmentEdit.render(student, assignment, Utilities.createSchoolClassesList(student), ""));
	}

	// Direct a teacher to the edit assignment page
	public Result readForTeacher(String assignmentID, String teacherID) {
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		return ok(views.html.assignmentEditForTeacher.render(teacher, assignment, Utilities.createSchoolClassListForTeacher(teacher), ""));
	}

	// Edit an assignment from a request
	// TODO make errors take them back to edit page
	public Result update(String assignmentID, String studentID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (filledForm.hasErrors()) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		} else {
			String description = filledForm.data().get("description");
			if (description.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Description was too long."));
			Assignment.edit(Long.parseLong(assignmentID), SchoolClass.find.where().eq("ID", Long.parseLong(filledForm.data().get("schoolClassID")));, filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
		}
	}

	// Edit an assignment from a teacher request
	// TODO make errors take them back to edit page
	public Result updateForTeacher(String assignmentID, String teacherID) {
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (filledForm.hasErrors()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "", "Error while trying to change assignment."));
		String description = filledForm.data().get("description");
		if (description.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Description was too long."));
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		List<Student> students = assignment.schoolClass.students;
		for (int i = 0; i < students.size(); i++) {
			List<Assignment> assignments = Utilities.createAssignmentsList(students.get(i));
			for (int j = 0; j < assignments.size(); j++) {
				if (assignments.get(j).schoolClass.id == assignment.schoolClass.id) {
					if (Assignment.same(assignments.get(j), assignment)) Assignment.edit(assignments.get(j).id, SchoolClass.find.where().eq("ID", Long.parseLong(filledForm.data().get("schoolClassID")));, filledform.data().get("dueDate"), filledform.data().get("kindOfAssignment"), filledform.data().get("description"));
				}
			}
		}
		Assignment.edit(Long.parseLong(assignmentID), SchoolClass.find.where().eq("ID", Long.parseLong(filledForm.data().get("schoolClassID")));, filledform.data().get("dueDate"), filledform.data().get("kindOfAssignment"), filledform.data().get("description"));
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
	}

	// Delete an assignment
	public Result delete(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		try {
			assignment.delete();
		} catch (PersistenceException e) {
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
	}

	// Delete an assignment for teacher
	public Result deleteForTeacher(String assignmentID, String teacherID) {
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (assignment == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", "Error while processing."));
		List<Student> students = assignment.schoolClass.students;
		for (int i = 0; i < students.size(); i++) {
			List<Assignment> assignments = Utilities.createAssignmentsList(students.get(i));
			for (int j = 0; j < assignments.size(); j++) {
				Assignment studentAssignment = assignments.get(j);
				if (studentAssignment.dueDate.equals(assignment.dueDate) && studentAssignment.kindOfAssignment.equals(assignment.kindOfAssignment) && studentAssignment.description.equals(assignment.description)) {
					studentAssignment.delete();
					break;
				}
			}
		}
		try {
			assignment.delete();
		} catch (PersistenceException e) {
			return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
		}
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
	}

	// Delete a late assignment
	public Result deleteLate(String assignmentID, String studentID) {
		Assignment lateAssignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (lateAssignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "lateAssignments", "Error while processing."));
		try {
			lateAssignment.delete();
		} catch (PersistenceException e) {
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "lateAssignments", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "lateAssignments", ""));
	}

	// Delete a finished assignment
	public Result deleteFinished(String assignmentID, String studentID) {
		Assignment finishedAssignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (finishedAssignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", "Error while processing."));
		try {
			finishedAssignment.delete();
		} catch (PersistenceException e) {
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
	}

	// Set the finished value of an assignment to true
	public Result setFinished(String assignmentID, String studentID) {
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		assignment.finished = true;
		try {
			assignment.save();
		} catch (PersistenceException e) {
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
	}

}
