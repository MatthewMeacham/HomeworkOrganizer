package models;

import org.junit.Test;

import static org.junit.Assert.*;

//Student model testing
public class StudentTest extends BaseModelTest {

	@Test
	public void createTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");

		assertNotNull(student.id);
		assertEquals("Matthew", student.name);
		assertEquals("matthew@gmail.com", student.email);
		assertEquals("f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", student.salt);
		assertEquals("4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", student.password);
		assertEquals("10", student.grade);
		assertNotEquals("Matthias", student.name);
		
		assertNull(Student.create("Matthiason", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "5"));
	}

	@Test
	public void authenticationTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		Student authenticated = Student.authenticate("matthew@gmail.com", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223");
		assertNotNull(authenticated);
		assertEquals(student, authenticated);
	}

	@Test
	public void existenceTest() {
		Student.create("Matthew", "matthew@gmail.com", "f9673401f27353ce150e71ae7a90c99b592463d566d73748d7f4110ae2059b19", "4087adbbc8f6fde6ae311fcf248ecfc07ed078147c36b7c69381a67aa311a223", "10");
		assertTrue(Student.exists("matthew@gmail.com"));
		assertTrue(Student.exists("MaTtHeW@gMaIl.CoM"));
	}

}