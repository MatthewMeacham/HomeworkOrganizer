import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.*;

import controllers.Application;

import com.google.common.collect.ImmutableMap;

import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.data.Form;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.WithApplication;
import play.twirl.api.Content;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest extends WithApplication {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }


//    @Test
//    public void renderTemplate() {
//        Content html = views.html.index.render("Your new application is ready.");
//        assertEquals("text/html", contentType(html));
//        assertTrue(contentAsString(html).contains("Your new application is ready."));
//    }


}
