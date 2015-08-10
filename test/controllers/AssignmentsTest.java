package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.contentAsString;

import java.util.HashMap;
import java.util.Map;

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
	private Student student = Student.create("Matthew", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
	private SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
	
	@Test
	public void createTest() {
		/* NOT WORKING ATM
		 * 
		Map<String, String> data = new HashMap<String, String>();
		data.put("dueDate", "2015-08-06");
		data.put("description", "My first homework assignment.");
		data.put("schoolClassId", schoolClass.id.toString());
		data.put("kindOfAssignment", "Homework");
		
		assignments.create(student.id.toString());
		RequestBuilder request = new RequestBuilder().method("POST").uri("/profileClassChanged/?studentID=" + student.id.toString()).bodyForm(data);
		
		Result result = Helpers.route(request);

		assertEquals(200, result.status());
		*/
	}
	
}
