package models;

import org.junit.Test;
import static org.junit.Assert.*;

//Teacher model testing
public class TeacherTest extends BaseModelTest {

	@Test
	public void createTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");

		assertNotNull(teacher.id);
		assertEquals("Tom", teacher.name);
		assertEquals("tom@gmail.com", teacher.email);
		assertEquals("9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", teacher.salt);
		assertEquals("e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7", teacher.password);
		assertNotEquals("TommyBoy", teacher.name);

		assertNull(Teacher.create("Tommy", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7"));
	}

	@Test
	public void authenticationTest() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		Teacher authenticated = Teacher.authenticate("tom@gmail.com", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		assertNotNull(authenticated);
		assertEquals(teacher, authenticated);
	}

	@Test
	public void existenceTest() {
		Teacher.create("Tom", "tom@gmail.com", "9d23859d828f657043c00e4ca7099d2371f1066092fb5e2730d3c2ecd9eae9de", "e611a47683b2cc971ece458e20f60207b74b1d68b53e90240678084b9d5484b7");
		assertTrue(Teacher.exists("tom@gmail.com"));
		assertTrue(Teacher.exists("ToM@gMaIl.CoM"));
	}

}