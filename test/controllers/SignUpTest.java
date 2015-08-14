package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.contentAsString;

import java.util.HashMap;
import java.util.Map;

import models.Parent;
import models.Student;
import models.Teacher;

import org.junit.Test;

import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;

public class SignUpTest extends BaseControllerTest {

	private Signups signups = new Signups();
	
	@Test
	public void accountTypeSelectorTest() {
		Result result = signups.accountTypeSelector();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).toLowerCase().contains("sign up as a"));
	}
	
	@Test
	public void studentFormTest() {
		Result result = signups.studentForm();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("Student Sign Up"));
	}
	
	@Test
	public void parentFormTest() {
		Result result = signups.parentForm();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("Parent Sign Up"));
	}
	
	@Test
	public void teacherFormTest() {
		Result result = signups.teacherForm();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("Teacher Sign Up"));
	}
	
	@Test
	public void createStudentTest() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("name", "Matthew");
		data.put("email", "matthew@gmail.com");
		data.put("password", "hunter223");
		data.put("grade", "10");
		
		RequestBuilder request = new RequestBuilder().method("POST").uri("/signup/student/").bodyForm(data);
		Result result = Helpers.route(request);
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("logged in as matthew"));
				
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("that email is already associated with an account"));
				
		data.put("email", "matt");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid email address"));
		
		data.put("email", "matt@gmail.com");
		data.put("password", " ");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid password"));
		
		data.put("password", "hunter223");
		data.put("name", " ");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid name"));
				
		String newName = "";
		for(int i = 0; i < 250; i++) {
			newName += "m";
		}
		
		data.put("name", newName);
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("name was too long"));
				
		data.put("name", "matt");
		data.put("email", newName + "@gmail.com");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("email was too long"));		
	}
	
	@Test
	public void createParentTest() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("name", "Jeanette");
		data.put("email", "jeanette@gmail.com");
		data.put("password", "hunter223");
		
		RequestBuilder request = new RequestBuilder().method("POST").uri("/signup/parent/").bodyForm(data);
		Result result = Helpers.route(request);
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("logged in as jeanette"));

		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("that email is already associated with an account"));

		data.put("email", "jeanette");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid email address"));

		data.put("email", "jean@gmail.com");
		data.put("password", " ");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid password"));

		data.put("password", "hunter223");
		data.put("name", " ");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid name"));
				
		String newName = "";
		for(int i = 0; i < 250; i++) {
			newName += "j";
		}

		data.put("name", newName);
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("name was too long"));

		data.put("name", "jeanette");
		data.put("email", newName + "@gmail.com");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("email was too long"));		
	}
	
	@Test
	public void createTeacherTest() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("name", "Tom");
		data.put("email", "tom@gmail.com");
		data.put("password", "hunter223");
		
		RequestBuilder request = new RequestBuilder().method("POST").uri("/signup/teacher/").bodyForm(data);
		Result result = Helpers.route(request);
		assertEquals(200, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("logged in as tom"));
				
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("that email is already associated with an account"));
				
		data.put("email", "tom");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid email address"));
		
		data.put("email", "tommy@gmail.com");
		data.put("password", " ");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid password"));
		
		data.put("password", "hunter223");
		data.put("name", " ");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("invalid name"));
				
		String newName = "";
		for(int i = 0; i < 250; i++) {
			newName += "t";
		}
		
		data.put("name", newName);
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("name was too long"));
				
		data.put("name", "tom");
		data.put("email", newName + "@gmail.com");
		request.bodyForm(data);
		result = Helpers.route(request);
		assertEquals(400, result.status());
		assertTrue(contentAsString(result).toLowerCase().contains("email was too long"));		
	}
	
	public void testBasicHTMLStuff(Result result) {
		assertEquals(200, result.status());
		assertEquals("text/html", result.contentType());
		assertEquals("utf-8", result.charset());
	}
	
}
