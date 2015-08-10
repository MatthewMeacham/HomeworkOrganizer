package models;

import org.junit.Test;

import static org.junit.Assert.*;

//Parent model testing
public class ParentTest extends BaseModelTest {

	@Test
	public void createTest() {
		Parent parent = Parent.create("Jeanette", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");

		assertNotNull(parent.id);
		assertEquals("Jeanette", parent.name);
		assertEquals("jeanette@gmail.com", parent.email);
		assertEquals("6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", parent.salt);
		assertEquals("b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625", parent.password);
		assertNotEquals("Jean", parent.name);
		
		assertNull(Parent.create("Jean", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625"));
	}

	@Test
	public void authenticationTest() {
		Parent parent = Parent.create("Jeanette", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");
		Parent authenticated = Parent.authenticate("jeanette@gmail.com", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");
		assertNotNull(authenticated);
		assertEquals(parent, authenticated);
	}

	@Test
	public void existenceTest() {
		Parent.create("Jeanette", "jeanette@gmail.com", "6207d516837d34bf3ced40bf94d2c0abd252b8b3446bf2e6175338ac12bd6290", "b5099d196facc9caaf740ba4114e3ff89ce057832328ca2e7e58e91ff3b2f625");
		assertTrue(Parent.exists("jeanette@gmail.com"));
		assertTrue(Parent.exists("JeAnEtTe@GmAiL.cOm"));
	}

}
