import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import play.test.FakeApplication;
import play.test.Helpers;

//Base class for integrations test(s)
public class BaseIntegrationTest {

	public static FakeApplication app;

	@BeforeClass
	public static void startApp() throws IOException {
		app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
		Helpers.start(app);
	}

	@AfterClass
	public static void stopApp() {
		Helpers.stop(app);
	}

}
