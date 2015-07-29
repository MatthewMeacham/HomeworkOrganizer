package controllers;

import static org.junit.Assert.*;
import static play.test.Helpers.contentAsString;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import models.Assignment;
import models.Parent;
import models.SchoolClass;
import models.Student;
import models.Teacher;

import org.junit.Test;

import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;

public class AssignmentsTest extends BaseControllerTest {

	private Assignments assignments = new Assignments();
	private Helpers helpers = new Helpers();

	@Test
	public void createTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");

		Map<String, String> data = new HashMap<String, String>();
		data.put("dueDate", "2015-08-06");
		data.put("description", "My first homework assignment.");
		data.put("schoolClassId", schoolClass.id.toString());
		data.put("kindOfAssignment", "Homework");

		// Java 8 lambda expression, ignore the error (if you're using eclipse), it's fine.
		Result result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.create(student.id.toString()));

		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("My first homework assignment."));
		assertTrue(contentAsString(result).contains("August 06, 2015"));

		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.create("74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(303, result.status());
	}

	@Test
	public void createForTeacherTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		SchoolClass schoolClass = SchoolClass.create("Math YAY", teacher.email, teacher.id, "#F2A4F2", "math");

		Map<String, String> data = new HashMap<String, String>();
		data.put("dueDate", "2015-09-06");
		data.put("description", "This is a final by a teacher.");
		data.put("schoolClassId", schoolClass.id.toString());
		data.put("kindOfAssignment", "Final");

		Result result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.createForTeacher(teacher.id.toString()));

		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("This is a final by a teacher."));
		assertTrue(contentAsString(result).contains("September 06, 2015"));

		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.createForTeacher("74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(303, result.status());
	}

	@Test
	public void readTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-08-06", schoolClass.id.toString(), "Homework", "Another homework assignment.", student.id.toString());

		Result result = assignments.read(assignment.id.toString(), student.id.toString());

		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Another homework assignment."));
		assertTrue(contentAsString(result).contains("2015"));
		assertTrue(contentAsString(result).contains("08"));
		assertTrue(contentAsString(result).contains("06"));
		assertTrue(contentAsString(result).contains("Homework"));

		result = assignments.read("5432412", student.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));

		result = assignments.read(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
	}

	@Test
	public void readForParentTest() {
		Parent parent = Parent.create("Jeanette", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");

		Student student = Student.create("Matthew", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2016-02-20", schoolClass.id.toString(), "Presentation", "My first presentation.", student.id.toString());

		Result result = assignments.readForParent(assignment.id, parent.id, student.id);

		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("My first presentation."));
		assertTrue(contentAsString(result).contains("2016"));
		assertTrue(contentAsString(result).contains("02"));
		assertTrue(contentAsString(result).contains("20"));
		assertTrue(contentAsString(result).contains("Presentation"));

		result = assignments.readForParent(504839292L, parent.id, student.id);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));

		result = assignments.readForParent(assignment.id, UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"), student.id);
		assertEquals(303, result.status());

		result = assignments.readForParent(assignment.id, parent.id, UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));
	}

	@Test
	public void readForTeacherTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		SchoolClass schoolClass = SchoolClass.create("Math YAY", teacher.email, teacher.id, "#F2A4F2", "math");
		Assignment assignment = Assignment.create("2015-05-28", schoolClass.id.toString(), "Other", "An other assignment, not another.", teacher.id.toString());

		Result result = assignments.readForTeacher(assignment.id.toString(), teacher.id.toString());

		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("An other assignment, not another."));
		assertTrue(contentAsString(result).contains("2015"));
		assertTrue(contentAsString(result).contains("05"));
		assertTrue(contentAsString(result).contains("28"));
		assertTrue(contentAsString(result).contains("Other"));

		result = assignments.readForTeacher("5398249821", teacher.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));

		result = assignments.readForTeacher(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
	}

	@Test
	public void updateTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		SchoolClass newSchoolClass = SchoolClass.create("English", student.email, student.id, "F2A4F2", "");
		Assignment assignment = Assignment.create("2015-08-06", schoolClass.id.toString(), "Homework", "My homework assignment.", student.id.toString());

		Map<String, String> data = new HashMap<String, String>();
		data.put("dueDate", "2015-08-06");
		data.put("description", "My homework assignment.");
		data.put("schoolClassID", schoolClass.id.toString());
		data.put("kindOfAssignment", "Homework");

		Result result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("August 06, 2015"));
		assertTrue(contentAsString(result).contains("My homework assignment."));
		assertTrue(contentAsString(result).contains("Math"));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));

		data.put("dueDate", "2016-08-06");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("August 06, 2016"));
		assertTrue(contentAsString(result).contains("My homework assignment."));
		assertTrue(contentAsString(result).contains("Math"));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));

		data.put("description", "My homework assignment edited.");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("August 06, 2016"));
		assertTrue(contentAsString(result).contains("My homework assignment edited."));
		assertTrue(contentAsString(result).contains("Math"));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));

		data.put("schoolClassID", newSchoolClass.id.toString());
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("August 06, 2016"));
		assertTrue(contentAsString(result).contains("My homework assignment edited."));
		assertTrue(contentAsString(result).contains("English"));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));

		data.put("kindOfAssignment", "Final");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("August 06, 2016"));
		assertTrue(contentAsString(result).contains("My homework assignment edited."));
		assertTrue(contentAsString(result).contains("English"));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));

		data.put("dueDate", "2015-12-25");
		data.put("description", "This is due on Christmas?!");
		data.put("schoolClassID", schoolClass.id.toString());
		data.put("kindOfAssignment", "Presentation");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("December 25, 2015"));
		assertTrue(contentAsString(result).contains("This is due on Christmas?!"));
		assertTrue(contentAsString(result).contains("Math"));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));

		data.put("schoolClassID", "42914213");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertFalse(contentAsString(result).contains("Logged In As Matthew"));

		data.put("schoolClassID", newSchoolClass.id.toString());
		String description = "";
		for (int i = 0; i < 251; i++) {
			description += "m";
		}
		data.put("description", description);
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), student.id.toString()));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Description was too long."));
		assertFalse(contentAsString(result).contains("Logged In As Matthew"));

		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update("3219421", student.id.toString()));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertTrue(contentAsString(result).contains("December 25, 2015"));
		assertTrue(contentAsString(result).contains("This is due on Christmas?!"));

		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.update(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(303, result.status());
	}

	@Test
	public void updateForParentTest() {
		Parent parent = Parent.create("Jeanette", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");
		Student student = Student.create("Matthew", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		SchoolClass newSchoolClass = SchoolClass.create("English", student.email, student.id, "F2A4F2", "");
		Assignment assignment = Assignment.create("2016-02-20", schoolClass.id.toString(), "Presentation", "My first presentation.", student.id.toString());

		Map<String, String> data = new HashMap<String, String>();
		data.put("dueDate", "2015-08-06");
		data.put("description", "My homework assignment.");
		data.put("schoolClassID", schoolClass.id.toString());
		data.put("kindOfAssignment", "Homework");

		Result result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(assignment.id, parent.id, student.id));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("August 06, 2015"));
		assertTrue(contentAsString(result).contains("My homework assignment."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));

		data.put("dueDate", "2016-02-24");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(assignment.id, parent.id, student.id));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("February 24, 2016"));
		assertTrue(contentAsString(result).contains("My homework assignment."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));

		data.put("description", "I have edited this homework assignment, sweetie.");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(assignment.id, parent.id, student.id));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("February 24, 2016"));
		assertTrue(contentAsString(result).contains("I have edited this homework assignment, sweetie."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));

		data.put("schoolClassID", newSchoolClass.id.toString());
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(assignment.id, parent.id, student.id));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("February 24, 2016"));
		assertTrue(contentAsString(result).contains("I have edited this homework assignment, sweetie."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));

		data.put("kindOfAssignment", "Final");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(assignment.id, parent.id, student.id));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("February 24, 2016"));
		assertTrue(contentAsString(result).contains("I have edited this homework assignment, sweetie."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));

		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(assignment.id, UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"), student.id));
		assertEquals(303, result.status());

		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(assignment.id, parent.id, UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae")));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("February 24, 2016"));
		assertTrue(contentAsString(result).contains("I have edited this homework assignment, sweetie."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));

		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForParent(48218421L, parent.id, student.id));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));
	}

	@Test
	public void updateForTeacherTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		SchoolClass schoolClass = SchoolClass.create("Math YAY", teacher.email, teacher.id, "#F2A4F2", "math");
		SchoolClass newSchoolClass = SchoolClass.create("English", teacher.email, teacher.id, "DD25FA", "");
		Assignment assignment = Assignment.create("2015-05-28", schoolClass.id.toString(), "Other", "An other assignment, not another.", teacher.id.toString());

		Map<String, String> data = new HashMap<String, String>();
		data.put("dueDate", "2015-08-06");
		data.put("description", "My homework assignment.");
		data.put("schoolClassID", schoolClass.id.toString());
		data.put("kindOfAssignment", "Homework");

		Result result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForTeacher(assignment.id.toString(), teacher.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("August 06, 2015"));
		assertTrue(contentAsString(result).contains("My homework assignment."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));

		data.put("dueDate", "2015-07-28");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForTeacher(assignment.id.toString(), teacher.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("July 28, 2015"));
		assertTrue(contentAsString(result).contains("My homework assignment."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));

		data.put("description", "This assignment has been edited.");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForTeacher(assignment.id.toString(), teacher.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("July 28, 2015"));
		assertTrue(contentAsString(result).contains("This assignment has been edited."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));
		
		data.put("schoolClassID", newSchoolClass.id.toString());
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForTeacher(assignment.id.toString(), teacher.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("July 28, 2015"));
		assertTrue(contentAsString(result).contains("This assignment has been edited."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));
		
		data.put("kindOfAssignment", "Presentation");
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForTeacher(assignment.id.toString(), teacher.id.toString()));
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("July 28, 2015"));
		assertTrue(contentAsString(result).contains("This assignment has been edited."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));
				
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForTeacher(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(303, result.status());
		
		result = helpers.invokeWithContext(Helpers.fakeRequest().bodyForm(data), () -> assignments.updateForTeacher("428794712", teacher.id.toString()));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("July 28, 2015"));
		assertTrue(contentAsString(result).contains("This assignment has been edited."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));
	}
	
	@Test
	public void deleteTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-08-06", schoolClass.id.toString(), "Homework", "My homework assignment.", student.id.toString());
		
		Result result = assignments.delete(assignment.id.toString(), student.id.toString());
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertFalse(contentAsString(result).contains("Error while processing."));
		assertFalse(contentAsString(result).contains("My homework assignment."));
		
		result = assignments.delete(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
		
		result = assignments.delete("24892418241", student.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
	}
	
	@Test
	public void deleteForParentTest() {
		Parent parent = Parent.create("Jeanette", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");
		Student student = Student.create("Matthew", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2016-02-20", schoolClass.id.toString(), "Presentation", "My first presentation.", student.id.toString());
		
		Result result = assignments.deleteForParent(assignment.id, parent.id, student.id);
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));
		assertFalse(contentAsString(result).contains("My first presentation."));
		
		result = assignments.deleteForParent(24890180942L, parent.id, student.id);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));
		
		result = assignments.deleteForParent(assignment.id, UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"), student.id);
		assertEquals(303, result.status());
		
		result = assignments.deleteForParent(assignment.id, parent.id, UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Jeanette"));
	}
	
	@Test
	public void deleteForTeacherTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		SchoolClass schoolClass = SchoolClass.create("Math YAY", teacher.email, teacher.id, "#F2A4F2", "math");
		Assignment assignment = Assignment.create("2015-05-28", schoolClass.id.toString(), "Other", "An other assignment, not another.", teacher.id.toString());
		
		Result result = assignments.deleteForTeacher(assignment.id.toString(), teacher.id.toString());
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Tom"));
		assertFalse(contentAsString(result).contains("An other assignment, not another."));
		assertFalse(contentAsString(result).contains("Error while processing."));
		
		result = assignments.deleteForTeacher("42984921", teacher.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Error while processing."));
		assertTrue(contentAsString(result).contains("Logged In As Tom"));
		
		result = assignments.deleteForTeacher(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
	}
	
	@Test
	public void deleteLateTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-02-02", schoolClass.id.toString(), "Homework", "My homework assignment.", student.id.toString());
		
		Result result = assignments.deleteLate(assignment.id.toString(), student.id.toString());
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertFalse(contentAsString(result).contains("My homework assignment."));
		assertFalse(contentAsString(result).contains("Error while processing."));
		
		result = assignments.deleteLate("4208421241", student.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertTrue(contentAsString(result).contains("Error while processing."));
		
		result = assignments.deleteLate(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
	}
	
	@Test
	public void deleteFinishedTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-02-02", schoolClass.id.toString(), "Homework", "My homework assignment.", student.id.toString());
		
		Result result = assignments.deleteFinished(assignment.id.toString(), student.id.toString());
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertFalse(contentAsString(result).contains("My homework assignment."));
		assertFalse(contentAsString(result).contains("Error while processing."));
		
		result = assignments.deleteFinished("4218498120", student.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertTrue(contentAsString(result).contains("Error while processing."));
		
		result = assignments.deleteFinished(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
	}
	
	@Test
	public void setFinishedTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-02-02", schoolClass.id.toString(), "Homework", "My homework assignment.", student.id.toString());
		
		Result result = assignments.setFinished(assignment.id.toString(), student.id.toString());
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertTrue(contentAsString(result).contains("My homework assignment"));
		assertFalse(contentAsString(result).contains("Error while processing."));
		
		result = assignments.setFinished("421784921", student.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertTrue(contentAsString(result).contains("Error while processing."));
		
		result = assignments.setFinished(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
	}
	
	
	@Test
	public void setUnfinishedTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-02-02", schoolClass.id.toString(), "Homework", "My homework assignment.", student.id.toString());
		
		Result result = assignments.setUnfinished(assignment.id.toString(), student.id.toString());
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertTrue(contentAsString(result).contains("My homework assignment."));
		assertFalse(contentAsString(result).contains("Error while processing."));
		
		result = assignments.setUnfinished("42184291", student.id.toString());
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).contains("Logged In As Matthew"));
		assertTrue(contentAsString(result).contains("Error while processing."));
		
		result = assignments.setUnfinished(assignment.id.toString(), "74692d62-1761-4027-8eb6-f36ee502cdae");
		assertEquals(303, result.status());
	}
	
	@Test
	public void createPrintableDocumentTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-02-02", schoolClass.id.toString(), "Homework", "My homework assignment.", student.id.toString());
		
		Result result = assignments.createPrintableDocument(student.id);
		assertEquals(200, result.status());
		
		result = assignments.createPrintableDocument(UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(303, result.status());
	}
	
	@Test
	public void createPrintableDocumentForParentTest() {
		Parent parent = Parent.create("Jeanette", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");
		Student student = Student.create("Matthew", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2016-02-20", schoolClass.id.toString(), "Presentation", "My first presentation.", student.id.toString());
		
		Result result = assignments.createPrintableDocumentForParent(parent.id);
		assertEquals(200, result.status());
		
		result = assignments.createPrintableDocumentForParent(UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(303, result.status());
	}
	
	@Test
	public void createPrintableDocumentForTeacherTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		SchoolClass schoolClass = SchoolClass.create("Math YAY", teacher.email, teacher.id, "#F2A4F2", "math");
		Assignment assignment = Assignment.create("2015-05-28", schoolClass.id.toString(), "Other", "An other assignment, not another.", teacher.id.toString());
		
		Result result = assignments.createPrintableDocumentForTeacher(teacher.id);
		assertEquals(200, result.status());
		
		result = assignments.createPrintableDocumentForTeacher(UUID.fromString("74692d62-1761-4027-8eb6-f36ee502cdae"));
		assertEquals(303, result.status());
	}

}
