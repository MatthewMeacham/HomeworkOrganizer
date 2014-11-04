package controllers;

import play.*;
import play.mvc.*;
import models.*;
import play.data.*;
import views.html.*;

public class Application extends Controller {

	static Form<Student> studentForm = Form.form(Student.class);
	static Form<SchoolClass> schoolClassForm = Form.form(SchoolClass.class);

    public static Result index() {
        return ok(index.render(Student.find.all(), studentForm, schoolClassForm));
    }
	
	public static Result newStudent() {
		Form<Student> filledForm = studentForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(index.render(Student.find.all(), filledForm, schoolClassForm));
		} else {
			Student.create(filledForm.get());
			return ok(index.render(Student.find.all(), filledForm, schoolClassForm));
		}
	}

	public static Result newSchoolClass() {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(index.render(Student.find.all(), studentForm, filledForm));
		}else {
			SchoolClass.create(filledForm.get());
			return ok(index.render(Student.find.all(), studentForm, filledForm));
		}
	}
	
}
