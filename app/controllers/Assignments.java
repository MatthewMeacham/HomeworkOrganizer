package controllers;

import java.util.List;
import java.util.UUID;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.persistence.PersistenceException;

import play.data.Form;
import play.mvc.Result;
import play.mvc.Controller;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import com.google.common.io.Files;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;

import controllers.Application.Login;

import models.Assignment;
import models.SchoolClass;
import models.Student;
import models.Parent;
import models.Teacher;

import views.html.index;
import views.html.studentProfile;
import views.html.parentProfile;
import views.html.teacherProfile;
import views.html.assignmentEdit;
import views.html.assignmentEditForTeacher;
import views.html.assignmentEditForParent;
import views.html.unauthorizedError;

public class Assignments extends Controller {

	private static Form<Assignment> assignmentForm = Form.form(Assignment.class);
	private static Form<Login> loginForm = Form.form(Login.class);

	// Create a new assignment from the request
	public Result create(String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "addAssignment", "Error while processing."));
		} else {
			String description = filledForm.data().get("description");
			if (description.length() >= 250) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "addAssignment", "Description was too long."));
			try {
				Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), description, studentID);
			} catch (PersistenceException e) {
				return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "addAssignment", "Error while processing."));
			}
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
		}
	}

	public Result createForTeacher(String teacherID) {
		if (session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Error while processing."));
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassId"))).findUnique();
		if (schoolClass == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Error while processing."));
		String description = filledForm.data().get("description");
		if (description.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Description was too long."));
		try {
			Assignment.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("kindOfAssignment"), description, teacherID);
		} catch (PersistenceException e) {
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Error while processing."));
		}
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
	}

	// TODO need to do these
	public Result createNote(String studentID) {
		return TODO;
	}

	// Direct to the edit assignment page
	public Result read(String assignmentID, String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));

		return ok(assignmentEdit.render(student, assignment, Utilities.createSchoolClassesList(student), ""));
	}

	// Direct a parent to the edit assignment page
	public Result readForParent(Long assignmentID, UUID parentID, UUID studentID) {
		if (session("userID") == null || !session("userID").equals(parentID.toString())) return unauthorized(unauthorizedError.render());
		Parent parent = Parent.find.where().eq("ID", parentID).findUnique();
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		Assignment assignment = Assignment.find.where().eq("ID", assignmentID).findUnique();
		if (parent == null) return redirect(routes.Application.index());
		if (student == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		if (assignment == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));

		return ok(assignmentEditForParent.render(parent, student, assignment, Utilities.createSchoolClassesList(student), ""));
	}

	// Direct a teacher to the edit assignment page
	public Result readForTeacher(String assignmentID, String teacherID) {
		if (session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (assignment == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", "Error while processing."));

		return ok(assignmentEditForTeacher.render(teacher, assignment, Utilities.createSchoolClassListForTeacher(teacher), ""));
	}

	// Edit an assignment from a request
	// TODO make errors take them back to edit page
	public Result update(String assignmentID, String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		if (filledForm.hasErrors()) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));

		String description = filledForm.data().get("description");
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassID"))).findUnique();
		if (schoolClass == null) return badRequest(assignmentEdit.render(student, assignment, Utilities.createSchoolClassesList(student), "Error while processing."));
		if (description.length() >= 250) return badRequest(assignmentEdit.render(student, assignment, Utilities.createSchoolClassesList(student), "Description was too long."));
		try {
			Assignment.edit(Long.valueOf(assignmentID), SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassID"))).findUnique(), filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
		} catch (PersistenceException e) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
	}

	// Edit an assignment from a parent request
	// TODO make errors take them back to edit page
	public Result updateForParent(Long assignmentID, UUID parentID, UUID studentID) {
		if (session("userID") == null || !session("userID").equals(parentID.toString())) return unauthorized(unauthorizedError.render());
		Parent parent = Parent.find.where().eq("ID", parentID).findUnique();
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		Assignment assignment = Assignment.find.where().eq("ID", assignmentID).findUnique();
		if (parent == null) return redirect(routes.Application.index());
		if (student == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		if (assignment == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		Result result = this.update(String.valueOf(assignmentID), studentID.toString());
		if (result.status() == 400) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", ""));
	}

	// Edit an assignment from a teacher request
	// TODO make errors take them back to edit page
	public Result updateForTeacher(String assignmentID, String teacherID) {
		if (session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Form<Assignment> filledForm = assignmentForm.bindFromRequest();
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		if (filledForm.hasErrors()) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "", "Error while trying to change assignment."));
		String description = filledForm.data().get("description");
		if (description.length() >= 250) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "addAssignment", "Description was too long."));
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (assignment == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", "Error while processing."));

		List<Student> students = assignment.schoolClass.students;
		for (int i = 0; i < students.size(); i++) {
			List<Assignment> assignments = Utilities.createAssignmentsList(students.get(i));
			List<Assignment> finishedAssignments = Utilities.createFinishedAssignmentsList(students.get(i));
			for (int j = 0; j < assignments.size(); j++) {
				if (Assignment.same(assignments.get(j), assignment)) {
					try {
						assignments.get(j).delete();
					} catch (PersistenceException e) {
						// Do nothing
					}
				}
			}
			for (int j = finishedAssignments.size() - 1; j >= 0; j--) {
				if (Assignment.same(finishedAssignments.get(j), assignment)) {
					try {
						finishedAssignments.get(j).delete();
					} catch (PersistenceException e) {
						// Do nothing
					}
				}
			}
		}

		SchoolClass schoolClass = SchoolClass.find.where().eq("ID", Long.valueOf(filledForm.data().get("schoolClassID"))).findUnique();
		try {
			Assignment.edit(Long.valueOf(assignmentID), schoolClass, filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
		} catch (PersistenceException e) {
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", "Error while processing."));
		}
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
	}

	// Delete an assignment
	public Result delete(String assignmentID, String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));

		try {
			assignment.delete();
		} catch (PersistenceException e) {
			// Do nothing
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
	}

	public Result deleteForParent(Long assignmentID, UUID parentID, UUID studentID) {
		if (session("userID") == null || !session("userID").equals(parentID.toString())) return unauthorized(unauthorizedError.render());
		Parent parent = Parent.find.where().eq("ID", parentID).findUnique();
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		Assignment assignment = Assignment.find.where().eq("ID", assignmentID).findUnique();
		if (parent == null) return redirect(routes.Application.index());
		if (student == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		if (assignment == null) return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		
		try {
			assignment.delete();
		} catch (PersistenceException e) {
			// Do nothing
		}
		return ok(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", ""));
	}

	// Delete an assignment for teacher
	public Result deleteForTeacher(String assignmentID, String teacherID) {
		if (session("userID") == null || !session("userID").equals(teacherID)) return unauthorized(unauthorizedError.render());
		Teacher teacher = Teacher.find.where().eq("ID", UUID.fromString(teacherID)).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (assignment == null) return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", "Error while processing."));

		List<Student> students = assignment.schoolClass.students;
		for (int i = 0; i < students.size(); i++) {
			List<Assignment> assignments = Utilities.createAssignmentsList(students.get(i));
			for (int j = 0; j < assignments.size(); j++) {
				Assignment studentAssignment = assignments.get(j);
				if (studentAssignment.dueDate.equals(assignment.dueDate) && studentAssignment.kindOfAssignment.equals(assignment.kindOfAssignment) && studentAssignment.description.equals(assignment.description)) {
					try {
						studentAssignment.delete();
					} catch (PersistenceException e) {
						// Do nothing
					}
					break;
				}
			}
		}
		try {
			assignment.delete();
		} catch (PersistenceException e) {
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
		}
		return ok(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
	}

	// Delete a late assignment
	public Result deleteLate(String assignmentID, String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		Assignment lateAssignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (lateAssignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "lateAssignments", "Error while processing."));
		try {
			lateAssignment.delete();
		} catch (PersistenceException e) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "lateAssignments", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "lateAssignments", ""));
	}

	// Delete a finished assignment
	public Result deleteFinished(String assignmentID, String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		Assignment finishedAssignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (finishedAssignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", "Error while processing."));
		try {
			finishedAssignment.delete();
		} catch (PersistenceException e) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
	}

	public Result deleteAllFinished(String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		List<Assignment> finishedAssignments = Utilities.createFinishedAssignmentsList(student);
		for (int i = finishedAssignments.size() - 1; i >= 0; i--) {
			if (finishedAssignments.get(i).schoolClass.teacherID != null) continue;
			deleteFinished(String.valueOf(finishedAssignments.get(i).id), studentID);
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));

	}

	// Set the finished value of an assignment to true
	public Result setFinished(String assignmentID, String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		assignment.finished = true;
		try {
			assignment.save();
		} catch (PersistenceException e) {
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "finishedAssignments", ""));
	}

	// Set the finished assignment back to unfinished
	public Result setUnfinished(String assignmentID, String studentID) {
		if (session("userID") == null || !session("userID").equals(studentID)) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		Assignment assignment = Assignment.find.where().eq("ID", Long.valueOf(assignmentID)).findUnique();
		if (student == null) return redirect(routes.Application.index());
		if (assignment == null) return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		assignment.finished = false;
		try {
			assignment.save();
		} catch (PersistenceException e) {
			return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
		}
		return ok(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", ""));
	}

	private float margin = 10;

	// Create a PDF of the assignments and deliver it to webpage
	public Result createPrintableDocument(UUID studentID) {
		if (session("userID") == null || !session("userID").equals(studentID.toString())) return unauthorized(unauthorizedError.render());
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		if (student == null) return redirect(routes.Application.index());
		List<Assignment> assignments = Utilities.createAssignmentsList(student);

		try {
			PDDocument document = new PDDocument();
			PDPage page = new PDPage();
			document.addPage(page);
			float yStartNewPage = page.findMediaBox().getHeight() - (2 * margin);
			float tableWidth = page.findMediaBox().getWidth() - (2 * margin);
			boolean drawContent = true;
			float yStart = yStartNewPage;
			float bottomMargin = 70;

			BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, document, page, true, drawContent);

			Row headerRow = table.createRow(15f);
			Cell cell = headerRow.createCell(100, student.name + "\'s" + " Assignments");
			cell.setFont(PDType1Font.HELVETICA_BOLD);
			cell.setFillColor(Color.BLACK);
			cell.setTextColor(Color.WHITE);

			table.setHeader(headerRow);

			Row columnNameRow = table.createRow(15f);

			cell = columnNameRow.createCell((100 / 5) * 1.0f, "Due Date");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 5) * 1.0f, "Class");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 5) * 1.0f, "Kind");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 5) * 2.0f, "Description");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			Row row = table.createRow(15f);
			for (Assignment assignment : assignments) {
				row = table.createRow(10f);
				cell = row.createCell((100 / 5) * 1.0f, assignment.month + "/" + assignment.day + "/" + assignment.year);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 5) * 1.0f, assignment.schoolClass.subject);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 5 * 1.0f), assignment.kindOfAssignment);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 5) * 2.0f, assignment.description);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);
			}

			table.draw();

			// File file = new File(student.name + "'s_assignments.pdf");
			File file = File.createTempFile(student.name + "'s_assignments", ".pdf");
			document.save(file);
			document.close();
			return ok(file);

		} catch (IOException | COSVisitorException e) {
			return badRequest(studentProfile.render(student, Utilities.createSchoolClassesList(student), Utilities.createAssignmentsList(student), Utilities.createFinishedAssignmentsList(student), Utilities.createLateAssignmentsList(student), Utilities.createTeachersList(student), Utilities.createNotesList(student), Utilities.today, "overview", "Error while processing."));
		}
	}

	public Result createPrintableDocumentForParent(UUID parentID) {
		if (session("userID") == null || !session("userID").equals(parentID.toString())) return unauthorized(unauthorizedError.render());
		Parent parent = Parent.find.where().eq("ID", parentID).findUnique();
		if (parent == null) return redirect(routes.Application.index());
		List<Assignment> assignments = Utilities.createAssignmentsListForParent(parent);

		try {
			PDDocument document = new PDDocument();
			PDPage page = new PDPage();
			document.addPage(page);
			float yStartNewPage = page.findMediaBox().getHeight() - (2 * margin);
			float tableWidth = page.findMediaBox().getWidth() - (2 * margin);
			boolean drawContent = true;
			float yStart = yStartNewPage;
			float bottomMargin = 70;

			BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, document, page, true, drawContent);

			Row headerRow = table.createRow(15f);
			List<Student> children = Utilities.createChildrenList(parent);
			Cell cell = null;
			if (children.size() > 1) {
				cell = headerRow.createCell(100, "Assignments List for " + parent.name + "\'s" + " Kids");
			} else {
				cell = headerRow.createCell(100, "Assignments List for " + parent.name + "\'s" + " Kid");
			}
			cell.setFont(PDType1Font.HELVETICA_BOLD);
			cell.setFillColor(Color.BLACK);
			cell.setTextColor(Color.WHITE);

			table.setHeader(headerRow);

			Row columnNameRow = table.createRow(15f);

			cell = columnNameRow.createCell((100 / 6) * 1.0f, "Due Date");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 6) * 1.0f, "Child");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 6) * 1.0f, "Class");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 6) * 1.0f, "Kind");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 6) * 2.0f, "Description");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			Row row = table.createRow(15f);
			for (Assignment assignment : assignments) {
				row = table.createRow(10f);
				cell = row.createCell((100 / 6) * 1.0f, assignment.month + "/" + assignment.day + "/" + assignment.year);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				try {
					cell = row.createCell((100 / 6) * 1.0f, Student.find.where().eq("ID", assignment.foreignID).findUnique().name);
				} catch (PersistenceException e) {
					cell = row.createCell((100 / 6) * 1.0f, "");
				}
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 6) * 1.0f, assignment.schoolClass.subject);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 6 * 1.0f), assignment.kindOfAssignment);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 6) * 2.0f, assignment.description);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);
			}

			table.draw();

			// File file = new File("Assignments_List.pdf");

			File file = File.createTempFile("Assignments_List", ".pdf");
			document.save(file);
			document.close();
			return ok(file);

		} catch (IOException | COSVisitorException e) {
			return badRequest(parentProfile.render(parent, Utilities.createChildrenList(parent), Utilities.createAssignmentsListForParent(parent), Utilities.today, "overview", "Error while processing."));
		}
	}

	public Result createPrintableDocumentForTeacher(UUID teacherID) {
		if (session("userID") == null || !session("userID").equals(teacherID.toString())) return unauthorized(unauthorizedError.render());
		Teacher teacher = Teacher.find.where().eq("ID", teacherID).findUnique();
		if (teacher == null) return redirect(routes.Application.index());
		List<Assignment> assignments = Utilities.createAssignmentsListForTeacher(teacher);

		try {
			PDDocument document = new PDDocument();
			PDPage page = new PDPage();
			document.addPage(page);
			float yStartNewPage = page.findMediaBox().getHeight() - (2 * margin);
			float tableWidth = page.findMediaBox().getWidth() - (2 * margin);
			boolean drawContent = true;
			float yStart = yStartNewPage;
			float bottomMargin = 70;

			BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, document, page, true, drawContent);

			Row headerRow = table.createRow(15f);

			Cell cell = headerRow.createCell(100, "Assignments List for " + teacher.name);
			cell.setFont(PDType1Font.HELVETICA_BOLD);
			cell.setFillColor(Color.BLACK);
			cell.setTextColor(Color.WHITE);

			table.setHeader(headerRow);

			Row columnNameRow = table.createRow(15f);

			// Duedate class kindof description

			cell = columnNameRow.createCell((100 / 5) * 1.0f, "Due Date");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 5) * 1.0f, "Class");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 5) * 1.0f, "Kind");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = columnNameRow.createCell((100 / 5) * 2.0f, "Description");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			Row row = table.createRow(15f);
			for (Assignment assignment : assignments) {
				row = table.createRow(10f);
				cell = row.createCell((100 / 5) * 1.0f, assignment.month + "/" + assignment.day + "/" + assignment.year);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 5) * 1.0f, assignment.schoolClass.subject);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 5 * 1.0f), assignment.kindOfAssignment);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);

				cell = row.createCell((100 / 5) * 2.0f, assignment.description);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(6);
			}

			table.draw();

			// File file = new File("Assignments_List.pdf");
			File file = File.createTempFile("Assignments_List", ".pdf");
			document.save(file);
			document.close();
			return ok(file);

		} catch (IOException | COSVisitorException e) {
			return badRequest(teacherProfile.render(teacher, Utilities.createAssignmentsListForTeacher(teacher), Utilities.createSchoolClassListForTeacher(teacher), Utilities.today, "overview", ""));
		}
	}

}
