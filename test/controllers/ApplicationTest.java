package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.contentAsString;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import models.Parent;
import models.Student;
import models.Teacher;
import play.mvc.Http.Cookie;
import play.mvc.Http.RequestBuilder;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.test.Helpers;

public class ApplicationTest extends BaseControllerTest {

	private Application application = new Application();

	@Test
	public void indexTest() {
		Result result = application.index();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).toLowerCase().contains("world wide users so far"));
	}

	@Test
	public void loginTest() {
		Result result = application.login();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("Login"));
	}

	@Test
	public void logoutTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "d4c446b3862597d42603ba832db64101c4baaac64113bb0412c3f728b8b16c3f", "4abcaea679015003dabdd76577b07dbd754a1ac01988921ffd0ad25efb19175c", "10");
		Result result = application.logout(student.id);
		assertEquals(303, result.status());
	}

	@Test
	public void faqTest() {
		Result result = application.faq();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("What is Orgnizer?"));
	}

	@Test
	public void contactUsPageTest() {
		Result result = application.contactUsPage();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("Contact Us"));
	}

	@Test
	public void privacyPolicyTest() {
		Result result = application.privacyPolicy();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("Privacy Policy"));
	}

	@Test
	public void termsAndConditionsTest() {
		Result result = application.termsAndConditions();
		testBasicHTMLStuff(result);
		assertTrue(contentAsString(result).contains("Copyright (c) 2014-2015 Orgnizer"));
	}

	@Test
	public void contactUsTest() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("name", "Matthew");
		data.put("email", "matthew@gmail.com");
		data.put("subject", "Nice App!");
		data.put("message", "This application is sweet, I haven't used a paper planner since I've found this!");
		
		RequestBuilder request = new RequestBuilder().method("POST").uri("/contact/").bodyForm(data);

		Result result = Helpers.route(request);

		assertEquals(303, result.status());
	}
	
	@Test
	public void studentAuthenticateTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "d4c446b3862597d42603ba832db64101c4baaac64113bb0412c3f728b8b16c3f", "4abcaea679015003dabdd76577b07dbd754a1ac01988921ffd0ad25efb19175c", "10");
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", student.email);
		data.put("password", "hunter2");
		RequestBuilder request = new RequestBuilder().session("UserID1", student.id.toString()).method("POST").uri("/authenticate/").bodyForm(data);
		
		Result result = Helpers.route(request);
		
		assertEquals(303, result.status());

		data.put("email", "max@gmail.com");
		request = new RequestBuilder().method("POST").uri("/authenticate/").bodyForm(data);
		result = Helpers.route(request);
		
		assertEquals(400, result.status());
	}
	
	@Test
	public void parentAuthenticateTest() {
		Parent parent = Parent.create("Jeanette", "jeanette@gmail.com", "f447e95dfb3f0ef4ffd38d15ffae2dd23882c75963930a8fe58136282e8e9e85", "b6f5e938d5902cab6af81265264a44b2dd75f5330ca07690467a2610595171f4");
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", parent.email);
		data.put("password", "hunter2");
		
		RequestBuilder request = new RequestBuilder().method("POST").uri("/authenticate/").bodyForm(data);
		Result result = Helpers.route(request);
		
		assertEquals(303, result.status());
		
		data.put("email", "max@gmail.com");
		request = new RequestBuilder().method("POST").uri("/authenticate/").bodyForm(data);
		result = Helpers.route(request);
		
		assertEquals(400, result.status());
	}
	
	@Test
	public void teacherAuthenticateTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "44a3ce63b21f51041b0d72f344b1bce21b47d02c134b4988ed9619a5cab5271a", "644ab571ee0a75acfdcb3b4f26bc72bbaa35c5c8881bc94d537d2ae787c5eee2");
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", teacher.email);
		data.put("password", "hunter2");
		
		RequestBuilder request = new RequestBuilder().method("POST").uri("/authenticate/").bodyForm(data);
		Result result = Helpers.route(request);
		
		assertEquals(303, result.status());
		
		data.put("email", "max@gmail.com");
		request = new RequestBuilder().method("POST").uri("/authenticate/").bodyForm(data);
		result = Helpers.route(request);
		
		assertEquals(400, result.status());
	}

	public void testBasicHTMLStuff(Result result) {
		assertEquals(200, result.status());
		assertEquals("text/html", result.contentType());
		assertEquals("utf-8", result.charset());
	}

}
