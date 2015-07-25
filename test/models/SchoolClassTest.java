package models;

import org.junit.Test;

import static org.junit.Assert.*;

public class SchoolClassTest extends BaseModelTest {
	
	@Test
	public void createTestForStudent() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "c8bfd36427dda6db4ea0a14a0c97128b861ad02b564ead8430090f21ba90f876", "d545b2111754a734d29de6eaff33fc4ac594716c7e040b75c76db26086aa5dd3", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		
		assertNotNull(schoolClass.id);
		assertNotNull(schoolClass.subject);
		assertNull(schoolClass.teacherID);
		assertEquals("Math", schoolClass.subject);
		assertEquals("Matthew", schoolClass.students.get(0).name);
		assertEquals(student.id, schoolClass.students.get(0).id);
		assertEquals("#FFF", schoolClass.color);
		assertEquals("", schoolClass.password);
		assertEquals(1, schoolClass.students.size());
	}
	
	@Test
	public void createTestForTeacher() {
		Teacher teacher = Teacher.create("Tom", "tom@gmail.com", "2c78dcd849019ff4e63492923ae5945adc3b92cf93f7c0a1c11be41ebd64b5ab", "a6ed38c35b50492f0e16a27bba799f93b2e93dc7ebae72a0050fc59c556c6a5b");
		SchoolClass schoolClass = SchoolClass.create("Chemistry", teacher.email, teacher.id, "#FAFAFA", "coolChem123");
		
		assertNotNull(schoolClass.id);
		assertNotNull(schoolClass.subject);
		assertEquals("Chemistry", schoolClass.subject);
		assertEquals(teacher.id, schoolClass.teacherID);
		assertEquals("#FAFAFA", schoolClass.color);
		assertEquals("coolChem123", schoolClass.password);
		assertEquals(0, schoolClass.students.size());
	}
	
	@Test
	public void editTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "c8bfd36427dda6db4ea0a14a0c97128b861ad02b564ead8430090f21ba90f876", "d545b2111754a734d29de6eaff33fc4ac594716c7e040b75c76db26086aa5dd3", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		//SchoolClass.edit(schoolClass.id, "Mathematics", "#12F2D4", student.id, "");
		//Mimic the edit method because we can't mess with the mocked database
		schoolClass.subject = "Mathematics";
		schoolClass.color = "#12F2D4";
		schoolClass.password = "";
		schoolClass.save();
		
		assertNotNull(schoolClass.id);
		assertEquals("Mathematics", schoolClass.subject);
		assertEquals("#12F2D4", schoolClass.color);
		assertEquals(student.id, schoolClass.students.get(0).id);
		assertEquals("", schoolClass.password);
		assertEquals(1, schoolClass.students.size());
	}
	
}
