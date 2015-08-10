import org.junit.Test;

import controllers.routes;

public class IntegrationTest extends BaseIntegrationTest{

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */	
	
	
    /* Commented out because it's generic and doesn't apply to Orgnizer.
	@Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                assertTrue(browser.pageSource().contains("Your new application is ready."));
            }
        });
    }
	*/
}
