package models;

import org.junit.Test;

import static org.junit.Assert.*;

public class AssignmentTest extends BaseModelTest{
	
	@Test
	public void createTest() {
		Student student = Student.create("Matthew", "matthew@gmail.com", "bea8fd519de550c6ce6d79317ec41477d68ea3df148bc3d92d759812686c730c", "0dc1b383152ba3c2dc47b13a8bca03d8ad54ff68e81fc2c9bf7124fb30d4930a", "10");
		SchoolClass schoolClass = SchoolClass.create("Math", student.email, student.id, "#FFF", "");
		Assignment assignment = Assignment.create("2015-08-06", String.valueOf(schoolClass.id), "Homework", "My first homework assignment.", student.id.toString());
	
		assertNotNull(assignment.id);
		assertNotNull(assignment.schoolClass.id);
		assertNotNull(assignment.foreignID);
		assertEquals("August 06, 2015", assignment.dueDate);
		assertEquals(schoolClass.id, assignment.schoolClass.id);
		assertEquals("Homework", assignment.kindOfAssignment);
		assertEquals("My first homework assignment.", assignment.description);
		assertEquals(false, assignment.finished);
		assertEquals(2015, assignment.year);
		assertEquals(8, assignment.month);
		assertEquals(6, assignment.day);
		assertEquals("H", assignment.spanner);
		assertNotEquals(50, assignment.total);
	}
	
}
