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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
			Assignment.edit(Long.parseLong(assignmentID), SchoolClass.find.where().eq("ID", Long.parseLong(filledForm.data().get("schoolClassID"))).findUnique(), filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
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
					if (Assignment.same(assignments.get(j), assignment)) Assignment.edit(assignments.get(j).id, SchoolClass.find.where().eq("ID", Long.parseLong(filledForm.data().get("schoolClassID"))).findUnique(), filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
				}
			}
		}
		Assignment.edit(Long.parseLong(assignmentID), SchoolClass.find.where().eq("ID", Long.parseLong(filledForm.data().get("schoolClassID"))).findUnique(), filledForm.data().get("dueDate"), filledForm.data().get("kindOfAssignment"), filledForm.data().get("description"));
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

	private float margin = 10;

	// Create a PDF of the assignments and deliver it to webpage
	public Result createPrintableDocument(UUID studentID) {
		Student student = Student.find.where().eq("ID", studentID).findUnique();
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

			// Create Fact header row
			Row factHeaderrow = table.createRow(15f);

			cell = factHeaderrow.createCell((100 / 5), "Due Date");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = factHeaderrow.createCell((100 / 5), "Class");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = factHeaderrow.createCell((100 / 5), "Kind");
			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(6);
			cell.setFillColor(Color.LIGHT_GRAY);

			cell = factHeaderrow.createCell((100 / 5) * 2, "Description");
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

			File file = new File("temp/assignments.pdf");
			Files.createParentDirs(file);
			document.save(file);
			document.close();
			return ok(file);

		} catch (IOException | COSVisitorException e) {
			return badRequest();
		}
	}

}
